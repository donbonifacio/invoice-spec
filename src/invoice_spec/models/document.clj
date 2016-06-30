(ns invoice-spec.models.document
  (:require [clojure.spec :as s]
            [invoice-spec.models.client]
            [invoice-spec.models.item]
            [clojure.spec.gen :as gen]))

(s/def ::document (s/keys :req-un [::sequence_number ::serie
                                   ::type ::status
                                   ::date ::due-date
                                   :invoice-spec.models.client/client
                                   :invoice-spec.models.item/items]))

(s/def ::type #{nil "CreditNode" "DebitNote" "Receipt"})
(s/def ::status #{"draft" "sent" "settled" "canceled"})

(s/def ::sequence_number (s/and integer? pos?))
(s/def ::serie (s/with-gen string? #(s/gen #{"2016"})))
(s/def ::date (s/with-gen string? #(s/gen #{"01/01/2016"})))
(s/def ::due-date (s/with-gen string? #(s/gen #{"01/01/2016"})))

(s/def ::transition #{
                      [:cancel]
                      [:pay ]
                      [:debit-note]
                      [:credit-note]
                      })
(s/def ::transitions (s/with-gen (s/+ ::transition)
                                 #(gen/vector (s/gen ::transition) 1 5)))


(comment
  (gen/generate (s/gen ::document))
  (def transitions-gen (s/gen ::transitions))

  (gen/sample transitions-gen)

  (gen/generate
    (gen/bind (s/gen ::document) (fn [doc] (gen/tuple (gen/return doc)
                                                     transitions-gen)))))



