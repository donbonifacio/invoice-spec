(ns invoice-spec.core
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]))

;; https://github.com/weareswat/invoicexpress/pull/2498

(s/def ::invoice (s/keys :req-un [::sequence-number ::serie
                                  ::date ::due-date
                                  ::client
                                  ::items]))

(s/def ::sequence-number (s/and integer? pos?))
(s/def ::serie (s/with-gen string? #(s/gen #{"2016"})))
(s/def ::date (s/with-gen string? #(s/gen #{"01/01/2016"})))
(s/def ::due-date (s/with-gen string? #(s/gen #{"01/01/2016"})))

(s/def ::client (s/keys :req-un [::client-name ::client-code]))
(s/def ::client-name (s/with-gen string? #(s/gen #{"Pedro" "António" "Zé"})))
(s/def ::client-code (s/with-gen integer? #(s/gen (set (range 10)))))

(s/def ::items (s/+ ::item))
(s/def ::item (s/keys :req-un [::item-name ::item-description
                               ::item-quantity ::item-unit-price]))
(s/def ::item-name (s/with-gen string? #(s/gen (->> (range 10) (map (fn [n] (str "Item " n))) (set)))))
(s/def ::item-description string?)
(s/def ::item-quantity (s/and number? #(<= 1 % 100)))
(s/def ::item-unit-price (s/and number? #(<= 1 % 100)))

#_(s/def ::transition #{:finalize
                      [:pay]
                      [:cancel]
                      [:pay :cancel-payment]
                      [:debit-note :pay]
                      [:pay :credit-note]
                      })


(comment
  (gen/generate (s/gen ::invoice))
  (gen/generate (s/gen ::item)))



