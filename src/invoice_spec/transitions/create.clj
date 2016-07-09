(ns invoice-spec.transitions.create
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [result.core :as result]
            [invoice-spec.transitions.transition :as transition]))

(defmethod transition/operate :create [context transition]
  (update-in context [:results] conj {:transition transition :result (result/success)}))
