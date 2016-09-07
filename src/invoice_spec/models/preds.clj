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

