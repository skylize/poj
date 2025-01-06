(ns skylize.poj.util-test
  (:require [clojure.test :refer [deftest is run-tests]]
            [skylize.poj.util :as util]))

(deftest substr-test
  (is (= (util/substr "foo" 1) (subs "foo" 1))
      "behaves like subs for contained indexes")
  (is (= (util/substr "foo" -4 20) (subs "foo" 0 3))
      "clamps indexes to string length"))

(deftest re-pre-anchor-test
  (is (= (str #"^foo") (str (util/re-pre-anchor "foo")))
      "returns regex with ^ at the front")
  (is (= (str #"^foo") (str (util/re-pre-anchor #"^foo")))
      "does not double up when ^ already there"))

(deftest re-find-first-test
  (is (= "foo" (util/re-find-first #"foo" "foobar"))
      "standard response to 1 value")
  (is (= "foo" (util/re-find-first #"foo" "foobar foo"))
      "returns single unwrapped result"))

(comment (run-tests))
