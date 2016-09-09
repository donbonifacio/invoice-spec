(ns invoice-spec.models.document
  (:require [clojure.spec :as s]
            [invoice-spec.models.client]
            [invoice-spec.models.item]
            [result.core :as result]
            [invoice-spec.models.preds :as preds]
            [clojure.spec.gen :as gen]))

(s/def ::document (s/keys :req-un [::sequence_number
                                   ::type ::status
                                   ::date ::due_date
                                   ::sum ::discount ::taxes ::total
                                   ::archived ::permalink ::reference
                                   :invoice-spec.models.client/client
                                   :invoice-spec.models.item/items]
                          :opt-un [::saft_hash
                                   ::sequence_id
                                   ::reference
                                   ::observations
                                   ::id
                                   ::retention
                                   ::cancel_reason
                                   ::currency
                                   ::before_taxes]))

(s/def ::guide (s/merge ::document
                        (s/keys :req-un [:invoice-spec.models.guide/type
                                         :invoice-spec.models.guide/loaded_at
                                         :invoice-spec.models.address/address_to
                                         :invoice-spec.models.address/address_from]
                                :opt-un [:invoice-spec.models.guide/license_plate])))

(s/def ::id nat-int?)
(s/def ::sequence_id nat-int?)
(s/def ::type #{"Invoice" "SimplifiedInvoice" "InvoiceReceipt" "CreditNode" "DebitNote" "Receipt" "Transport" "Devolution" "Shipping"})
(s/def :invoice-spec.models.guide/type #{"Transport" "Devolution" "Shipping"})
(s/def ::primary-type #{"Invoice" "InvoiceReceipt" "SimplifiedInvoice"})
(s/def ::status #{"draft" "deleted" "final" "settled" "canceled"})

(defn sequence-number? [raw]
  (re-matches #"^\d+\/\w+" raw))

(s/def ::sequence_number (s/with-gen
                           (s/or :complete-number sequence-number?
                                 :draft #{"draft"})
                           #(s/gen #{"draft"})))
(s/def ::date (s/with-gen string? #(s/gen #{"01/01/2016"})))
(s/def ::due_date (s/with-gen string? #(s/gen #{"01/01/2016"})))
(s/def :invoice-spec.models.guide/loaded_at (s/with-gen string? #(s/gen #{"01/01/2016 19:00:00"})))
(s/def :invoice-spec.models.guide/license_plate (s/with-gen string? #(s/gen #{"EI-12-12"})))

(s/def ::discount preds/currency?)
(s/def ::sum preds/currency?)
(s/def ::taxes preds/currency?)
(s/def ::total preds/currency?)
(s/def ::before_taxes preds/currency?)
(s/def ::archived boolean?)
(s/def ::permalink string?)
(s/def ::reference (s/nilable string?))
(s/def ::retention (s/with-gen (s/nilable (s/and nat-int? #(<= 0 % 100)))
                     #(s/gen (s/int-in 0 99))))

(s/def ::saft_hash (s/and string? #(= 4 (count %))))
(s/def ::currency #{"Euro"})
(s/def ::cancel_reason string?)
(s/def ::reference (s/nilable string?))
(s/def ::observations (s/nilable preds/medium-string?))

(s/def ::transition #{
                      [:cancel]
                      [:pay ]
                      [:debit-note]
                      [:credit-note]
                      })
(s/def ::transitions (s/with-gen (s/+ ::transition)
                                 #(gen/vector (s/gen ::transition) 1 5)))


(s/def ::basic-document
  (s/keys :req-un []
          :opt-un []))

(def fields-for-create [;; global
                        :date :due_date
                        :type
                        :client
                        :items
                        :reference :observations :retention

                        ;; guides
                        :loaded_at
                        :license_plate
                        :address_to
                        :address_from])

(defn document-generator []
  (gen/fmap (fn [document]
              (select-keys document fields-for-create))
            (s/gen ::document)))

(defn type-generator []
  (s/gen ::primary-type))

(defn random-new []
  (-> (document-generator)
      (gen/generate)
      (assoc :status "draft")))

(defn invoice-generator []
  (document-generator))

(defn new-invoice []
  (-> (random-new)
      (assoc :type "Invoice")))

#_(new-invoice)

(s/fdef new-invoice
  :ret ::document)

(defn expected-final-status [document]
  (if (= "InvoiceReceipt" (:type document))
    "settled"
    "final"))

(s/fdef expected-final-status
  :args (s/cat :document ::document)
  :ret ::status)

(defn validate [document]
  (if (s/valid? ::document document)
    (result/success document)
    (result/failure (s/explain-str ::document document))))
