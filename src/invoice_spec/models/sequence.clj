(ns invoice-spec.models.sequence
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]))

(s/def ::sequence (s/keys :req-un [::id]))

(s/def ::id (s/and integer? pos?))
(s/def ::serie (s/with-gen string?
                           #(gen/fmap (fn [v]
                                        (-> v str (clojure.string/replace #"-" "")))
                                      (gen/uuid))))

(defn serie-generator
  []
  (s/gen ::serie))
