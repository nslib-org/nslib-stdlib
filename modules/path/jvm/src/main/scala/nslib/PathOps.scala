package nslib

import nslib.path.PathException

/** File-system operations for [[Path]] on the JVM.
  *
  * These are available automatically when you `import nslib._`.
  */
implicit class PathOps(private val p: Path) extends AnyVal {

  // ── Queries ─────────────────────────────────────────────────────────────

  /** True if the path exists (file or directory). */
  def exists: Boolean = file.exists()

  /** True if the path is a regular file. */
  def isFile: Boolean = file.isFile

  /** True if the path is a directory. */
  def isDir: Boolean = file.isDirectory

  /** File size in bytes. Returns 0 for directories. */
  def size: Long = file.length()

  /** Last-modified time in milliseconds since the Unix epoch. */
  def lastModified: Long = file.lastModified()

  // ── Listing ─────────────────────────────────────────────────────────────

  /** List the immediate children of a directory. */
  def list: List[Path] = {
    if (!file.isDirectory)
      throw PathException(s"Not a directory: ${p.value}")
    Option(file.list())
      .getOrElse(throw PathException(s"Cannot list directory: ${p.value}"))
      .sorted
      .map(name => p / name)
      .toList
  }

  // ── Navigation ──────────────────────────────────────────────────────────

  /** Canonical absolute path. */
  def absolute: Path = Path(file.getCanonicalPath)

  /** Relative path from `base` to this path. */
  def relativeTo(base: Path): Path = {
    val from = new java.io.File(base.value).getCanonicalPath
    val to   = file.getCanonicalPath
    if (!to.startsWith(from))
      throw PathException(s"'$to' is not under '$from'")
    val rel = to.substring(from.length).stripPrefix(java.io.File.separator)
    Path(rel)
  }

  // ── Internal ────────────────────────────────────────────────────────────

  private def file: java.io.File = new java.io.File(p.value)
}
