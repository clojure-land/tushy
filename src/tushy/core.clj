(ns tushy.core
  (:require [clojure.zip :as zip]
            [clojure.xml :as xml]
            [clojure.contrib.duck-streams :as streams]
            [net.cgrand.enlive-html :as html]
            [clj-time.format])
  (:use clojure.contrib.zip-filter.xml))

(defn zip-str [s]
  (zip/xml-zip (xml/parse (java.io.ByteArrayInputStream. (.getBytes s)))))

(def xml-planet-info (zip-str (streams/slurp* "./config.xml.sample")))

(def planet-map (map #(hash-map :author %1 :feed %2)
                     (xml-> xml-planet-info :blog :author text)
                     (xml-> xml-planet-info :blog :feed text)))

(defn fetch-feed
  "Fetch contents of blog feed"
  [feed-address]
  (html/html-resource (java.net.URL. feed-address)))

(defn feed-type
  "Return the type of feed we are dealing with"
  [feed]
  (cond
   (not (empty? (html/select feed [:rss]))) :rss
   (not (empty? (html/select feed [:feed]))) :atom
   :else :false))

(def feed-tag-definitions {:rss {:title :title
                                 :pubDate :pubDate
                                 :post :description
                                 :item :item
                                 }
                           :atom {:title :title
                                  :pubDate :published
                                  :post :content
                                  :item :entry
                                  }})
(defn feed-map
  "Create a map for the contents of each feed"
  [feed-address feed-author]
  (let [feed (fetch-feed feed-address)
        ftype (feed-type feed)]
    (map #(hash-map :title %1 :pubDate %2 :author feed-author) ;Removed post for testing
         (html/select feed [(:item (feed-tag-definitions ftype))
                            (:title (feed-tag-definitions ftype))
                            text])
         (html/select feed [(:item (feed-tag-definitions ftype))
                            (:pubDate (feed-tag-definitions ftype))
                            text])
         ;; (html/select feed [(:item (feed-tag-definitions ftype))
         ;;                    (:post (feed-tag-definitions ftype))
         ;;                    text])
         )))

(defn consolidated-feed-map
  "Create a single map containing the posts from all the feeds mentioned in the planet-map"
  [m]
  (cond
   (empty? m) (quote ())
   :else (concat (feed-map (:feed (first m)) (:author (first m)))
               (consolidated-feed-map (rest m)))))
