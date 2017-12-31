#!/usr/bin/env boot

(set-env! :dependencies '[[http-kit "2.2.0"]
                          [cheshire "5.8.0"]
                          [org.clojure/core.async "0.3.465"]
                          [clj-time "0.14.2"]])

(require '[org.httpkit.client :as http-kit])
(require '[cheshire.core :as cheshire])
(require '[clojure.core.async :as async])
(require '[clj-time.core :as clj-time])
(require '[clj-time.format :as clj-time-format])
(require '[clojure.string :as string])

(def github-owner "status-im")
(def github-repo "status-react")

;(defn get-pull-requests-for-page [page]
;  @)

(defn get-pr-count []
  (-> (http-kit/request
       {:url (str "https://api.github.com/search/issues"
                  "?q=type:pr+repo:" github-owner "/" github-repo
                  "&per_page=1")})
      deref
      :body
      (cheshire/parse-string true)
      :total_count))

(defn get-prs-for-page [page]
  (let [out-ch (async/chan)]
    (println (str "REQUESTING PAGE " page))
    (http-kit/request
     {:url (str "https://api.github.com/search/issues"
                "?q=type:pr+repo:" github-owner "/" github-repo
                "&per_page=1&page=" page)}
     (fn [res]
       (let [prs (-> res
                     :body
                     (cheshire/parse-string true)
                     :items)]
         (when (nil? prs)
           (println "NIL!!"))
         (async/put! out-ch prs))))
    out-ch))

(defn get-pull-requests []
  (let [pr-count       (get-pr-count)
        page-count     (-> (/ pr-count 100) Math/ceil int)
        page-count 1
        out-ch         (async/chan)
        pages-in-ch    (async/to-chan (range 1 (inc page-count)))
        page-out-ch    (async/chan)
        page-processor (fn [page c]
                         (async/go
                           (async/put! c (async/<! (get-prs-for-page page)))
                           ; wait a little to not exceed github api rate limit
                           (async/<! (async/timeout 1000))
                           (async/close! c)))]
    (async/pipeline-async
     ; this parallelism can be improved if we can use a higher rate limit
     ; in the github api
     1
     page-out-ch
     page-processor
     pages-in-ch)
    (async/go-loop [all-prs []]
      (let [page-prs (async/<! page-out-ch)]
        (if (nil? page-prs)
          (async/put! out-ch all-prs)
          (let [all-prs (->> (into all-prs page-prs)
                             ; ensure only one by number,
                             ; in case we have taken the same twice in different pages
                             ; because of some state changed while requesting
                             (group-by :number)
                             (map #(-> % val first))
                             (vec))]
            (recur all-prs)))))
    out-ch))

(def gh-date-formatter (clj-time-format/formatter "yyyy-MM-dd HH:mm:ss "))

(defn gh-date->clj-date [gh-date]
  (clj-time-format/parse gh-date-formatter
                         (string/replace gh-date #"(T|Z)" " ")))

(defn pr->open-time [pr]
  #_(clj-time-format/parse
   (clj-time-format/formatter "YYYY-MM-DDTHH:MM:SSZ")
   (:created_at pr))
  ;(string/replace (:created_at pr) #"(T|Z)" " ")
  (:closed_at pr)
  #_(clj-time/interval (gh-date->clj-date (:created_at pr))
                     (gh-date->clj-date (:closed_at pr))))

(defn -main [& args]
  (let [prs (async/<!! (get-pull-requests))]
    (prn "PRS: " (count prs))
    (prn (map pr->open-time prs))
    (prn (keys (first prs)))))
