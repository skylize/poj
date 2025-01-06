(ns skylize.poj.algebra-test
  (:require [clojure.test :refer [deftest is run-tests]]
            [clojure.string :as str]
            [skylize.poj.algebra :as p]
            [skylize.poj.parser :as poj]
            :reload-all))

(deftest bind-test
  (let [state {:pos 1 :source "foo"}]
    (let [f (fn [x] (p/winner (str/capitalize x)))
          x "bar"]
      (is (= ((p/>>= f (p/winner x)) state)
             ((f x) state))
          "left identity: `(= (>>= f (winner x)) (f x))`"))

    (let [m (p/winner "bar")]
      (is (= ((p/>>= p/winner m) state)
             (m state))
          "right identity `(= (>>= winner m) m)`"))

    (let [f     (fn [s] (p/winner (concat s "baz")))
          g     (fn [s] (p/winner (apply str s)))
          m     (p/winner "bar")]
      (is (= ((p/>>= g (p/>>= f m)) state)
             ((p/>>= (fn [x] (p/>>= g (f x))) m) state))
          "associativity: `(=  (>>= g (>>= f m))  (>>= (fn [x] (>>= g (f x))) m)`"))))

(deftest ap-test
  (let [state {:pos  1
               :source "foo"}]
    (let [v (p/winner :bar)]
      (is (= ((p/ap (p/winner identity) v) state) (v state))
          "identity: `(= (ap (winner identity) v) v)`"))

    (let [f name
          x :bar]
      (is (= ((p/ap (p/winner f) (p/winner x)) state) ((p/winner (f x)) state))
          "homomorphism: `(= (ap (winner f) (winner x)) (winner (f x)))`"))

    (let [u (p/winner name)
          y :bar]
      (is (= ((p/ap u (p/winner y)) state) ((p/ap (p/winner (fn [f] (f y))) u) state))
          "interchange: = `(ap u (winner y)) (ap (winner (fn [f] (f y))) u))`"))

    (let [u       (p/winner str/capitalize)
          v       (p/winner name)
          w       (p/winner :bar)
          compose (fn [f] (fn [g] (comp f g)))]
      (is (= ((p/ap (p/ap (p/ap (p/winner compose) u) v) w) state)
             ((p/ap u (p/ap v w)) state))
          "composition: `(= (ap (ap (ap (winner comp) u) v) w) (ap u (ap v w)))`"))))

(deftest fmap-test
    (let [state {:pos 4 :source "foo bar"}]
      (is (= "Bar"
             (:win
              ((p/fmap str/capitalize (poj/string "bar")) state)))
          "basic usaage")

    ; jumping through hoops to prove equivalence b/c practical impl of
    ;  `fmap` adds meaningless `:win nil` when key is missing.
      (is (let [result ((p/fmap identity identity) state)]
            (->> result
                 keys
                 (map (fn [k] (= (k state) (k result))))
                 (filter identity)
                 count
                 (= 3)))
          "identity: `(=  (fmap identity)  identity)")

      (let [g (partial apply str)
            f (partial concat "baz")]
        (is (and (= "bazbar"
                    (:win ((p/fmap (comp g f) (poj/string "bar"))
                           state)))
                 (= "bazbar"
                    (:win ((p/fmap g (p/fmap f (poj/string "bar")))
                           state))))
            "composition: `(=  (fmap (comp g f))  (comp (fmap g) (fmap f)) )`"))

      (let [x "bar"
            f str/capitalize]
        (is (= ((p/fmap f (p/winner x)) state)
               ((p/ap (p/winner f) (p/winner x)) state))
            "equivalence of `fmap` and `ap` applications:
             `(=  (fmap f (winner x))  (ap (winner f) (winner x)))`"))))

(comment
  (run-tests)
  )