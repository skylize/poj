(ns skylize.poj.parser-test
  (:require [clojure.test :refer [deftest is run-tests]]
            [skylize.poj.algebra :as p]
            [skylize.poj.parser :as poj]
            :reload-all))

(def digits (poj/regex #"\d+"))
(def letters (poj/regex #"[A-Za-z]+"))

(deftest any-of-test
  (let [parse (p/join (poj/any-of [digits letters]))]
    (is (= "12" (parse {:source "12c5", :pos 0}))
        "`any-of` finds first match and stops")
    (is (= "ef" (parse {:source "ef12c5", :pos 0}))
        "`any-of` finds second match and stops")))

(deftest min-cnt
  (let [state {:pos    1 :source "foo"}
        str-p (poj/string "o")
        seq-p (poj/in-seq [str-p str-p])]

    (is (:win (poj/min-cnt 2 seq-p state))
        "`min-cnt` wins if sequence is long enough")
    (is (:loss (poj/min-cnt 3 seq-p state))
        "`min-cnt` loses if sequence is too short")

    (is (:win (poj/min-cnt 1 str-p state))
        "`min-cnt` wins if string is long enough")
    (is (:loss (poj/min-cnt 2 str-p state))
        "`min-cnt` loses if string is too short")))

(deftest btwn-test
  (let [parse ((poj/btwn (poj/string "[") (poj/string "]")) letters)]
    (is (= "foo"
           (:win (parse {:pos 0 :source "[foo]"})))
        "finds value between, returns only that value")
    (is (not= "foo"
              (:win (parse {:pos 0 :source "[foo1]"})))
        "fails if not directly between")))

(deftest regex-test
  (let [parse (poj/regex "a[bc]")]
    (is (= "ac" (:win (parse {:pos 0 :source "aceg"})))
        "finds a regular expression")))

(comment
  (run-tests))
