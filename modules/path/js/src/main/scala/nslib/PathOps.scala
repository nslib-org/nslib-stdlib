package nslib

/** File-system operations for [[Path]] — Scala.js stub.
  *
  * Full file-system operations are not yet implemented for Scala.js. To contribute a Node.js
  * implementation, open an issue at https://github.com/nslib-org/nslib-stdlib
  */
implicit class PathOps(private val p: Path) extends AnyVal {

  private def notSupported(op: String): Nothing =
    throw new UnsupportedOperationException(
      s"Path.$op is not yet supported on Scala.js. " +
        "Please use the JVM platform, or contribute a Node.js implementation: " +
        "https://github.com/nslib-org/nslib-stdlib"
    )

  def exists: Boolean    = notSupported("exists")
  def isFile: Boolean    = notSupported("isFile")
  def isDir: Boolean     = notSupported("isDir")
  def size: Long         = notSupported("size")
  def lastModified: Long = notSupported("lastModified")
  def list: List[Path]   = notSupported("list")
  def absolute: Path     = notSupported("absolute")

  def relativeTo(base: Path): Path = notSupported("relativeTo")
}
