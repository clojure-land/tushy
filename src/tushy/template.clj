(ns tushy.template
  (:require [net.cgrand.enlive-html :as html])
  (:use [tushy.defaults]
        [clojure.contrib.zip-filter.xml]))

(defn generate-ctxt
  "parse the XML configuration file and generate a map with the appropriate values"
  []
  (let [xml-planet-info (zip-str *xml-file-name*)]
    (map #(zipmap [:site :site_copyright]
                  [%1 %2])
         (xml-> xml-planet-info :site :name text)
         (xml-> xml-planet-info :site :site_copyright text))))

(def *link-sel* [[:#subscriptions (html/nth-of-type 1)] :> html/first-child])

(html/defsnippet link-model "tushy/html_templates/index.html" *link-sel*
  [{href :href text :text}]
  [:a] (html/do->
        (html/content text)
        (html/set-attr :href href)))

(html/deftemplate index "tushy/html_templates/index.html"
  [ctxt]
  [:title] (html/content (get ctxt :site "No site name"))
  [:h1#title] (html/content (get ctxt :site "No title given"))
  [:footer#footer :p] (html/content (get ctxt :site_copyright "No site description")))
