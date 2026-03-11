package nslib

/** File I/O utilities — Scala.js stub.
  *
  * Full file-system IO is not yet implemented for Scala.js.
  * The JVM platform provides the complete implementation.
  *
  * To contribute a Node.js implementation, open an issue at
  * https://github.com/nslib-org/nslib-stdlib
  */
object IO {

  private def notSupported(op: String): Nothing =
    throw new UnsupportedOperationException(
      s"IO.$op is not yet supported on Scala.js. " +
        "Please use the JVM platform, or contribute a Node.js implementation: " +
        "https://github.com/nslib-org/nslib-stdlib"
    )

  def readString(path: String): String                         = notSupported("readString")
  def readString(path: String, charset: String): String        = notSupported("readString")
  def readLines(path: String): List[String]                    = notSupported("readLines")
  def readBytes(path: String): Array[Byte]                     = notSupported("readBytes")

  def writeString(path: String, content: String): Unit                    = notSupported("writeString")
  def writeString(path: String, content: String, charset: String): Unit   = notSupported("writeString")
  def appendString(path: String, content: String): Unit                   = notSupported("appendString")
  def writeLines(path: String, lines: Seq[String]): Unit                  = notSupported("writeLines")
  def writeBytes(path: String, bytes: Array[Byte]): Unit                  = notSupported("writeBytes")

  def exists(path: String): Boolean         = notSupported("exists")
  def isFile(path: String): Boolean         = notSupported("isFile")
  def isDirectory(path: String): Boolean    = notSupported("isDirectory")
  def size(path: String): Long              = notSupported("size")
  def lastModified(path: String): Long      = notSupported("lastModified")

  def listFiles(path: String): List[String] = notSupported("listFiles")

  def delete(path: String): Unit            = notSupported("delete")
  def deleteRecursive(path: String): Unit   = notSupported("deleteRecursive")
  def mkdirs(path: String): Unit            = notSupported("mkdirs")
  def move(from: String, to: String): Unit  = notSupported("move")
  def copy(from: String, to: String): Unit  = notSupported("copy")

  def tempFile(prefix: String = "nslib", suffix: String = ".tmp"): String = notSupported("tempFile")
  def tempDir(prefix: String = "nslib"): String                           = notSupported("tempDir")

  def absolutePath(path: String): String    = notSupported("absolutePath")
  def fileName(path: String): String        = notSupported("fileName")
  def parentDir(path: String): String       = notSupported("parentDir")
  def joinPath(parts: String*): String      = notSupported("joinPath")
}
