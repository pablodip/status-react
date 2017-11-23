(ns status-im.test.i18n
  (:require [cljs.test :refer-macros [deftest is]]
            [status-im.i18n :as i18n]
            [clojure.set :as set]
            [cljs.spec.alpha :as s]))

(deftest label-options
  (is (not (nil? (:key (i18n/label-options {:key nil}))))))

(deftest locales-only-have-existing-tran-ids
  (is (s/valid? ::i18n/trans-ids (i18n/trans-ids-for-all-locales))
      (->> i18n/locales
           (remove #(s/valid? ::i18n/trans-ids (i18n/locale->trans-ids %)))
           (map (fn [l]
                  (str "Extra translation ids in locale " l "\n" (set/difference (i18n/locale->trans-ids l) i18n/trans-ids) "\n\n")))
           (apply str))))

(deftest supported-locales-are-actually-supported
  (is (s/valid? ::i18n/supported-locales (i18n/actual-supported-locales))
      (s/explain-str ::i18n/supported-locales (i18n/actual-supported-locales))))
