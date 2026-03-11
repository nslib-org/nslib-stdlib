package nslib

/** nslib stdlib — all modules in one.
  *
  * Add a single dependency and get everything:
  * {{{
  * libraryDependencies += "io.github.nslib-org" %% "stdlib-all" % "<version>"
  * }}}
  *
  * Then import once:
  * {{{
  * import nslib._
  * }}}
  *
  * Available namespaces:
  *   - [[JSON]]    — parse, build, serialise JSON
  *   - [[IO]]      — read and write files
  *   - [[HTTP]]    — make HTTP requests
  */
object All
