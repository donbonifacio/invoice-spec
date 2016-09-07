(ns invoice-spec.models.client-names
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]))

(def first-names
  #{"Pedro"
    "Marcos"
    "Greg"
    "Bruna"
    "Mariana"
    "Daniela"
    "Hugo"
    "Joana"
    "Rui"
    "Madalena"
    "Margarida"
    "Cláudia"
    "Zé"
    "Sara"
    "Ana"
    "Pawel"
    "Fernando"})

(def last-names
  #{"Abasto"
    "Morgado"
    "Nunes"
    "França"
    "Stoos"
    "Krysiak"
    "Abrantes"
    "gueda"
    "Alcântara"
    "Ataíde"
    "Balsemão"
    "Barata"
    "Batista"
    "Belchiorinho"
    "Belém"
    "Brás"
    "Cabral"
    "Cabeça de Vaca"
    "Câmara"
    "Caminha"
    "Carvalhais"
    "Castelo Branco"
    "Coutinho"
    "Damasceno"
    "Domingues"
    "Escobar"
    "Espírito Santo"
    "Goulart"
    "Guimarães"
    "Hipólito"
    "Igrejas"
    "Jordão"
    "Keil do Amaral"
    "Lameirinhas"
    "Madruga"
    "Nazário"
    "Padilha"
    "Peseiro"
    "Vilaverde"
    "Lamúria"
    "Santos"
    "dos Santos"
    "Ferreira"
    "Pereira"
    "Gonçalves"
    "Amores"
    "Martinho"
    "Barros"
    "Alves"
    "Malaca"
    "արգսյան"
    "Æyev"
    "Hüseynov"
    "Süleymanov"
    "ÐозловÑкий"
    "Dubois"})

(s/def ::first-name (s/with-gen string?
                   #(s/gen firsk-names)))

(s/def ::last-name (s/with-gen string?
                   #(s/gen last-names)))

(s/def ::name (s/with-gen string?
                #(gen/fmap
                  (fn [[first-name middle-name last-name]]
                    (str first-name " " middle-name " " last-name))
                  (gen/tuple (s/gen ::first-name)
                             (s/gen ::last-name)
                             (s/gen ::last-name)))))

(gen/sample (s/gen ::name))

