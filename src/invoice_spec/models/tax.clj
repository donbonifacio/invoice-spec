(ns invoice-spec.models.tax
  (:require [clojure.spec :as s]
            [invoice-spec.models.preds :as preds]
            [clojure.spec.gen :as gen]))

(s/def ::tax (s/keys :req-un [::name ::id ::value]))

(s/def ::name (s/with-gen string?
                #(s/gen #{"IVA"})))
(s/def ::id nat-int?)
(s/def ::value (s/with-gen preds/nat-number?
                 #(s/gen (s/int-in 0 25))))
