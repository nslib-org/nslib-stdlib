package nslib

import nslib.io.IOException

/** Simple file and path utilities.
  *
  * All operations are synchronous.  Errors are thrown as [[nslib.io.IOException]].
  *
  * == Quick start ==
  * {{{
  * import nslib._
  *
  * IO.writeString("/tmp/hello.txt", "Hello, world!")
  * val text = IO.readString("/tmp/hello.txt")   // "Hello, world!"
  *
  * IO.exists("/tmp/hello.txt")                   // true
  * IO.listFiles("/tmp")                          // List(...)
  * IO.delete("/tmp/hello.txt")
  * }}}
  */
object IO {

  // ── Read ──────────────────────────────────────────────────────────────────

  /** Read the entire file as a UTF-8 string. */
  def readString(path: String): String = readString(path, "UTF-8")

  /** Read the entire file using the given charset name (e.g. `"UTF-8"`, `"ISO-8859-1"`). */
  def readString(path: String, charset: String): String =
    new String(readBytes(path), charset)

  /** Read all lines of a UTF-8 text file. */
  def readLines(path: String): List[String] = {
    val reader = new java.io.BufferedReader(
      new java.io.InputStreamReader(new java.io.FileInputStream(path), "UTF-8")
    )
    val buf = scala.collection.mutable.ListBuffer[String]()
    try {
      var line = reader.readLine()
      while (line != null) { buf += line; line = reader.readLine() }
    } finally reader.close()
    buf.toList
  }

  /** Read the entire file as raw bytes. */
  def readBytes(path: String): Array[Byte] = {
    val is = openInputStream(path)
    try readAllBytes(is)
    finally is.close()
  }

  // ── Write ─────────────────────────────────────────────────────────────────

  /** Write a UTF-8 string to a file, creating or overwriting it. */
  def writeString(path: String, content: String): Unit =
    writeString(path, content, "UTF-8")

  /** Write a string to a file using the given charset. */
  def writeString(path: String, content: String, charset: String): Unit =
    writeBytes(path, content.getBytes(charset))

  /** Append a UTF-8 string to a file (creates the file if it does not exist). */
  def appendString(path: String, content: String): Unit = {
    val os = openOutputStream(path, append = true)
    try os.write(content.getBytes("UTF-8"))
    finally os.close()
  }

  /** Write all lines joined by the system line separator. */
  def writeLines(path: String, lines: Seq[String]): Unit =
    writeString(path, lines.mkString(System.lineSeparator()))

  /** Write raw bytes to a file. */
  def writeBytes(path: String, bytes: Array[Byte]): Unit = {
    val os = openOutputStream(path, append = false)
    try { os.write(bytes); os.flush() }
    finally os.close()
  }

  // ── Query ─────────────────────────────────────────────────────────────────

  /** True if the path exists (file or directory). */
  def exists(path: String): Boolean = new java.io.File(path).exists()

  /** True if the path is a regular file. */
  def isFile(path: String): Boolean = new java.io.File(path).isFile

  /** True if the path is a directory. */
  def isDirectory(path: String): Boolean = new java.io.File(path).isDirectory

  /** File size in bytes.  Returns 0 for directories. */
  def size(path: String): Long = new java.io.File(path).length()

  /** Last-modified time in milliseconds since the Unix epoch. */
  def lastModified(path: String): Long = new java.io.File(path).lastModified()

  // ── Listing ───────────────────────────────────────────────────────────────

  /** List the immediate children of a directory.  Returns absolute paths. */
  def listFiles(path: String): List[String] = {
    val dir = new java.io.File(path)
    if (!dir.isDirectory)
      throw IOException(s"Not a directory: $path")
    Option(dir.list())
      .getOrElse(throw IOException(s"Cannot list directory: $path"))
      .sorted
      .map(name => path + java.io.File.separator + name)
      .toList
  }

  // ── Manipulation ──────────────────────────────────────────────────────────

  /** Delete a file.  Does nothing if the path does not exist. */
  def delete(path: String): Unit = {
    val f = new java.io.File(path)
    if (f.exists() && !f.delete())
      throw IOException(s"Failed to delete: $path")
  }

  /** Delete a directory and all of its contents recursively. */
  def deleteRecursive(path: String): Unit = {
    val f = new java.io.File(path)
    if (f.isDirectory) {
      Option(f.listFiles()).foreach(_.foreach(c => deleteRecursive(c.getAbsolutePath)))
    }
    if (f.exists() && !f.delete())
      throw IOException(s"Failed to delete: $path")
  }

  /** Create a directory and all missing parent directories.
    * Does nothing if the directory already exists.
    */
  def mkdirs(path: String): Unit = {
    val f = new java.io.File(path)
    if (!f.isDirectory && !f.mkdirs())
      throw IOException(s"Failed to create directories: $path")
  }

  /** Move (rename) a file or directory. */
  def move(from: String, to: String): Unit = {
    val src = new java.io.File(from)
    val dst = new java.io.File(to)
    if (!src.renameTo(dst)) {
      // Fallback: copy then delete (needed for cross-filesystem moves)
      copy(from, to)
      delete(from)
    }
  }

  /** Copy a file. */
  def copy(from: String, to: String): Unit =
    writeBytes(to, readBytes(from))

  // ── Temporary files ───────────────────────────────────────────────────────

  /** Create a new temporary file and return its path. */
  def tempFile(prefix: String = "nslib", suffix: String = ".tmp"): String =
    java.io.File.createTempFile(prefix, suffix).getAbsolutePath

  /** Create a new temporary directory and return its path. */
  def tempDir(prefix: String = "nslib"): String = {
    val f = java.io.File.createTempFile(prefix, "")
    f.delete()
    f.mkdirs()
    f.getAbsolutePath
  }

  // ── Paths ─────────────────────────────────────────────────────────────────

  /** Absolute canonical path of the given path. */
  def absolutePath(path: String): String =
    new java.io.File(path).getCanonicalPath

  /** The file name (last component) of a path. */
  def fileName(path: String): String = new java.io.File(path).getName

  /** The parent directory path, or `"."` if there is no parent. */
  def parentDir(path: String): String =
    Option(new java.io.File(path).getParent).getOrElse(".")

  /** Join path components together. */
  def joinPath(parts: String*): String =
    parts.mkString(java.io.File.separator)

  // ── Internals ─────────────────────────────────────────────────────────────

  private def openInputStream(path: String): java.io.InputStream =
    try new java.io.FileInputStream(path)
    catch {
      case e: java.io.FileNotFoundException =>
        throw IOException(s"File not found: $path", e)
    }

  private def openOutputStream(path: String, append: Boolean): java.io.OutputStream =
    try new java.io.FileOutputStream(path, append)
    catch {
      case e: java.io.FileNotFoundException =>
        throw IOException(s"Cannot write file: $path", e)
    }

  private def readAllBytes(is: java.io.InputStream): Array[Byte] = {
    val buf  = new Array[Byte](8192)
    val baos = new java.io.ByteArrayOutputStream()
    var n    = is.read(buf)
    while (n != -1) { baos.write(buf, 0, n); n = is.read(buf) }
    baos.toByteArray
  }
}
