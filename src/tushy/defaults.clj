(ns tushy.defaults
  (:require [clojure.zip :as zip]
            [clojure.xml :as xml]
            [clojure.contrib.duck-streams :as streams]))

(def *xml-file-name* "/home/vedang/Work/tushy/config.xml.sample")

(def *final-file-name* "/home/vedang/Work/tushy/src/tushy/html_templates/final.html")

(defn zip-str
  "Parse an xml file"
  [f]
  (zip/xml-zip (xml/parse (java.io.ByteArrayInputStream. (.getBytes (streams/slurp* f))))))

