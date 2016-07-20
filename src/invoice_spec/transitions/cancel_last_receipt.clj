(ns invoice-spec.transitions.cancel-last-receipt
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [result.core :as result]
            [invoice-spec.dates :as dates]
            [invoice-spec.api.documents :as api]
            [invoice-spec.models.document :as document]
            [clojure.core.async :refer [chan <!! >!! close! go <! timeout]]
            [invoice-spec.transitions.transition :as transition]))

(defn validate [invoice receipt]
  (let [status "canceled"]
    (cond

      (not= status (:status receipt))
        (result/failure {:error "invalid receipt satus"
                         :expected status
                         :receipt-type (:type receipt)
                         :got (:status receipt)})

      (not= "final" (:status invoice))
        (result/failure {:error "receipt canceled but invoice not final"
                         :status-invoice (:status invoice)
                         :status-receipt (:status receipt)})

      :else
        (result/success))))

(defn process-success [invoice receipt]
  (result/enforce-let [valid-logic? (validate invoice receipt)]
    (result/success {:document invoice})))

(defmethod transition/operate :cancel-last-receipt [context transition]
  (result/enforce-let [document (result/presence (:document context) "no-document")
                       related (<!! (api/related-documents context document))]
    (if-let [receipt (last (filter #(= "Receipt" (:type %)) (:documents related)))]
      (let [canceled (<!! (api/cancel context receipt))
            document (<!! (api/reload-document context document))]
        (if (result/succeeded? canceled)
          (process-success document canceled)
          (transition/process-failure canceled document)))
      (result/success {:document document}))))
