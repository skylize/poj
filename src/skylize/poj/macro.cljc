(ns skylize.poj.macro)

(defmacro defalias
  "Create a new symbol pointing to a function var, copying the `:doc` and `:arglists` metadata from an existing symbol."
  [symbol target]
  `(let [vt# (meta (var ~target))
         doc# (:doc vt#)
         arglists# (:arglists vt#)]
     (def ~symbol ~target)
     (alter-meta! (var ~symbol)
                  (fn [metadata#] (merge metadata# {:doc doc#
                                                    :arglists arglists#})))
     (var ~symbol)))

(defmacro defcurry
  "Define a function with auto-curried parameters. (This builds a multi-arity function instead of using recursion.)"
  {:clj-kondo/lint-as 'clj-kondo.lint-as/def-catch-all
   :attribution "Multi-arity curry found on https://euandre.org/til/2021/04/24/clojure-auto-curry.html and used under Creative Commons license CC BY-SA 4.0, as can be viewed at https://creativecommons.org/licenses/by-sa/4.0/. Altered to accept docstring and metadata. Reversed the list of partials for aesthetics. This macro, including changes, can be taken, used, and altered under the same CC BY-SA 4.0 license, with attribution to github.com/skylize, as an alternative to licensing provided for the project."}
  [name & stuff]
  (let [[mdata args body] (loop [m [] stuff stuff]
                            (let [a (first stuff)
                                  b (rest stuff)]
                              (if (vector? a) [m a b]
                                  (recur (conj m a) b))))
        partials (reverse (map (fn [arity]
                                 `(~(subvec args 0 arity) (partial ~name ~@(take arity args))))
                               (range 1 (count args))))]
    `(defn ~name
       ~@mdata (~args ~@body)
       ~@partials)))
