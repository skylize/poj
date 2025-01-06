(ns skylize.poj.algebra
  (:require [skylize.poj.macro :refer [defalias defcurry]]
            :reload-all))

(comment (set! *print-meta* true)
         )

;; ;;

(defn winner
  "Apply :win using provided value without moving pos."
  [x]
  (fn [t] (assoc t :win x)))

(defn loser
  "Apply loss using provided message without moving pos."
  [e]
  (fn [t] (assoc t :loss e)))

(defcurry bind
  "Map a function that returns a parser, without stacking parser contexts."
  [fun parse state]
  (if (:loss state) state
      (let [next-state ((parse) state)]
        (if (:loss next-state)
          next-state
          (let [next-win (fun (:win next-state))]
            (next-win next-state))))))

(defalias >>= bind)

(defcurry fmap
  "Map function over a loss, returning a winning state."
  [f parse state]
  (if (:loss state) state
      (let [next-state (parse state)]
        (if (:loss next-state) next-state
            (assoc next-state
                   :win (f (:win next-state)))))))

(defcurry f-atlas
  "Map function over the full parsing state on `:win` condition."
  [f parse state]
  (let [next-state (parse state)]
    (if (:loss next-state) next-state
        (f next-state))))

(defcurry join
  "Pull result out of a parser context."
  [parse state]
  (let [next-state (parse state)]
    (if (:loss next-state) next-state
        (:win next-state))))

(defcurry ap
  "Apply function inside a parser context to a winning result."
  [fp xp state]
  (if (:loss state) state
      (let [next-state (xp state)
            f (:win (fp next-state))]
        (if (:loss next-state)
          next-state
          (assoc next-state :win (f (:win next-state)))))))

(defcurry lost-atlas
  "Map function `f` over the full parsing state on losing condition."
  [f parse state]
  (let [next-state (parse state)]
    (if (:loss next-state) (f next-state)
        next-state)))

 (defcurry lost-map
   "Map function `f` over a parse error."
   [f parse state]
   (let [next-state (parse state)]
     (if (:loss next-state)
       (assoc next-state
              :loss (f next-state))
       next-state)))

(defcurry recover
  "Map function over a loss, returning a winning state."
  [f parse state]
  (let [next-state (parse state)]
    (if (:loss next-state)
      (assoc next-state
             :win (f next-state))
      next-state)))

(defcurry tap
  "Run a side effect with a winning parser state, and return the state unaltered."
  [f parse state]
  (let [next-state (parse state)]
    (if (:loss next-state) next-state
        (do (f (:win next-state))
            next-state))))

(defcurry hard-tap
  "Debugging tool, run a side effect function with a full parsing state and return it unaltered."
  [f parse state]
  (let [next-state (parse state)]
    (f next-state)
    next-state))
