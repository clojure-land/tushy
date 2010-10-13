(ns tushy.defaults
  (:require [clojure.zip :as zip]
            [clojure.xml :as xml]
            [clojure.contrib.duck-streams :as streams])
  (:use [clojure.contrib.zip-filter.xml]))

(def *xml-file-name* "/home/vedang/Work/tushy/config.xml.sample")

(defn zip-str
  "Parse an xml file"
  [f]
  (zip/xml-zip (xml/parse (java.io.ByteArrayInputStream. (.getBytes (streams/slurp* f))))))

(def *planet-config-info* (zip-str *xml-file-name*))

(def *template-file-name* (first (xml-> *planet-config-info* :site :html_template text)))
(def *final-file-name* (first (xml-> *planet-config-info* :site :output_file text)))
(def *no-of-items* (first (xml-> *planet-config-info* :site :items_per_page text)))
