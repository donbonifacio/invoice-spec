(ns invoice-spec.models.client
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [invoice-spec.models.preds :as preds]
            [invoice-spec.models.client-names]))

#_(gen/sample (s/gen ::client))

(s/def ::client (s/keys :req-un [:invoice-spec.models.client-names/name]
                        :opt-un [::email ::language ::code]))

(s/def ::code (s/with-gen string? #(s/gen (->> (range 10) (map (fn [n] (str "Client " n))) (set)))))
(s/def ::email string?)
(s/def ::language #{"en" "pt" "es"})

