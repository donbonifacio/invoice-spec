(ns invoice-spec.models.item
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]))

#_(gen/generate (s/gen ::item))

(s/def ::items (s/+ ::item))
(s/def ::item (s/keys :req-un [::name ::description
                               ::quantity ::unit-price]))
(s/def ::name (s/with-gen string? #(s/gen (->> (range 10) (map (fn [n] (str "Item " n))) (set)))))
(s/def ::description string?)
(s/def ::quantity (s/and number? #(<= 1 % 100)))
(s/def ::unit-price (s/and number? #(<= 1 % 100)))

