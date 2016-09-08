(ns invoice-spec.models.client
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [invoice-spec.models.preds :as preds]
            [invoice-spec.models.client-names]))

#_(gen/sample (s/gen ::client))

(s/def ::client (s/keys :req-un [:invoice-spec.models.client-names/name]
                        :opt-un [::email ::language ::code
                                 ::address ::city ::postal_code ::country
                                 ::phone ::fax
                                 ::website
                                 ::observations
                                 ::send_options]))

(s/def ::code (s/with-gen string?
                #(gen/fmap (fn [n]
                             (str "code" n))
                           (s/gen (s/int-in 1 1000)))))

(s/def ::email (s/with-gen string?
                 #(gen/fmap (fn [raw]
                              (str "aa" raw "@gmail.com"))
                            (gen/string-alphanumeric))))

(s/def ::country (s/with-gen string?
                   #(s/gen #{"Portugal"})))

(s/def ::postal_code (s/with-gen string?
                       #(gen/fmap (fn [[part1 part2]]
                                    (str part1 "-" part2))
                                  (gen/tuple (s/gen (s/int-in 1000 3000))
                                             (s/gen (s/int-in 100 300))))))
(s/def ::address string?)
(s/def ::city string?)
(s/def ::observations string?)
(s/def ::language #{"en" "pt" "es"})
(s/def ::send_options (s/with-gen nat-int?
                        #(s/gen #{1 2 3})))

(s/def ::website (s/with-gen string?
                   #(gen/fmap (fn [raw]
                                (str "www." raw ".com"))
                              (gen/string-alphanumeric))))

(s/def ::phone preds/phone?)

(s/def ::fax preds/phone?)
