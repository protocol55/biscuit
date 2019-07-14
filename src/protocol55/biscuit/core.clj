(ns protocol55.biscuit.core
  (:require [hiccup2.core :as h]
            [hiccup.util :as util]
            [hiccup.compiler :as hc]
            [camel-snake-kebab.core :as csk]
            [clojure.set :refer [rename-keys]]))

(def void-tags
  #{"area" "base" "br" "col" "command" "embed" "hr" "img" "input" "keygen" "link"
    "meta" "param" "source" "track" "wbr"})

(defn void-tag? [tag]
  (some? (void-tags tag)))

(defn fragment-tag? [tag]
  (= "<>" tag))

(defn static-attr? [k]
  (= (namespace k) "static"))

(defn attrs* [attrs]
  (when attrs
    (let [attrs (filter #(some? (val %)) attrs)
          static-pairs (->> attrs (filter #(static-attr? (key %))))
          dynamic-pairs (->> attrs (filter #(not (static-attr? (key %)))))
          static-vec (->> (mapcat (fn [[n v]] [(name n) v]) static-pairs) vec)]
      (concat [static-vec] (mapcat (fn [[n v]] [(name n) v]) dynamic-pairs)))))

(defn write-event
  [{:keys [kind tag attrs content id form] :as parse-event}]
  (case kind
    :expression
    {:write form}

    :open
    {:write `(protocol55.biscuit.core/element-open* ~tag ~id ~@(attrs* attrs) )}

    :void
    {:write `(protocol55.biscuit.core/element-void* ~tag ~id ~@(attrs* attrs))}

    :close
    {:write `(protocol55.biscuit.core/element-close* ~tag)}

    :text
    {:write `(protocol55.biscuit.core/text* ~content)}))

(defn expression-form? [form]
  (-> (meta form) ::expression?))

(defn get-key [form]
  (-> form meta :key))

(defmulti directive (fn [k normalized-form] k))
(defmulti structural-directive (fn [k normalized-form] k))

;; TODO: Assert only one structural directive

(defn structural-directive-key [attrs]
  (let [ks (-> structural-directive methods keys)]
    (some #(when (attrs %) %) ks)))

(comment

  (defmethod structural-directive ::for [_ [tag attrs children]]
    `(doseq ~(::for attrs)
       (ui [~tag ~(dissoc attrs ::for) ~@children]))))

;; vector? -> contains structural directive attr? -> expression or vector?
;; recur

(defn normalize-tag [kw]
  (if (namespace kw)
    (keyword (str (clojure.string/replace (namespace kw) #"\." "-") "-" (name kw)))
    kw))

(defn attr->property [kw]
  (csk/->camelCase (name kw)))

(comment
  (namespace :foo)
  (normalize-attrs {::foo 1 :value 2})
  (normalize-tag ::foo))

(defn normalize-attrs [attrs]
  (if-some [namespaced-keys (seq (filter namespace (keys attrs)))]
    (rename-keys attrs (->> namespaced-keys
                            (map (juxt identity normalize-tag))
                            (into {})))
    attrs))

(defn fragmentize*
  [form]
  (when form
    (cond
      (expression-form? form)
      [{:kind :expression :form form}]

      (vector? form)
      (let [[_ attrs children] (hc/normalize-element form)
            tag (first form)]
        (if-some [k (structural-directive-key attrs)]
          ;; expand into structural expression 
          (recur (with-meta (structural-directive k [tag attrs children])
                            {::expression? true}))
          ;; handle all other tags
          (let [tag (-> tag normalize-tag name)
                attrs (normalize-attrs attrs)
                id (get-key form)
                void? (void-tag? tag)]
            (if (fragment-tag? tag)
              (lazy-seq
                (mapcat fragmentize* children))
              (lazy-seq
                (cons {:kind (if void? :void :open) :tag tag :attrs attrs :id id}
                      (concat (mapcat fragmentize* children)
                              (when-not void? [{:kind :close :tag tag}]))))))))

      :else
      [{:kind :text :content form}])))

(defn fragmentize [form]
  (->> (fragmentize* form) (map write-event)))

(defmacro ui [forms]
  (let [events (fragmentize forms)]
    `(do
       ~@(for [{:keys [write] :as event} events]
          write))))

(defmacro defpartial [sym bindings forms]
  `(defn ~sym ~bindings (protocol54.biscuit.core/ui ~forms)))

(comment
  (fragmentize* [:div 'i])
  (fragmentize* [:div 'i])
  (fragmentize* '[:div (::foo x)])
  (fragmentize* '[:div {::for [i (range 100)]} "foo"])
  (fragmentize* '[::foo @i])
  (fragmentize*
    '[:div
     [:button {:onclick (fn [] (swap! state inc))} "Click"]
     [:span "foo"]
     [:pre "hoopla"]
     ^::expression? (println "foo")
     @state
     
     ]
    )

  (macroexpand-1 '(ui [:div "foo" (protocol55.biscuit.core/text* @state)]))

  (fragmentize* '[:div "::foo " (::foo @props)])

  (fragmentize* '[:div.foo.bar
       [:button {:onclick (fn [] (println "test"))
                 :id "foo"
                 :class "bar baz"
                 :disabled true
                 :readonly false
                 }
        "Hello World"]
       [:<>
        [:span "Foo"]]
       [:div {::for [i (range 1000)]} i]
       [:div {::when on} "Is ON"]])

  )
