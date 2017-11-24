(ns status-im.i18n
  (:require
    [cljs.spec.alpha :as spec]
    [status-im.react-native.js-dependencies :as rn-dependencies]
    [status-im.translations.af :as af]
    [status-im.translations.ar :as ar]
    [status-im.translations.bel :as be]
    [status-im.translations.cs :as cs]
    [status-im.translations.da :as da]
    [status-im.translations.de :as de]
    [status-im.translations.de-ch :as de-ch]
    [status-im.translations.en :as en]
    [status-im.translations.es :as es]
    [status-im.translations.es-ar :as es-ar]
    [status-im.translations.es-mx :as es-mx]
    [status-im.translations.fi :as fi]
    [status-im.translations.fr :as fr]
    [status-im.translations.fr-ch :as fr-ch]
    [status-im.translations.fy :as fy]
    [status-im.translations.he :as he]
    [status-im.translations.hi :as hi]
    [status-im.translations.hu :as hu]
    [status-im.translations.id :as id]
    [status-im.translations.it :as it]
    [status-im.translations.it-ch :as it-ch]
    [status-im.translations.ja :as ja]
    [status-im.translations.ko :as ko]
    [status-im.translations.la :as la]
    [status-im.translations.lt :as lt]
    [status-im.translations.lv :as lv]
    [status-im.translations.ms :as ms]
    [status-im.translations.nb :as nb]
    [status-im.translations.ne :as ne]
    [status-im.translations.nl :as nl]
    [status-im.translations.pl :as pl]
    [status-im.translations.pt-br :as pt-br]
    [status-im.translations.pt-pt :as pt-pt]
    [status-im.translations.ro :as ro]
    [status-im.translations.ru :as ru]
    [status-im.translations.sl :as sl]
    [status-im.translations.sr-rs-cyrl :as sr-rs-cyrl]
    [status-im.translations.sr-rs-latn :as sr-rs-latn]
    [status-im.translations.sv :as sv]
    [status-im.translations.sw :as sw]
    [status-im.translations.th :as th]
    [status-im.translations.tr :as tr]
    [status-im.translations.uk :as uk]
    [status-im.translations.ur :as ur]
    [status-im.translations.vi :as vi]
    [status-im.translations.zh-hans :as zh-hans]
    [status-im.translations.zh-hant :as zh-hant]
    [status-im.translations.zh-wuu :as zh-wuu]
    [status-im.translations.zh-yue :as zh-yue]
    [status-im.utils.js-resources :refer [default-contacts]]
    [taoensso.timbre :as log]
    [clojure.string :as string]
    [clojure.set :as set]))

(set! (.-fallbacks rn-dependencies/i18n) true)
(set! (.-defaultSeparator rn-dependencies/i18n) "/")

;; translations

(def translations-by-locale {:af          af/translations
                             :ar          ar/translations
                             :be          be/translations
                             :cs          cs/translations
                             :da          da/translations
                             :de          de/translations
                             :de-ch       de-ch/translations
                             :en          en/translations
                             :es          es/translations
                             :es-ar       es-ar/translations
                             :es-mx       es-mx/translations
                             :fi          fi/translations
                             :fr          fr/translations
                             :fr-ch       fr-ch/translations
                             :fy          fy/translations
                             :he          he/translations
                             :hi          hi/translations
                             :hu          hu/translations
                             :id          id/translations
                             :it          it/translations
                             :it-ch       it-ch/translations
                             :ja          ja/translations
                             :ko          ko/translations
                             :la          la/translations
                             :lt          lt/translations
                             :lv          lv/translations
                             :ms          ms/translations
                             :nb          nb/translations
                             :ne          ne/translations
                             :nl          nl/translations
                             :pl          pl/translations
                             :pt-br       pt-br/translations
                             :pt-pt       pt-pt/translations
                             :ro          ro/translations
                             :ru          ru/translations
                             :sl          sl/translations
                             :sr          sr-rs-cyrl/translations
                             :sr-RS_#Latn sr-rs-latn/translations
                             :sr-RS_#Cyrl sr-rs-cyrl/translations
                             :sv          sv/translations
                             :sw          sw/translations
                             :th          th/translations
                             :tr          tr/translations
                             :uk          uk/translations
                             :ur          ur/translations
                             :vi          vi/translations
                             :zh          zh-hans/translations
                             :zh-hans     zh-hans/translations
                             :zh-hans-tw  zh-hans/translations
                             :zh-hans-sg  zh-hans/translations
                             :zh-hans-hk  zh-hans/translations
                             :zh-hans-cn  zh-hans/translations
                             :zh-hans-mo  zh-hans/translations
                             :zh-hant     zh-hant/translations
                             :zh-hant-tw  zh-hant/translations
                             :zh-hant-sg  zh-hant/translations
                             :zh-hant-hk  zh-hant/translations
                             :zh-hant-cn  zh-hant/translations
                             :zh-hant-mo  zh-hant/translations
                             :zh-wuu      zh-wuu/translations
                             :zh-yue      zh-yue/translations})

;; english as source of truth
(def labels (set (keys en/translations)))

(spec/def ::label labels)
(spec/def ::labels (spec/coll-of ::label :kind set? :into #{}))

(defn labels-for-all-locales []
  (->> translations-by-locale
       (mapcat #(-> % val keys))
       set))

;; checkpoints

;; Checkpoints specify milestones for locales.
;;
;; With milestones we can ensure that expected supported languages
;; are actually supported, and visualize the translation state for
;; the rest of locales according to these milestones.
;;
;; Checkpoints are defined by indicating the labels that need to be present
;; in a locale to achieve that checkpoint.
;;
;; We need to define the checkpoint that needs to be achieved for
;; a locale to be considered supported. This is why as we develop
;; we add translations, so we need to be defining a new target
;; for supported languages to achieve.
;;
;; Checkpoints are only used in dev and test. In dev when we want to
;; manually check the state of checkpoints for locales, and in test
;; to automatically check supported locales against the target checkpoint.

(spec/def ::checkpoint.id keyword?)
(spec/def ::checkpoint-defs (spec/map-of ::checkpoint.id ::labels))

(def checkpoint-1-labels labels)
;; these could be status versions instead of incremental numbers
(def checkpoints-def (spec/assert ::checkpoint-defs
                                  {::checkpoint-1 checkpoint-1-labels
                                   ::checkpoint-2 (into checkpoint-1-labels #{})}))
(def checkpoints (set (keys checkpoints-def)))

(spec/def ::checkpoint checkpoints)

(def checkpoint-to-consider-locale-supported ::checkpoint-1)

(defn checkpoint->labels [checkpoint]
  (get checkpoints-def checkpoint))

(defn checkpoint-val-to-compare [c]
  (-> c name (string/replace #"^.*\|" "") int))

(defn >checkpoints [& cs]
  (apply > (map checkpoint-val-to-compare cs)))

;; locales

(def locales (set (keys translations-by-locale)))

(spec/def ::locale locales)
(spec/def ::locales (spec/coll-of ::locale :kind set? :into #{}))

(def supported-locales (spec/assert ::locales #{:en :es}))

(spec/def ::supported-locale supported-locales)
(spec/def ::supported-locales (spec/coll-of ::supported-locale :kind set? :into #{}))

(defn locale->labels [locale]
  (-> translations-by-locale (get locale) keys set))

(defn locale->checkpoint [locale]
  (let [locale-labels (locale->labels locale)
        checkpoint    (->> checkpoints-def
                           (filter (fn [[checkpoint checkpoint-labels]]
                                     (set/subset? locale-labels checkpoint-labels)))
                           ffirst)]
    checkpoint))

(defn locales-with-checkpoint []
  (->> locales
       (map (fn [locale]
              [locale (locale->checkpoint locale)]))
       (into {})))

(defn locale-is-supported-based-on-translations? [locale]
  (let [c (locale->checkpoint locale)]
    (and c (or (= c checkpoint-to-consider-locale-supported)
               (>checkpoints checkpoint-to-consider-locale-supported c)))))

(defn actual-supported-locales []
  (->> locales
       (filter locale-is-supported-based-on-translations?)
       set))

(defn locales-with-full-support []
  (->> locales
       (filter (fn [locale]
                 (set/subset? labels (locale->labels locale))))
       set))

(defn supported-locales-that-are-not-considered-supported []
  (set/difference (actual-supported-locales) supported-locales))


(set! (.-translations rn-dependencies/i18n)
      (clj->js translations-by-locale))

;:zh, :zh-hans-xx, :zh-hant-xx have been added until this bug will be fixed https://github.com/fnando/i18n-js/issues/460

(def delimeters
  "This function is a hack: mobile Safari doesn't support toLocaleString(), so we need to pass
  this map to WKWebView to make number formatting work."
  (let [n          (.toLocaleString (js/Number 1000.1))
        delimiter? (= (count n) 7)]
    (if delimiter?
      {:delimiter (subs n 1 2)
       :separator (subs n 5 6)}
      {:delimiter ""
       :separator (subs n 4 5)})))

(defn label-number [number]
  (when number
    (let [{:keys [delimiter separator]} delimeters]
      (.toNumber rn-dependencies/i18n
                 (string/replace number #"," ".")
                 (clj->js {:precision                 10
                           :strip_insignificant_zeros true
                           :delimiter                 delimiter
                           :separator                 separator})))))

(def default-option-value "<no value>")

(defn label-options [options]
  ;; i18n ignores nil value, leading to misleading messages
  (into {} (for [[k v] options] [k (or v default-option-value)])))

(defn label
  ([path] (label path {}))
  ([path options]
   (if (exists? rn-dependencies/i18n.t)
     (let [options (update options :amount label-number)]
       (.t rn-dependencies/i18n (name path) (clj->js (label-options options))))
     (name path))))

(defn label-pluralize [count path & options]
  (if (exists? rn-dependencies/i18n.t)
    (.p rn-dependencies/i18n count (name path) (clj->js options))
    (name path)))

(defn message-status-label [status]
  (->> status
       (name)
       (str "t/status-")
       (keyword)
       (label)))

(def locale
  (.-locale rn-dependencies/i18n))

(defn get-contact-translated [contact-id key fallback]
  (let [translation #(get-in default-contacts [(keyword contact-id) key (keyword %)])]
    (or (translation locale)
        (translation (subs locale 0 2))
        fallback)))
