(ns skylize.poj.parser
  (:require [clojure.string :as str]
            [skylize.poj.algebra :as p]
            [skylize.poj.util :as util]
            [skylize.poj.macro :refer [defcurry]]
            :reload-all))

;; shared short var names:
;   p = parser
;   t = state
;   u = new state
;   v = some other state

;; ;;

; todo: rewrite most parsers to use winning? losing?
(defn winning?
  "Satisfies win condition (even if a nil win)."
  [t]
  (boolean (and (contains? t :win) (not (:loss t)))))

(defn losing?
  "Has a non-nil :loss."
  [t]
  (boolean (:loss t)))

(defcurry regex
  "Parse first match of a regular expression."
  [pat t]
  (if (:loss t) t
      ; extract the text under test, so we can ensure it's not empty
      (let [{:keys [source pos]} t
            test (util/substr source pos)]
        ; edge case, fail if testing is empty text
        (if (= 0 (count test))
          (assoc t
                 :loss (str "unexpected end of input at " pos))
          ; anchor regex to start of line before matching
          (let [match (util/re-start pat test)]
            (if match
              ; return state with match
              (assoc t
                     :win match
                     :pos (+ pos (count match)))
              ; ... or with fail
              (assoc t
                     :loss (str "failed to match \"" pat "\" at " pos))))))))


(defcurry string
  "Parse a specific string."
  [s t]
  (if (:loss t) t
      (let [{:keys [source pos]} t
            sub-t (util/substr source pos)]
        (if (str/starts-with? sub-t s)
          (assoc t
                 :win s
                 :pos (+ pos (count s)))
          (assoc t
                 :loss (str "failed to match string \"" s "\""
                            " at pos " (:pos t)))))))

(defcurry not-str
  "Parse any string that is not the given string `s`."
  [s t]
  (if (or (:loss t) (not (seq s))) t
      (let [to-pat
            (fn [[c & cs]]
              ; escape string before making into regex
              (let [c (util/escape-str util/re-bracket-meta-chars c)
                    cs (util/escape-str util/re-norm-meta-chars (apply str cs))]
                ;  ex. pattern `([^f]|f(?!oo))+"` matches until `foo`.
                ;   frame for multichar match is a no-op when `cs` is empty
                (re-pattern (str "([^" c "]|" c "(?!" cs "))+"))))
            target-str (util/substr (:source t) (:pos t))
            match (util/re-start (to-pat s) target-str)]
        (if match
          (assoc t :win match :pos (+ (:pos t) (count match)))
          (assoc t :loss "immediately found prohibited match"
                 " at pos " (:pos t))))))

(defcurry any-of
  "Parser that takes first match from a sequence of parsers."
  [ps t]
  (if (:loss t) t
      (loop [[p & ps] ps]
        (cond p
              (let [u (p t)]
                ; done with first success
                (if (not (:loss u))
                  u
                  (recur ps)))
              ; fail if we run out of parsers to try
              (not p) (assoc t
                             :loss (str "failed to make choice"
                                        " at pos " (:pos t)))))))

(defcurry in-seq
  "If all parsings succeed in sequence, win with a single vector of results."
  [ps t]
  (if (:loss t) t
      (loop [[p & ps] ps, v t, us []]
        (cond p (let [u (p v)
                      us (if (not (:win u)) us
                             (conj us (:win u)))]
                  (if (:loss u)
                    (assoc t :loss (:loss u))
                    (recur ps u us)))
              ; done when no parsers left
              (not p) (assoc v
                             :win us
                             :pos (:pos v))))))

(defcurry many
  "Repeat provided parse until it stops succeeding."
  [p t]
  (if (:loss t) t
      (loop [t t us []]
        (let [u (p t)]
          ; done when we lose
          (cond (:loss u) (:loss (assoc t
                                        :win us
                                        :pos (:pos t)))
                (not (:loss u)) (recur u (conj us (:win u))))))))

(defcurry min-cnt
  "Force failure of parser that returns sequence with too short of length."
  [cnt p t]
  (let [u (p t)]
    (if (>= (count (:win u)) cnt) u
        (assoc t :loss
               (str "Failed to find minimum number of matches"
                    " at pos " (:pos t))))))

(defcurry satisfy
  "Match parse only if it fulfills a predicate."
  [pred p t]
  (if (:loss t) t
      (let [u (p t)]
        (if (or (:loss u)
                (pred (:win u)))
          u
          (assoc u
                 :loss (str "value \"" (:win u) "\"failed to satisfy condition at " (:pos u)))))))

(defcurry sep-by
  "Parse any number of matches `p-x` that are divided by a separator `p-sep`."
  [p-sep p-x]
  ; alternate endlessly over each parser in sequence until one fails
  (let [alternating-parses (in-seq (cycle [p-sep p-x]))
        drop-seps (partial take-nth 2)]
    (p/fmap drop-seps alternating-parses)))

(defcurry before
  "If 2 parses succeed in sequence, win with the first result."
  [p-keep p-drop]
  (p/fmap (fn [[A _]] A) (in-seq [p-keep p-drop])))

(defcurry after
  "If 2 parses succeed in sequence, win with the second result."
  [p-drop p-keep]
  (p/fmap (fn [[_ B]] B) (in-seq [p-drop p-keep])))

(defcurry btwn
  "Parse `middle` if it lies between `p-before` and `p-after`. *Note that* `middle` *parser may clobber* `p-after` *parser if they overlap in match set.*"
  ; todo: More robust version can be built for strings using `not-str` parser.
  [p-before p-after p-keep]
  ((before (after p-before p-keep) p-after)))

(defcurry if-else
  "Win with with first result if it wins. Else win or lose with second parse over same input."
  [p-if p-else t]
  (if (:loss t) t (let [u (p-if t)]
                    (if (:win u) u (p-else t)))))

(defcurry all
  "Run every parser in sequence `ps` starting from the same `:pos`, and win if every parser is `winning?`. Wins with the :win with the highest `count`."
  [ps t]
  (if (:loss t) t
      (let [us (->> ps
                    (map (fn [p] (p t)))
                    (filter winning?))]
        (if (= (count us) (count ps))
          (->> us
               (sort (fn [u] (count (:win u))))
               first)
          (assoc t :loss
                 (str "Failed one more attempts to match all at pos "
                      (:pos)))))))

(defcurry ignore
  "Parse but discard the result."
  [p t]
  (if (:loss t) t
      (let [u (p t)]
        (if (:loss u) u
            (assoc u :win nil)))))

(defn clean
  "Clear nils and empty seqs from first nesting level of a winning sequence."
  [p]
  (p/fmap (partial remove empty?) p))
