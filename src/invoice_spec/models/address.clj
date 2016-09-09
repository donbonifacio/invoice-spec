(ns invoice-spec.models.address
  (:require [clojure.spec :as s]
            [invoice-spec.models.preds :as preds]
            [clojure.spec.gen :as gen]))

(s/def ::address (s/keys :req-un [::detail
                                  ::city
                                  ::postal_code
                                  ::country]))

(s/def ::address_from ::address)
(s/def ::address_to ::address)

(s/def ::detail preds/medium-string?)
(s/def ::city string?)
(s/def ::postal_code preds/postal-code?)
(s/def ::country preds/country?)


#_(gen/sample (s/gen ::detail))
