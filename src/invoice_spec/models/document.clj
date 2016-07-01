(ns invoice-spec.models.document
  (:require [clojure.spec :as s]
            [invoice-spec.models.client]
            [invoice-spec.models.item]
            [clojure.spec.gen :as gen]))

(s/def ::document (s/keys :req-un [::sequence_number
                                   ::type ::status
                                   ::date ::due_date
                                   ::sum ::discount ::taxes ::total
                                   ::archived ::permalink ::reference
                                   :invoice-spec.models.client/client
                                   :invoice-spec.models.item/items]
                          :opt-un [::saft_hash
                                   ::cancel_reason
                                   ::currency
                                   ::before_taxes]))

(s/def ::type #{"Invoice" "CreditNode" "DebitNote" "Receipt"})
(s/def ::status #{"draft" "final" "settled" "canceled"})

(defn sequence-number? [raw]
  (re-matches #"^\d+\/\w+" raw))

(s/def ::sequence_number (s/with-gen
                           (s/or :complete-number sequence-number?
                                 :draft #{"draft"})
                           #(s/gen #{"draft"})))
(s/def ::date (s/with-gen string? #(s/gen #{"01/01/2016"})))
(s/def ::due_date (s/with-gen string? #(s/gen #{"01/01/2016"})))

(s/def ::discount number?)
(s/def ::sum number?)
(s/def ::taxes number?)
(s/def ::total number?)
(s/def ::before_taxes number?)
(s/def ::archived boolean?)
(s/def ::permalink string?)
(s/def ::reference (s/nilable string?))

(s/def ::saft_hash (s/and string? #(= 4 (count %))))
(s/def ::currency #{"Euro"})
(s/def ::cancel_reason string?)

(s/def ::transition #{
                      [:cancel]
                      [:pay ]
                      [:debit-note]
                      [:credit-note]
                      })
(s/def ::transitions (s/with-gen (s/+ ::transition)
                                 #(gen/vector (s/gen ::transition) 1 5)))

(defn random-new []
  (-> (s/keys :req-un [::type
                       ::date ::due_date
                       :invoice-spec.models.client/client
                       :invoice-spec.models.item/items])
      (s/gen)
      (gen/generate)
      (assoc :status "draft")))

(defn new-invoice []
  (-> (random-new)
      (dissoc :type)))

(comment
  (random-new)
  (gen/generate (s/gen ::document))
  (def transitions-gen (s/gen ::transitions))

  (gen/sample transitions-gen)

  (gen/generate
    (gen/bind (s/gen ::document) (fn [doc] (gen/tuple (gen/return doc)
                                                     transitions-gen)))))



