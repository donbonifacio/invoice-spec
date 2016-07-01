(ns invoice-spec.models.tax
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]))

(s/def ::tax (s/keys :req-un [::name ::id ::value]))

(s/def ::name (s/with-gen string?
                #(s/gen #{"IVA"})))
(s/def ::id (s/with-gen (s/and integer? pos?)
              #(s/gen (s/int-in 1 100000))))
(s/def ::value (s/with-gen number?
                 #(s/gen (s/int-in 0 100))))
