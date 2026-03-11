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
  *   - [[Json]] — parse, build, serialise Json
  *   - [[IO]] — read and write files
  *   - [[Http]] — make Http requests
  */
object All
