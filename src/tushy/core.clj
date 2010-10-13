(ns tushy.core
  (:require [net.cgrand.enlive-html :as html])
  (:use [clojure.contrib.zip-filter.xml]
        [clj-time.format
         :only [formatters formatter parse]]
        [clj-time.coerce
         :only [to-long]]
        [tushy.defaults]))

(def date-formatters [(formatters :date-time)
                 (formatters :date-time-no-ms)
                 (formatter "EEE, d MMM yyyy HH:mm:ss Z")])

(defn return-date-object
  "Take a date-string and return a date-object by running the list of formatters on the string"
  [s]
  (first (filter (comp not nil?)
                 (for [f date-formatters
                       :let [date (try
                                    (parse f s)
                                    (catch Exception _ nil))]]
                   date))))

(defn planet-map
  "Take an XML file and return a sequence of maps containing the author of each blog and the feed of his blog"
  [xml-file-name]
  (let [xml-planet-info (zip-str xml-file-name)]
    (map #(zipmap [:author :feed :addr :name]
                  [%1 %2 %3 %4])
         (xml-> xml-planet-info :blog :author text)
         (xml-> xml-planet-info :blog :feed text)
         (xml-> xml-planet-info :blog :addr text)
         (xml-> xml-planet-info :blog :name text))))

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
                                 :link :link
                                 }
                           :atom {:title :title
                                  :pubDate :published
                                  :post :content
                                  :item :entry
                                  :link :link  ;Atom is #FAIL
                                  }})
(defn feed-map
  "Create a map of posts for a planet entry"
  [planet-entry]
  (let [feed (fetch-feed (:feed planet-entry))
        ftype (feed-type feed)
        item-feed (html/select feed [(:item (feed-tag-definitions ftype))])]
    (map #(zipmap [:title :pubDate :planet-entry] [%1 %2 planet-entry]) ;Removed post for testing
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
   :else (concat (feed-map (first a-planet-map))
                 (consolidated-feed-map (rest a-planet-map)))))

(defn dated-feed-map
  "Convert the date strings in the consolidated feed map to date objects"
  [a-consolidated-map]
  (for [m a-consolidated-map]
    (assoc m :pubDateObj (return-date-object (:pubDate m)))))

(defn sorted-feed-map
  "Sort a dated feed map on it's date object"
  [a-dated-map]
  (sort-by #(to-long (get % :pubDateObj))
           >
           a-dated-map))
