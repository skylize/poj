(ns skylize.dev)

;; ;;


#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn parallel
  "Parse 2 sequences of parsers `a` and `b`, applying a transformation function `x-state -> y-state -> [x-state y-state a-new-state]` to the pair of updated states at each step. (Note that the 3rd new result state has no memory between steps. `x-state` and `y-state` separate paths, as defined by their parsers.)"
  [fun]
  (fn [xs] (fn [ys]
             (fn [state]
               (loop [[x & xs] xs xstate state
                      [y & ys] ys ystate state]
                 (let [next-xstate (x xstate)
                       next-ystate (y ystate)
                       zstate (fun next-xstate next-ystate)]
                   (cond (:loss zstate) state
                         (:win zstate) zstate
                         :else (recur xs next-xstate
                                      ys next-ystate))))))))
