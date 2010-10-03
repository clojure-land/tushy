(ns tushy.core
  (:require [clojure.zip :as zip]
            [clojure.xml :as xml]
            [clojure.contrib.duck-streams :as streams]
            [net.cgrand.enlive-html :as html])
  (:use clojure.contrib.zip-filter.xml))

(defn zip-str [s]
  (zip/xml-zip (xml/parse (java.io.ByteArrayInputStream. (.getBytes s)))))

(def xml-planet-info (zip-str (streams/slurp* "./config.xml.sample")))

(def feed-map (map #(hash-map :author %1 :feed %2)
                   (xml-> xml-planet-info :blog :author text)
                   (xml-> xml-planet-info :blog :feed text)))

