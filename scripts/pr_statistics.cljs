#!/bin/sh
"exec" "lumo" "-D" "org.clojure/tools.cli:0.3.5" "$0" "$@"

(ns status-im.script.pr-statistics
  (:require-macros [cljs.core.async.macros :as a])
  (:require [cljs.nodejs :as node]
            [cljs.core.async :as a]))

(node/enable-util-print!)
(.on js/process "uncaughtException" #(js/console.error %))

(def github-owner "status-im")
(def github-repo "status-react")

(defonce https (node/require "https"))

(defn get-pull-requests []
  (let [url     (str "https://api.github.com"
                     "/repos/" github-owner "/" github-repo "/pulls")
        options (clj->js {:hostname "api.github.com"
                          :path     (str "/repos/" github-owner "/" github-repo "/pulls")
                          :headers  {:User-Agent "Status-im"}
                          })]
    (-> https
        (.get options
              (fn [res]
                (let [body (atom "")]
                  (-> res
                      (.on "data" #(swap! body str (.toString %)))
                      (.on "end" #(-> @body
                                      js/JSON.parse
                                      (js->clj :keywordize-keys true)
                                      println)))))))))

;(get-pull-requests)

(a/go
 (a/timeout 1000)
 (println "lala"))
