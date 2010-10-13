(ns tushy.template
  (:require [net.cgrand.enlive-html :as html])
  (:use [tushy.defaults]
        [clojure.contrib.zip-filter.xml]))

(defn generate-site-info
  "parse the XML configuration file and generate a map with the appropriate values"
  []
  (let [xml-planet-info (zip-str *xml-file-name*)]
    (map #(zipmap [:site_name :site_title :site_copyright]
                  [%1 %2 %3])
         (xml-> xml-planet-info :site :name text)
         (xml-> xml-planet-info :site :title text)
         (xml-> xml-planet-info :site :site_copyright text))))

(def *subscription-link-sel* [[:.links (html/nth-of-type 1)] :> html/first-child])

(html/defsnippet link-model "tushy/html_templates/index.html" *subscription-link-sel*
  [{href :href text :text}]
  [:a] (html/do->
        (html/content text)
        (html/set-attr :href href)))

(def *section-sel* {[:.title] [[:.links (html/nth-of-type 1)]]})

(html/defsnippet section-model "tushy/html_templates/index.html" *section-sel*
  [{title :title data :data} model]
  [:.title] (html/content title)
  [:.links] (html/content (map model data)))

(html/deftemplate index "tushy/html_templates/index.html"
  [ctxt section-data]
  [:title] (html/content (get ctxt :site_name "No site name"))
  [:#header :#title] (html/content (get ctxt :site_title "No title given"))
  [:.sidebar-list#Subscriptions] (html/content (section-model section-data link-model))
  [:footer#footer :p] (html/content (get ctxt :site_copyright "No site description")))

