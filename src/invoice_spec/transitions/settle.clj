(ns invoice-spec.transitions.settle
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [result.core :as result]
            [invoice-spec.dates :as dates]
            [invoice-spec.api.documents :as api]
            [invoice-spec.models.document :as document]
            [clojure.core.async :refer [chan <!! >!! close! go <! timeout]]
            [invoice-spec.transitions.transition :as transition]))

(defn- validate [document]
  (let [status "settled"]
    (cond
      (not= status (:status document))
        (result/failure {:error "status-mismatch"
                         :expected status
                         :got (:status document)})

      :else (result/success))))

(defn validate-receipt [document]
  (result/on-success [result (<!! (api/related-documents document))]
    (let [receipt (last (filter #(= "Receipt" (:type %)) (:documents result)))
          status (document/expected-final-status receipt)]
      (cond
        (nil? receipt)
          (result/failure {:error "no receipt found"
                           :type (:type document)
                           :status (:status document)})

        (not= status (:status receipt))
          (result/failure {:error "invalid receipt satus"
                           :expected status
                           :receipt-type (:type receipt)
                           :got (:status receipt)})
        :else
          (result/success)))))

(defn process-success [final]
  (result/enforce-let [valid? (document/validate final)
                       valid-logic? (validate final)
                       receipt-present? (validate-receipt final)]
    (result/success {:document final})))

(defmethod transition/operate :settle [context transition]
  (result/on-success [document (result/presence (:document context))]
    (let [settled (<!! (api/settle document))]
      (if (result/succeeded? settled)
        (process-success settled)
        (transition/process-failure settled document)))))
