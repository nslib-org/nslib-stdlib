package nslib.examples

import nslib._

/** Run with: sbt "examples/runMain nslib.examples.IOExample" */
object IOExample extends App {

  val dir = IO.tempDir("nslib-io-example")
  println(s"Working in: $dir")

  // ── Write & Read ──────────────────────────────────────────────────────

  val file = IO.joinPath(dir, "hello.txt")
  IO.writeString(file, "Hello, nslib IO!")
  println(IO.readString(file)) // Hello, nslib IO!

  // ── Lines ─────────────────────────────────────────────────────────────

  val linesFile = IO.joinPath(dir, "items.txt")
  IO.writeLines(linesFile, List("apple", "banana", "cherry"))
  val lines = IO.readLines(linesFile)
  println(lines) // List(apple, banana, cherry)

  // ── Append ────────────────────────────────────────────────────────────

  IO.appendString(linesFile, "\ndate")
  println(IO.readLines(linesFile).length) // 4

  // ── Metadata ──────────────────────────────────────────────────────────

  println(IO.exists(file))     // true
  println(IO.isFile(file))     // true
  println(IO.isDirectory(dir)) // true
  println(IO.size(file))       // byte count

  // ── List files ────────────────────────────────────────────────────────

  val children = IO.listFiles(dir)
  children.foreach(p => println(s"  ${IO.fileName(p)}"))

  // ── Subdirectory ──────────────────────────────────────────────────────

  val subdir = IO.joinPath(dir, "sub", "nested")
  IO.mkdirs(subdir)
  IO.writeString(IO.joinPath(subdir, "deep.txt"), "deep content")
  println(IO.readString(IO.joinPath(subdir, "deep.txt")))

  // ── Copy / Move ───────────────────────────────────────────────────────

  val copy = IO.joinPath(dir, "hello-copy.txt")
  IO.copy(file, copy)
  println(IO.readString(copy)) // Hello, nslib IO!

  val moved = IO.joinPath(dir, "hello-moved.txt")
  IO.move(copy, moved)
  println(IO.exists(copy))  // false
  println(IO.exists(moved)) // true

  // ── Cleanup ───────────────────────────────────────────────────────────

  IO.deleteRecursive(dir)
  println(IO.exists(dir)) // false
}
