# `skylize/poj`

Minimalist combinator library for generic parsing in Clojure. Views a parser as no more than application of a function over a map. Provides some core combinators and pointfree monadic functions to help you out.

## Installation

Add the following to your `deps.edn` file.
```
com.github.skylize/poj {:git/tag "v0.1.0 :git/sha ""}
```

## Usage

A "parser" can be as simple as a function that takes a state and returns a state of the same form. In `poj`, that state looks like

```clojure
{ :source "probably a string"
  :pos 0                       ; positional index in the source
  :win, nil                    ; optional success
  :loss nil }                  ; optional failure
```

A parser is any function that can read from `:source` and return a new state. A `:win` represents a success state, and a `:loss` represents a failure. Most parsing functions attempt parsing the `:source` string from the current position `:pos`, but you're free to do otherwise as needed. If parsing succeeds, store it in the `:win` key of the map. If it fails, store it as a `:loss`.

Since it's just maps and functions, you can easily add or change data as needed (including changing the source if that somehow serves you 🧙). Poj's algebraic library helps with more elegant transformations. For example, you can use `fmap` to apply a function directly to the `:win` value, ignoring the rest of the map structure, and transform a string into an AST node showing context and details about what was parsed.

```clojure
; poj/string is a one of the provided parsers that just matches exact string.
(let [state {:pos 25 :source "Wow! Changing results is pretty easy."}]
  ((fmap (fn [s] {:type  :string
                  :value s})
         (poj/string "pretty"))
   state))
```

You can find some core parsers to simplify your life. Some examples include ... **TODO** ... and the all powerful `poj/regex` to throw any regular expression you can devise at the problem.

Poj offers a collection of basic functorial and monadic tranformations. These functions are more discovered than imposed, relying solely on the existence of map keys to behave correctly. Parsers are still just functions, and results still just maps.

## License

Copyright © 2024 skylize (John Higgins)

EPLv2.0


Distributed under the Eclipse Public License version 2.0. Also available under Secondary License of GNU GPLv3 following conditions set forth in EPLv2.0.