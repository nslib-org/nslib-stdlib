package nslib

import nslib.path.PathException

/** File-system operations for [[Path]] on Scala Native.
  *
  * These are available automatically when you `import nslib._`.
  */
implicit class PathOps(private val p: Path) extends AnyVal {

  def exists: Boolean = file.exists()

  def isFile: Boolean = file.isFile

  def isDir: Boolean = file.isDirectory

  def size: Long = file.length()

  def lastModified: Long = file.lastModified()

  def list: List[Path] = {
    if (!file.isDirectory)
      throw PathException(s"Not a directory: ${p.value}")
    Option(file.list())
      .getOrElse(throw PathException(s"Cannot list directory: ${p.value}"))
      .sorted
      .map(name => p / name)
      .toList
  }

  def absolute: Path = Path(file.getCanonicalPath)

  def relativeTo(base: Path): Path = {
    val from = new java.io.File(base.value).getCanonicalPath
    val to   = file.getCanonicalPath
    if (!to.startsWith(from))
      throw PathException(s"'$to' is not under '$from'")
    val rel = to.substring(from.length).stripPrefix(java.io.File.separator)
    Path(rel)
  }

  private def file: java.io.File = new java.io.File(p.value)
}
