(ns protocol55.biscuit.component
  (:require [protocol55.biscuit.core :as b]
            [protocol55.biscuit.common :as c]
            ["document-register-element"]
            goog.object)
  (:require-macros [protocol55.biscuit.component :refer [defcomponent]]))

(defprotocol ICustomElementLifecycle
  (created-callback! [this])
  (attached-callback! [this])
  (detached-callback! [this])
  (attribute-changed-callback! [this attr-name old-val new-val]))

(defn callback-fn3 [event-kw]
  (case event-kw
    :created
    (fn []
      (this-as this
        (created-callback! this)))
    :attached
    (fn []
      (this-as this
        (attached-callback! this)))
    :detached
    (fn []
      (this-as this
        (detached-callback! this)))
    :attribute-changed
    (fn [attr-name old-val new-val]
      (this-as this
        (attribute-changed-callback! this attr-name old-val new-val)))))

(defn register-element2 [tag]
  (.registerElement js/document
    tag
    #js {:prototype
         (.create js/Object (.-prototype js/HTMLElement)
                  #js {:createdCallback
                       #js {:value (callback-fn3 :created)}
                       :attachedCallback
                       #js {:value (callback-fn3 :attached)}
                       :detachedCallback
                       #js {:value (callback-fn3 :detached)}
                       :attributeChangedCallback
                       #js {:value (callback-fn3 :attribute-changed)}})}))

(defprotocol IReactiveState
  (initialize-state! [this] "Initial setup of the state.")
  (get-state [this] "Returns the state of the component."))

(defprotocol ICreated
  (created! [this]))

(defprotocol IAttached
  (attached! [this]))

(defprotocol IDetached
  (detached! [this]))

(defprotocol IChanged
  (attribute-changed! [this attr-name old-val new-val]))

(defprotocol IRender
  (render! [this]))

(defprotocol IReactiveProps
  (initialize-props! [this])
  (get-props [this]))

(defprotocol IEvented
  (listen [this event-name callback])
  (unlisten [this event-name]))

(def state-property "@@biscuit/state")
(def props-property "@@biscuit/props")

(defn wire-normalized-outputs [element outputs]
  (doseq [[attribute event-name deregister-property] outputs]
    (b/set-attribute attribute
                     (fn [el _ callback]
                       (listen el event-name callback)))
    
    ))

(defn wire-normalized-inputs [element inputs]
  (doseq [[attribute property k] inputs]
    (b/set-attribute attribute
                     (fn [el _ value]
                       (goog.object/set el property value)))

    (let [private-prop (str "_" property)]
      (js/Object.defineProperty element property
        #js {:get (fn []
                    (when-not (js-in private-prop element)
                      (goog.object/set element private-prop (.getAttribute element attribute)))
                    (goog.object/get element private-prop))
             :set (fn [v]
                    (goog.object/set element private-prop v)
                    (let [props (get-props element)]
                      (swap! props assoc k v)))}))))

(defn extend-element [Element inputs render-fn]
  (extend-type Element
    IReactiveProps
    (initialize-props! [this]
      (let [props (atom nil)]
        (goog.object/set this props-property props)
        (add-watch props :render-watch #(render! this))))

    (get-props [this]
      (goog.object/get this props-property))

    IRender
    (render! [this]
      (let [props (get-props this)]
        (js/setTimeout (fn [] (b/patch* this #(render-fn props))) 0)))

    ICustomElementLifecycle
    (created-callback! [this]
      (initialize-props! this)
      (wire-normalized-inputs this inputs)
      (when (satisfies? ICreated this)
        (created! this)))

    (attached-callback! [this]
      (when (satisfies? IAttached this)
        (attached! this))

      (render! this))

    (detached-callback! [this]
      (when (satisfies? IDetached this)
        (detached! this)))

    (attribute-changed-callback! [this attr-name old-val new-val]
      (println attr-name (type new-val))
      (when (satisfies? IChanged this)
        (attribute-changed! this))

      (render! this))))

(defprotocol IEvent
  (event-detail [this]))

(extend-type js/CustomEvent
  IEvent
  (event-detail [this]
    (goog.object/get this "detail")))

(defn ->Event [event-name detail]
  (js/CustomEvent. event-name #js {:detail detail}))

(comment

  ;; TODO: defcomponent needs to defn a render fn for specing reasons
  ;; Oh, we could actually have a name passed via the options

  (comment
    [::my-element {"(clicked)" (fn [e] )}]
    [:div {:onclick (fn [e] (dispatch (js/CustomEvent. "clicked" #js {})))}]
    )

  ;; TODO: outputs design
  ;; (::click) (fn [e] )
  ;; (:click) (fn [e] )
  ;; ::clicked -> protocol55-biscut-component-clicked
  ;; -- Installs via attribute setter listener
  ;; needs to be listed in outputs for this

  (defcomponent MyElement
    {:selector ::my-element
     :inputs [::foo ::some-prop]
     :outputs [::clicked]
     }
    [props]
    ;;[:div {:onclick #(dispatch (->Event ::clicked {:x 1}))} "Click me!"]
    [:div "::foo " (::foo @props)]
    [:div "::some-prop " (::some-prop @props)])

  (defcomponent StatefulTest1
    {:selector ::stateful-test-1}
    [props]
    [:div
     (pr-str @props)
     [:button {:onclick (fn [] (swap! props update ::c inc))} "Click"]
     [:span "foo"]
     [::my-element {::foo (::c @props)
                    ;;[(::foo)] 
                    ;;(::clicked) (fn [e] (println (event/detail e)))
                    }]
     [:pre {::c/for [i (range 10)]} "hoopla"]])

  ;; or

  (def root (.getElementById js/document "root"))
  (b/patch* root (fn [] (b/ui [:div 1])))
  (b/patch* root (fn [] (b/ui [::stateful-test-1])))

  )
