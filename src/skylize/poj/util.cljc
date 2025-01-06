(ns skylize.poj.util
  (:import java.util.regex.Pattern))

;; Functions

(defn curry
  "Auto-curry the parameters of a function."
  [arity f]
  (fn curry' [& xs]
    (if (< (count xs) arity)
      (apply partial curry' xs)
      (apply f xs))))


;; ;; Strings

(def re-bracket-meta-chars "\\^-&]")
(def re-norm-meta-chars "\\.[]{}()<>*+-=!?^$|")

(defn escape-str
  "Escape any characters in provided sequence `meta-chars`"
  [meta-chars s]
  (let [meta? (fn [c]
                (boolean (seq (filter (partial = c) meta-chars))))
        escape-char (fn [c] (if (meta? c) (str "\\" c) c))]
    (apply str (map escape-char (str s)))))

(defn substr
  "Wrapper around `clojure/subs` to clamp start and end values to string length."
  ([s start] (subs s (min (count s) start)))
  ([s start end] (subs s
                       (max 0 (min (count s) start))
                       (max 0 (max start (min (count s) end))))))

(defn shift-char
  "Split string into pair of first char and rest of string."
  [s]
  [(substr s 0 1) (substr s 1)])

;; ;; Regex

(defn re-pre-anchor
  "Prepend a start-of-line anchor `^` if not already there. Coerces `pattern` to regular expression."  [pattern]
  (let [patt-str (str pattern)]
    (if (= \^ (first patt-str))
      (if (= Pattern (type pattern))
        pattern
        (re-pattern patt-str))
      (re-pattern (str \^ patt-str)))))

(defn re-find-first
  "Return first match of expression. Coerces `pattern` to regular expression."
  [pattern s]
  (let [patt (if (= Pattern (type pattern))
               pattern
               (re-pattern (str pattern)))
        match (re-find patt s)]
    (if (vector? match) (first match) match)))

(defn re-start
  "Return first match of expression that is anchored to start of line. Coerces `pattern` to regular expression."
  [pattern string]
  (re-find-first (re-pre-anchor pattern) string))

;; ;; Sequences

(defn append
  "Append value to end of a sequence."
  [m x]
  `(~@m ~x))

;;
