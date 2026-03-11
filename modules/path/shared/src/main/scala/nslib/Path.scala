package nslib

/** Immutable file-system path with Python-style `/` composition.
  *
  * Pure string operations work on all platforms. File-system operations (`exists`, `isFile`,
  * `isDir`, etc.) are provided via [[PathOps]] and require JVM or Native; import `nslib._` to bring
  * them into scope.
  *
  * ==Quick start==
  * {{{
  * import nslib._
  *
  * val p = Path("/usr/local") / "bin" / "python"
  * p.name       // "python"
  * p.stem       // "python"
  * p.ext        // ""
  * p.parent     // Path("/usr/local/bin")
  * p.isAbsolute // true
  *
  * // FS operations (JVM / Native only)
  * val home = Path.home / ".config" / "app.toml"
  * home.exists
  * home.parent.list
  * }}}
  */
final case class Path(private[nslib] val value: String) {

  // ── Composition ─────────────────────────────────────────────────────────

  /** Append a child segment and return a new [[Path]]. */
  def /(child: String): Path =
    if (child.isEmpty) this
    else {
      val sep  = java.io.File.separator
      val base = value.stripSuffix("/").stripSuffix("\\")
      val rest = child.stripPrefix("/").stripPrefix("\\")
      Path(base + sep + rest)
    }

  /** Append another [[Path]]. */
  def /(child: Path): Path = this / child.value

  // ── Components ──────────────────────────────────────────────────────────

  /** The final path component (file or directory name). */
  def name: String = {
    val n = normalized
    val i = n.lastIndexOf('/')
    if (i < 0) n else n.substring(i + 1)
  }

  /** File extension including the leading dot, or `""` if none. */
  def ext: String = {
    val n = name
    val i = n.lastIndexOf('.')
    if (i > 0) n.substring(i) else ""
  }

  /** File name without extension. */
  def stem: String = {
    val n = name
    val i = n.lastIndexOf('.')
    if (i > 0) n.substring(0, i) else n
  }

  /** Parent directory, or `Path(".")` when there is no parent. */
  def parent: Path = {
    val n = normalized
    val i = n.lastIndexOf('/')
    if (i <= 0) Path(".")
    else Path(value.substring(0, i))
  }

  /** All path components.  The root `"/"` is included for absolute paths. */
  def parts: List[String] = {
    val n     = normalized
    val isAbs = n.startsWith("/")
    val segs  = n.split("/").filter(_.nonEmpty).toList
    if (isAbs) "/" :: segs else segs
  }

  /** True if this is an absolute path. */
  def isAbsolute: Boolean = {
    val v = value
    v.startsWith("/") || (v.length >= 2 && v.charAt(1) == ':')
  }

  /** Alias for `/`. */
  def resolve(other: String): Path = this / other

  /** Alias for `/`. */
  def resolve(other: Path): Path = this / other

  override def toString: String = value

  // ── Internal ────────────────────────────────────────────────────────────

  private def normalized: String = value.replace('\\', '/')
}

object Path {

  /** Construct a [[Path]] from a string (no validation performed). */
  def apply(s: String): Path = new Path(s)

  /** User's home directory.
    *
    * Returns `Path("~")` on platforms where the property is unavailable.
    */
  def home: Path = Path(sys.props.getOrElse("user.home", "~"))

  /** Current working directory.
    *
    * Returns `Path(".")` on platforms where the property is unavailable.
    */
  def cwd: Path = Path(sys.props.getOrElse("user.dir", "."))
}
