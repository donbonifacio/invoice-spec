(ns invoice-spec.models.client
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]))

#_(gen/sample (s/gen ::client))

(s/def ::client (s/keys :req-un [::name]
                        :opt-un [::email ::language ::code]))

(s/def ::name (s/with-gen string? #(s/gen #{"Pedro" "António" "Zé"})))
(s/def ::code (s/with-gen string? #(s/gen (->> (range 10) (map (fn [n] (str "Client " n))) (set)))))
(s/def ::email string?)
(s/def ::language #{"en" "pt" "es"})

