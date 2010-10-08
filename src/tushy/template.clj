(ns tushy.template
  (:require [net.cgrand.enlive-html :as html])
  (:use [tushy.core]
        [tushy.defaults]))

(html/deftemplate index "tushy/html_templates/index.html"
  [ctxt]
  [:title] (html/content (get ctxt :title *default-title*))
  [:h1#title] (html/content (get ctxt :h1title *default-title*))
  [:footer#footer] (html/content (get ctxt :footer *default-site-description*)))
