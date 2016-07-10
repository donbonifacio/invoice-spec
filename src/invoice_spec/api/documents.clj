(ns invoice-spec.api.documents
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [request-utils.core :as request-utils]
            [clojure.core.async :refer [chan <!! >!! close! go <! timeout]]
            [invoice-spec.api.common :as common]
            [invoice-spec.models.sequence :as sequences]
            [invoice-spec.api.sequences :as seq-api]
            [result.core :as result]
            [environ.core :refer [env]]
            [clojure.data.xml :as xml]))

(def url common/url)
(def load-from-xml common/load-from-xml)
(def load-coll-from-xml common/load-coll-from-xml)

(defn doc-path [document]
  (cond
    (= "InvoiceReceipt" (:type document)) "invoice_receipt"
    (= "SimplifiedInvoice" (:type document)) "simplified_invoice"
    (= "Receipt" (:type document)) "receipt"
    :else "invoice"))

(defn sequence-key [document]
  (let [path (doc-path document)]
    (keyword (str "current_" path "_sequence_id"))))

(defn document-xml [document]
  (xml/element (keyword (doc-path document)) {}
               (xml/element :date {} (:date document))
               (if-let [sequence-number (:sequence_number document)]
                 (xml/element :sequence_number {} sequence-number))
               (if-let [sequence-id (:sequence_id document)]
                 (xml/element :sequence_id {} sequence-id))
               (xml/element :status {} (:status document))
               (xml/element :client {}
                            (xml/element :name {} (get-in document [:client :name]))
                            (if-let [language (get-in document [:client :language])]
                              (xml/element :language {} language))
                            (xml/element :code {} (get-in document [:client :code])))
               (xml/element :items {:type "array"}
                  (map (fn [item]
                         (xml/element :item {}
                           (xml/element :name {} (:name item))
                           (xml/element :unit_price {} (:unit_price item))
                           (xml/element :quantity {} (:quantity item))
                           (xml/element :description {} (:description item))))
                       (:items document)))))

(defn document-xml-str [document]
  (xml/emit-str (document-xml document)))

(defn create [document]
  (go
    (result/on-success [response (<! (request-utils/http-post
                                       {:host (url (str "/" (doc-path document) "s.xml"))
                                        :headers {"Content-type" "application/xml; charset=utf-8"}
                                        :plain-body? true
                                        :body (document-xml-str document)}))]
      (result/success (load-from-xml (:body response))))))

(defn reload-document [{:keys [id] :as document}]
  {:pre [(some? id)]}
  (go
    (result/on-success [response (<! (request-utils/http-get
                                       {:host (url (str "/" (doc-path document) "s/" id ".xml"))
                                        :headers {"Content-type" "application/xml; charset=utf-8"}
                                        :plain-body? true}))]
      (result/success (load-from-xml (:body response))))))

(defn related-documents [{:keys [id] :as document}]
  {:pre [(some? id)]}
  (go
    (result/on-success [response (<! (request-utils/http-get
                                       {:host (url (str "/document/" id "/related_documents.xml"))
                                        :headers {"Content-type" "application/xml; charset=utf-8"}
                                        :plain-body? true}))]
      (result/success {:documents (load-coll-from-xml (:body response))}))))

(defn change-state-body [document data]
  (let [path (doc-path document)]
    (str "<"path">"
           (reduce-kv (fn [all k v]
                     (str all "<"(name k)">"v"</"(name k)">"))
                   ""
                   data)
          "</"path">")))

(defn change-state [{:keys [id] :as document} data]
  {:pre [(some? id)]}
  (go
    (result/enforce-let [response (<! (request-utils/http-put
                                         {:host (url (str "/"(doc-path document)"s/" id "/change-state.xml"))
                                          :headers {"Content-type" "application/xml; charset=utf-8"}
                                          :plain-body? true
                                          :body (change-state-body document data)}))
                         document (<! (reload-document document))]
        document)))

(defn finalize [document]
  (change-state document {:state "finalized"}))

(defn cancel [document & cancel-message]
  (change-state document {:state "canceled"
                          :message (or cancel-message "Canceled")}))

(defn settle [document]
  (change-state document {:state "settled"}))

(defn delete [document]
  (change-state document {:state "deleted"}))

(defn set-random-sequence [document]
  (let[serie (gen/generate (sequences/serie-generator))
        ixseq (<!! (seq-api/create {:serie serie}))
        seq-id (get ixseq (sequence-key document))
        document (assoc document :sequence_id seq-id)]
    document))
