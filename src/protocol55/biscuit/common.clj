(ns protocol55.biscuit.common
  (:require [protocol55.biscuit.core :as b]))

(defmethod b/structural-directive ::for [_ [tag attrs children]]
  `(doseq ~(::for attrs)
     (protocol55.biscuit.core/ui [~tag ~(dissoc attrs ::for) ~@children])))

(defmethod b/structural-directive ::when [_ [tag attrs children]]
  `(when ~(::when attrs)
     (protocol55.biscuit.core/ui [~tag ~(dissoc attrs ::when) ~@children])))

(defmethod b/structural-directive ::if [_ [tag attrs children]]
  (let [if-pred (::if attrs)
        else-expr (::else attrs)
        attrs (dissoc attrs ::if ::else)]
    `(if ~if-pred
       (protocol55.biscuit.core/ui [~tag ~attrs ~@children])
       ~(or else-expr nil))))

;; TODO: Create dynamic var for *env* that collects templates etc. When
;; referencing blocks like in ::if (get-in *env* [:templates (::else attrs)])
;; This will give us the actual expression
