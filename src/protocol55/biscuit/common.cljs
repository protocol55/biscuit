(ns protocol55.biscuit.common
  (:require [protocol55.biscuit.core :as b] :reload-all)
  (:require-macros protocol55.biscuit.common))

(comment
  (macroexpand-1 '(b/ui [:div {::for [i (range 1000)]} i]))
  (macroexpand-1 '(b/ui [:div {::when true} "hi"]))

  (def root (.getElementById js/document "root"))

  (defn off-block []
    (b/ui [:div "Is Off"]))

  (defn my-comp [{:keys [on]}]
    (b/ui
      [:div.foo.bar
       [:button {:onclick (fn [] (println "test"))
                 :id "foo"
                 :class "bar baz"
                 :disabled true
                 :readonly false}
        "Hello World"]
       [:<>
        [:span "Foo"]]
       [:div {::if on ::else (off-block)}
        "Is ON"]
       #_[::block {:# 1}
        [:div "Is OFF"]]
       [:div {::for [i (range 10)]} i]
       [:div {::when on} "Is ON"]]))

  (do b/patch)

  (b/patch* root (fn [] (my-comp {:on false})))
  
  )
