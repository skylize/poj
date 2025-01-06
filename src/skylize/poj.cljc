(ns skylize.poj
  (:require [skylize.poj.algebra :as p]
            [skylize.poj.parser :as poj]
            [skylize.poj.util :as util]
            [skylize.poj.macro :refer [defalias]]))

; Utility (for building bottom layer parsers)
(defalias curry util/curry)
(defalias escape-str util/escape-str)
(defalias re-bracket-meta-chars util/re-bracket-meta-chars)
(defalias re-find-first util/re-find-first)
(defalias re-norm-meta-chars util/re-norm-meta-chars)
(defalias substr util/substr)
(defalias re-start util/re-start)

; Algebra
(defalias ap p/ap)
(defalias bind p/bind)
(defalias >>= p/bind)
(defalias f-atlas p/f-atlas)
(defalias fmap p/fmap)
(defalias hard-tap p/hard-tap)
(defalias join p/join)
(defalias loser p/loser)
(defalias lost-atlas p/lost-atlas)
(defalias lost-map p/lost-map)
(defalias recover p/recover)
(defalias tap p/tap)
(defalias winner p/winner)

;; ; Core Parsers
(defalias after poj/after)
(defalias all poj/all)
(defalias any-of poj/any-of)
(defalias before poj/before)
(defalias btwn poj/btwn)
(defalias clean poj/clean)
(defalias if-else poj/if-else)
(defalias in-seq poj/in-seq)
(defalias ignore poj/ignore)
(defalias losing? poj/losing?)
(defalias many poj/many)
(defalias min-cnt poj/min-cnt)
(defalias not-str poj/not-str)
(defalias regex poj/regex)
(defalias satisfy poj/satisfy)
(defalias sep-by poj/sep-by)
(defalias string poj/string)
(defalias winning poj/winning?)