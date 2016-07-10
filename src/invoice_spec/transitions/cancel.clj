(ns invoice-spec.transitions.cancel
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [result.core :as result]
            [invoice-spec.dates :as dates]
            [invoice-spec.api.documents :as api]
            [invoice-spec.models.document :as document]
            [clojure.core.async :refer [chan <!! >!! close! go <! timeout]]
            [invoice-spec.transitions.transition :as transition]))

(defn- validate [document]
  (cond
    (not= "canceled" (:status document))
      (result/failure "status-mismatch")

    :else (result/success)))

(defn process-success [final]
  (result/enforce-let [valid? (document/validate final)
                       valid-logic? (validate final)]
    (result/success {:document final})))

(defmethod transition/operate :cancel [context transition]
  (result/on-success [document (result/presence (:document context))]
    (let [canceled (<!! (api/cancel document))]
      (if (result/succeeded? canceled)
        (process-success canceled)
        (transition/process-failure canceled document)))))
