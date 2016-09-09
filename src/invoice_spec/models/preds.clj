(ns invoice-spec.models.preds
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]))

(defn nat-number? [n]
  (and (number? n)
       (not (neg? n))))

(def currency? nat-number?)
(def quantity-int? (s/with-gen nat-number?
                     #(s/gen (s/int-in 1 100))))

(def quantity-num? (s/or :nat-int (s/with-gen nat-int?
                                    #(s/gen (s/int-in 1 10000)))
                         :nat-num (s/with-gen nat-number?
                                    #(gen/fmap (fn [lucky]
                                                 (double (/ lucky 100)))
                                               (s/gen (s/int-in 1 10000))))))

(def currency? quantity-num?)

(def phone? (s/with-gen string?
                 #(gen/fmap (fn [n]
                              (str n))
                            (s/gen (s/int-in 100000000 999999999)))))

(def postal-code? (s/with-gen string?
                       #(gen/fmap (fn [[part1 part2]]
                                    (str part1 "-" part2))
                                  (gen/tuple (s/gen (s/int-in 1000 3000))
                                             (s/gen (s/int-in 100 300))))))

(def country? (s/with-gen string?
                   #(s/gen #{"Portugal"})))

(def string-gen (gen/one-of [#_(gen/string)
                             (gen/string-ascii)
                             (gen/fmap clojure.string/upper-case (gen/string-ascii))
                             (gen/string-alphanumeric)
                             (gen/fmap clojure.string/upper-case (gen/string-alphanumeric))]))

(def medium-string? (s/with-gen string?
                      #(gen/fmap (fn [c]
                                   (clojure.string/join " " c))
                                 (gen/vector string-gen 5 10))))

(def large-string? (s/with-gen string?
                      #(gen/fmap (fn [c]
                                   (clojure.string/join " " c))
                                 (gen/vector string-gen 5 20))))
