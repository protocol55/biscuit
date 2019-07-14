(ns protocol55.biscuit.component
  (:require [protocol55.biscuit.core :as b]
            [camel-snake-kebab.core :as csk]))

(defn- html-tag [tag]
  (-> tag b/normalize-tag name))

(defn- js-property [k]
  (csk/->camelCase (name k)))

(defn normalize-inputs
  "Returns a seq of [attribute input key]."
  [inputs]
  (mapv (fn [input]
          (cond
            (keyword? input)
            [(html-tag input) (js-property input) input]

            (vector? input)
            input))
        inputs))

(defmacro defcomponent [ElementSymbol {:keys [selector inputs]} bindings & body]
  #_(assert (and (keyword? selector) (namespace selector)) "Selector must be namespaced.")
  `(let [tag-name# ~(html-tag selector)]
     (defonce ~ElementSymbol (protocol55.biscuit.component/register-element2 tag-name#))
     (protocol55.biscuit.component/extend-element
       ~ElementSymbol
       ~(normalize-inputs inputs)
       (fn ~bindings (protocol55.biscuit.core/ui [:<> ~@body])))
     ~ElementSymbol))
