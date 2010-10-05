(ns tushy.core
  (:require [clojure.zip :as zip]
            [clojure.xml :as xml]
            [clojure.contrib.duck-streams :as streams]
            [net.cgrand.enlive-html :as html]
            [clj-time.core]
            [clj-time.format]
            [clj-time.coerce])
  (:use [clojure.contrib.zip-filter.xml]))

(def formatters [(clj-time.format/formatters :date-time)
                 (clj-time.format/formatters :date-time-no-ms)
                 (clj-time.format/formatter "EEE, d MMM yyyy HH:mm:ss Z")])

(defn return-date-object
  "Take a date-string and return a date-object by running the list of formatters on the string"
  [s]
  (first (filter (comp not nil?)
                 (for [f formatters
                       :let [date (try
                                    (clj-time.format/parse f s)
                                    (catch Exception _ nil))]]
                   date))))

(defn zip-str
  "Parse an xml file"
  [s]
  (zip/xml-zip (xml/parse (java.io.ByteArrayInputStream. (.getBytes s)))))

(defn planet-map
  "Take an XML file and return a sequence of maps containing the author of each blog and the feed of his blog"
  [xml-file-name]
  (let [xml-planet-info (zip-str (streams/slurp* xml-file-name))]
    (map #(zipmap [:author :feed] [%1 %2])
         (xml-> xml-planet-info :blog :author text)
         (xml-> xml-planet-info :blog :feed text))))

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
        ftype (feed-type feed)
        item-feed (html/select feed [(:item (feed-tag-definitions ftype))])]
    (map #(zipmap [:title :pubDate :author] [%1 %2 feed-author]) ;Removed post for testing
         (html/select item-feed [(:title (feed-tag-definitions ftype))
                                 text])
         (html/select item-feed [(:pubDate (feed-tag-definitions ftype))
                                 text])
         ;; (html/select item-feed [(:post (feed-tag-definitions ftype))
         ;;                         text])
         )))

(defn consolidated-feed-map
  "Create a single map containing the posts from all the feeds mentioned in the planet-map"
  [a-planet-map]
  (cond
   (empty? a-planet-map) (quote ())
   :else (concat (feed-map (:feed (first a-planet-map))
                           (:author (first a-planet-map)))
                 (consolidated-feed-map (rest a-planet-map)))))

