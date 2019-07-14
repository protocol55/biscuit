(ns protocol55.biscuit.core
  (:require ["incremental-dom" :refer [elementOpen elementClose elementVoid
                                       text patch attributes]]
            goog.object)
  (:require-macros [protocol55.biscuit.core :refer [ui]]))

(defn element-open*
  ([tag id sattrs]
   (elementOpen tag id (js->clj sattrs)))
  ([tag id sattrs pk1 pv1]
   (elementOpen tag id (js->clj sattrs) pk1 pv1))
  ([tag id sattrs pk1 pv1 pk2 pv2]
   (elementOpen tag id (js->clj sattrs) pk1 pv1 pk2 pv2))
  ([tag id sattrs pk1 pv1 pk2 pv2 pk3 pv3]
   (elementOpen tag id (js->clj sattrs) pk1 pv1 pk2 pv2 pk3 pv3))
  ([tag id sattrs pk1 pv1 pk2 pv2 pk3 pv3 pk4 pv4]
   (elementOpen tag id (js->clj sattrs) pk1 pv1 pk2 pv2 pk3 pv3 pk4 pv4))
  ([tag id sattrs pk1 pv1 pk2 pv2 pk3 pv3 pk4 pv4 pk5 pv5]
   (elementOpen tag id (js->clj sattrs) pk1 pv1 pk2 pv2 pk3 pv3 pk4 pv4 pk5 pv5))
  ([tag id sattrs pk1 pv1 pk2 pv2 pk3 pv3 pk4 pv4 pk5 pv5 & attrs]
   (apply elementOpen tag id (js->clj sattrs) pk1 pv1 pk2 pv2 pk3 pv3 pk4 pv4 pk5 pv5 attrs)))

(defn element-close* [tag]
  (elementClose tag))

(defn element-void*
  ([tag id sattrs]
   (elementVoid tag id (js->clj sattrs)))
  ([tag id sattrs pk1 pv1]
   (elementVoid tag id (js->clj sattrs) pk1 pv1))
  ([tag id sattrs pk1 pv1 pk2 pv2]
   (elementVoid tag id (js->clj sattrs) pk1 pv1 pk2 pv2))
  ([tag id sattrs pk1 pv1 pk2 pv2 pk3 pv3]
   (elementVoid tag id (js->clj sattrs) pk1 pv1 pk2 pv2 pk3 pv3))
  ([tag id sattrs pk1 pv1 pk2 pv2 pk3 pv3 pk4 pv4]
   (elementVoid tag id (js->clj sattrs) pk1 pv1 pk2 pv2 pk3 pv3 pk4 pv4))
  ([tag id sattrs pk1 pv1 pk2 pv2 pk3 pv3 pk4 pv4 pk5 pv5]
   (elementVoid tag id (js->clj sattrs) pk1 pv1 pk2 pv2 pk3 pv3 pk4 pv4 pk5 pv5))
  ([tag id sattrs pk1 pv1 pk2 pv2 pk3 pv3 pk4 pv4 pk5 pv5 & attrs]
   (apply elementVoid tag id (js->clj sattrs) pk1 pv1 pk2 pv2 pk3 pv3 pk4 pv4 pk5 pv5 attrs)))

(defn text* [s]
  (text s))

(defn- boolean-attr-hook [el attr value]
  (goog.object/set el attr value))

(def boolean-attrs ["readonly" "checked" "disabled"])

(doseq [attr boolean-attrs]
  (goog.object/set attributes attr boolean-attr-hook))

(defn set-attribute [attr f]
  (goog.object/set attributes attr f))

(defn patch* [node f]
  (patch node f))
