package nslib

import nslib.io.IOException

/** Simple file and path utilities (Scala Native implementation).
  *
  * Uses java.io which is available in Scala Native 0.5+.
  * API is identical to the JVM implementation.
  */
object IO {

  def readString(path: String): String = readString(path, "UTF-8")

  def readString(path: String, charset: String): String =
    new String(readBytes(path), charset)

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

  def readBytes(path: String): Array[Byte] = {
    val is = openInputStream(path)
    try readAllBytes(is)
    finally is.close()
  }

  def writeString(path: String, content: String): Unit =
    writeString(path, content, "UTF-8")

  def writeString(path: String, content: String, charset: String): Unit =
    writeBytes(path, content.getBytes(charset))

  def appendString(path: String, content: String): Unit = {
    val os = openOutputStream(path, append = true)
    try os.write(content.getBytes("UTF-8"))
    finally os.close()
  }

  def writeLines(path: String, lines: Seq[String]): Unit =
    writeString(path, lines.mkString("\n"))

  def writeBytes(path: String, bytes: Array[Byte]): Unit = {
    val os = openOutputStream(path, append = false)
    try { os.write(bytes); os.flush() }
    finally os.close()
  }

  def exists(path: String): Boolean = new java.io.File(path).exists()

  def isFile(path: String): Boolean = new java.io.File(path).isFile

  def isDirectory(path: String): Boolean = new java.io.File(path).isDirectory

  def size(path: String): Long = new java.io.File(path).length()

  def lastModified(path: String): Long = new java.io.File(path).lastModified()

  def listFiles(path: String): List[String] = {
    val dir = new java.io.File(path)
    if (!dir.isDirectory) throw IOException(s"Not a directory: $path")
    Option(dir.list())
      .getOrElse(throw IOException(s"Cannot list directory: $path"))
      .sorted
      .map(name => path + java.io.File.separator + name)
      .toList
  }

  def delete(path: String): Unit = {
    val f = new java.io.File(path)
    if (f.exists() && !f.delete()) throw IOException(s"Failed to delete: $path")
  }

  def deleteRecursive(path: String): Unit = {
    val f = new java.io.File(path)
    if (f.isDirectory) {
      Option(f.listFiles()).foreach(_.foreach(c => deleteRecursive(c.getAbsolutePath)))
    }
    if (f.exists() && !f.delete()) throw IOException(s"Failed to delete: $path")
  }

  def mkdirs(path: String): Unit = {
    val f = new java.io.File(path)
    if (!f.isDirectory && !f.mkdirs()) throw IOException(s"Failed to create directories: $path")
  }

  def move(from: String, to: String): Unit = {
    val src = new java.io.File(from)
    if (!src.renameTo(new java.io.File(to))) {
      copy(from, to)
      delete(from)
    }
  }

  def copy(from: String, to: String): Unit = writeBytes(to, readBytes(from))

  def tempFile(prefix: String = "nslib", suffix: String = ".tmp"): String =
    java.io.File.createTempFile(prefix, suffix).getAbsolutePath

  def tempDir(prefix: String = "nslib"): String = {
    val f = java.io.File.createTempFile(prefix, "")
    f.delete(); f.mkdirs(); f.getAbsolutePath
  }

  def absolutePath(path: String): String = new java.io.File(path).getCanonicalPath

  def fileName(path: String): String = new java.io.File(path).getName

  def parentDir(path: String): String =
    Option(new java.io.File(path).getParent).getOrElse(".")

  def joinPath(parts: String*): String = parts.mkString(java.io.File.separator)

  private def openInputStream(path: String): java.io.InputStream =
    try new java.io.FileInputStream(path)
    catch {
      case e: java.io.FileNotFoundException => throw IOException(s"File not found: $path", e)
    }

  private def openOutputStream(path: String, append: Boolean): java.io.OutputStream =
    try new java.io.FileOutputStream(path, append)
    catch {
      case e: java.io.FileNotFoundException => throw IOException(s"Cannot write file: $path", e)
    }

  private def readAllBytes(is: java.io.InputStream): Array[Byte] = {
    val buf  = new Array[Byte](8192)
    val baos = new java.io.ByteArrayOutputStream()
    var n    = is.read(buf)
    while (n != -1) { baos.write(buf, 0, n); n = is.read(buf) }
    baos.toByteArray
  }
}
