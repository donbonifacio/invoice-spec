(ns invoice-spec.models.item-names
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]))

(def names
  #{"Batata"
    "Açaí"
    "Abacaxi"
    "Carambola"
    "Guaraná"
    "Jabuticaba"
    "Pêssego"
    "Abóbora"
    "Beringela"
    "Beterraba"
    "Couve-Flor"
    "Alcachofra"
    "Aspargo"
    "Illidan Tempesfúria"
    "Endívia"
    "Brócolis"
    "Cebolinha"
    "Almeirão"})

(s/def ::name (s/with-gen string?
                #(s/gen names)))
