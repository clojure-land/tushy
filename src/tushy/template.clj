(ns tushy.template
  (:require [net.cgrand.enlive-html :as html])
  (:use [tushy.defaults]
        [tushy.core]
        [clojure.contrib.zip-filter.xml]))

(defn generate-site-info
  "parse the XML configuration file and generate a map with the appropriate values"
  []
  (let [xml-planet-info (zip-str *xml-file-name*)]
    ;; We return the first element of the map because we know that
    ;; there is only one <site></site> element in the config file
    (first (map #(zipmap [:site_name :site_title :site_copyright]
                         [%1 %2 %3])
                (xml-> xml-planet-info :site :name text)
                (xml-> xml-planet-info :site :title text)
                (xml-> xml-planet-info :site :site_copyright text)))))

(defn generate-subscriptions-info
  "Parse the configuration file and generate a map of blogs we are subscribed to"
  []
  (let [xml-planet-info (zip-str *xml-file-name*)]
    (map #(zipmap [:text :href]
                  [%1 %2])
         (xml-> xml-planet-info :blog :name text)
         (xml-> xml-planet-info :blog :addr text))))

(def *subscription-link-sel* [[:.links (html/nth-of-type 1)] :> html/first-child])

(html/defsnippet link-model *template-file-name* *subscription-link-sel*
  [{href :href text :text}]
  [:a] (html/do->
        (html/content text)
        (html/set-attr :href href)))

(def *sidebar-section-sel* {[:.title] [[:.content (html/nth-of-type 1)]]})

(html/defsnippet sidebar-section-model *template-file-name* *sidebar-section-sel*
  [{title :title data :data} model]
  [:.title] (html/content title)
  [:.content] (html/content (map model data)))

(def *aside-sel* [:.aside])

(html/defsnippet aside-section-model *template-file-name* *aside-sel*
  [post-entry]
  [:.title :a] (html/do->
                (html/content (:author (:planet-entry post-entry)))
                (html/set-attr :href (:addr (:planet-entry post-entry)))
                (html/set-attr :title (:name (:planet-entry post-entry))))
  [:.date] (html/content (:pubDate post-entry)))

(def *article-sel* [:.article])

(html/defsnippet article-section-model *template-file-name* *article-sel*
  [post-entry]
  [:.title :a] (html/do->
                (html/content (:title post-entry))
                ;; (html/set-attr :href (get post-entry :link "nolink"))
                )
  [:.post-content] (html/html-content (:post post-entry))
  ;; [:.meta] [:a] (html/set-attr :href (get post-entry :link "nolink"))
  )

(def *post-section-sel* [[:.entry (html/nth-of-type 1)]])

(html/defsnippet post-section-model *template-file-name* *post-section-sel*
  [post-entry aside-model article-model]
  [:.aside] (html/content (aside-model post-entry))
  [:.article] (html/content (article-model post-entry)))

(defn return-model-data
  "Return a :title :data map which we will use with our models"
  [title data]
  {:title title :data data})

(html/deftemplate index *template-file-name*
  [site-data subscription-data post-data]
  [:title] (html/content (get site-data :site_name "No site name"))
  [:#header :#title] (html/content (get site-data :site_title "No title given"))
  [:.sidebar-list#Subscriptions] (html/content (sidebar-section-model (return-model-data "Subscriptions" subscription-data) link-model))
  [:.posts] (html/content (map #(post-section-model % aside-section-model article-section-model) post-data))
  [:footer#footer :p] (html/html-content (get site-data :site_copyright "No site description")))
