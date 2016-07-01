(ns invoice-spec.models.item
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [invoice-spec.models.tax :as tax]))

#_(gen/generate (s/gen ::item))

(s/def ::items (s/with-gen (s/+ ::item)
                           #(gen/vector (s/gen ::item) 1 3)))
(s/def ::item (s/keys :req-un [::name ::description
                               ::quantity ::unit_price]
                      :opt-un [:invoice-spec.models.tax/tax
                               ::discount ::discount_amount
                               ::total ::subtotal]))

(def pos-or-zero? (comp not neg?))

(s/def ::name (s/with-gen string? #(s/gen (->> (range 10) (map (fn [n] (str "Item " n))) (set)))))
(s/def ::description (s/nilable string?))
(s/def ::quantity (s/and number? #(<= 1 % 100)))
(s/def ::unit_price (s/and number? #(<= 1 % 100)))
(s/def ::discount (s/and number? pos-or-zero?))
(s/def ::discount_amount (s/and number? pos-or-zero?))
(s/def ::total (s/and number? pos?))
(s/def ::subtotal (s/and number? pos?))

