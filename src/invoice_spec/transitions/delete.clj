(ns invoice-spec.transitions.delete
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [result.core :as result]
            [invoice-spec.dates :as dates]
            [invoice-spec.api.documents :as api]
            [invoice-spec.models.document :as document]
            [clojure.core.async :refer [chan <!! >!! close! go <! timeout]]
            [invoice-spec.transitions.transition :as transition]))

(defn- validate [document previous]
  (cond
    (not= "deleted" (:status document))
      (result/failure "status-mismatch")

    (not= "draft" (:status previous))
      (result/failure {:error "deleted a non draft document"
                       :previous-status (:status previous)
                       :status (:status document)})

    :else (result/success)))

(defn process-success [final previous]
  (result/enforce-let [valid? (document/validate final)
                       valid-logic? (validate final previous)]
    (result/success {:document final})))

(defmethod transition/operate :delete [context transition]
  (result/on-success [document (result/presence (:document context))]
    (let [deleted (<!! (api/delete document))]
      (if (result/succeeded? deleted)
        (process-success deleted document)
        (transition/process-failure deleted document)))))
