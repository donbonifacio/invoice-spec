(ns invoice-spec.models.item
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [invoice-spec.models.preds :as preds]
            [invoice-spec.models.item-names]
            [invoice-spec.models.tax :as tax]))

#_(gen/sample (s/gen ::item))

(s/def ::items (s/with-gen (s/+ ::item)
                           #(gen/vector (s/gen ::item) 1 3)))

(s/def ::item (s/keys :req-un [:invoice-spec.models.item-names/name ::description
                               ::quantity ::unit_price]
                      :opt-un [:invoice-spec.models.tax/tax
                               ::discount ::discount_amount
                               ::total ::subtotal]))

(s/def ::description (s/nilable string?))

(s/def ::quantity preds/quantity-num?)
(s/def ::unit_price preds/currency?)
(s/def ::discount (s/or :no-discount (s/with-gen zero? #(s/gen #{0}))
                        :with-discount (s/with-gen (s/and nat-int? #(<= 0 % 100))
                                         #(s/gen (s/int-in 1 100)))))
(s/def ::discount_amount preds/currency?)
(s/def ::total preds/currency?)
(s/def ::subtotal preds/currency?)
