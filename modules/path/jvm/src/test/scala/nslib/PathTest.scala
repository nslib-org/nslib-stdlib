package nslib

import munit.FunSuite
import nslib.path.PathException

class PathTest extends FunSuite {

  // ── Construction & toString ───────────────────────────────────────────────

  test("apply and toString round-trip") {
    assertEquals(Path("/usr/local/bin").toString, "/usr/local/bin")
    assertEquals(Path("relative/path").toString, "relative/path")
    assertEquals(Path("").toString, "")
  }

  // ── / operator ───────────────────────────────────────────────────────────

  test("/ appends a segment") {
    val sep = java.io.File.separator
    assertEquals((Path("/usr") / "local").toString, s"/usr${sep}local")
    assertEquals((Path("/usr") / "local" / "bin").toString, s"/usr${sep}local${sep}bin")
  }

  test("/ with Path argument") {
    val sep = java.io.File.separator
    assertEquals((Path("/a") / Path("b")).toString, s"/a${sep}b")
  }

  test("/ with empty string returns same path") {
    assertEquals((Path("/usr") / "").toString, "/usr")
  }

  // ── name ─────────────────────────────────────────────────────────────────

  test("name: last component") {
    assertEquals(Path("/usr/local/bin").name, "bin")
    assertEquals(Path("/usr/local/file.txt").name, "file.txt")
    assertEquals(Path("file.txt").name, "file.txt")
    assertEquals(Path("/").name, "")
  }

  // ── ext ──────────────────────────────────────────────────────────────────

  test("ext: extension with dot") {
    assertEquals(Path("/tmp/hello.txt").ext, ".txt")
    assertEquals(Path("archive.tar.gz").ext, ".gz")
    assertEquals(Path("no-ext").ext, "")
    assertEquals(Path(".dotfile").ext, "")
  }

  // ── stem ─────────────────────────────────────────────────────────────────

  test("stem: name without extension") {
    assertEquals(Path("/tmp/hello.txt").stem, "hello")
    assertEquals(Path("archive.tar.gz").stem, "archive.tar")
    assertEquals(Path("no-ext").stem, "no-ext")
    assertEquals(Path(".dotfile").stem, ".dotfile")
  }

  // ── parent ───────────────────────────────────────────────────────────────

  test("parent: one level up") {
    assertEquals(Path("/usr/local/bin").parent.toString, "/usr/local")
    assertEquals(Path("/usr/local").parent.toString, "/usr")
  }

  test("parent: at root returns Path(\".\")") {
    assertEquals(Path("/top").parent.toString, ".")
    assertEquals(Path("file.txt").parent.toString, ".")
  }

  // ── parts ────────────────────────────────────────────────────────────────

  test("parts: absolute path") {
    assertEquals(Path("/usr/local/bin").parts, List("/", "usr", "local", "bin"))
  }

  test("parts: relative path") {
    assertEquals(Path("a/b/c").parts, List("a", "b", "c"))
  }

  // ── isAbsolute ───────────────────────────────────────────────────────────

  test("isAbsolute") {
    assert(Path("/usr/local").isAbsolute)
    assert(!Path("relative/path").isAbsolute)
    assert(!Path("").isAbsolute)
  }

  // ── resolve ──────────────────────────────────────────────────────────────

  test("resolve is an alias for /") {
    val sep = java.io.File.separator
    assertEquals(Path("/usr").resolve("local").toString, s"/usr${sep}local")
  }

  // ── FS operations ────────────────────────────────────────────────────────

  test("exists/isFile/isDir on temp file") {
    val tmp = java.io.File.createTempFile("nslib-path-test", ".txt")
    tmp.deleteOnExit()
    val p = Path(tmp.getAbsolutePath)

    assert(p.exists)
    assert(p.isFile)
    assert(!p.isDir)
    assert(p.size >= 0L)
    assert(p.lastModified > 0L)
  }

  test("exists returns false for nonexistent path") {
    assert(!Path("/this/does/not/exist/42").exists)
  }

  test("list on temp dir") {
    val dir = java.io.File.createTempFile("nslib-path-dir", "")
    dir.delete()
    dir.mkdir()
    dir.deleteOnExit()
    val dirP = Path(dir.getAbsolutePath)

    new java.io.File(dir, "a.txt").createNewFile()
    new java.io.File(dir, "b.txt").createNewFile()

    val children = dirP.list.map(_.name).sorted
    assertEquals(children, List("a.txt", "b.txt"))
  }

  test("list throws PathException for non-directory") {
    val tmp = java.io.File.createTempFile("nslib-list-err", ".txt")
    tmp.deleteOnExit()
    intercept[PathException](Path(tmp.getAbsolutePath).list)
  }

  test("absolute resolves relative path") {
    val p = Path(".")
    assert(p.absolute.isAbsolute)
  }

  test("relativeTo") {
    val dir = java.io.File.createTempFile("nslib-rel", "")
    dir.delete(); dir.mkdir(); dir.deleteOnExit()
    val child = new java.io.File(dir, "sub/file.txt")
    child.getParentFile.mkdirs()
    child.createNewFile()

    val base = Path(dir.getAbsolutePath)
    val full = Path(child.getAbsolutePath)
    assertEquals(full.relativeTo(base).toString, "sub" + java.io.File.separator + "file.txt")
  }

  test("relativeTo throws for unrelated path") {
    intercept[PathException] {
      Path("/a/b").relativeTo(Path("/x/y"))
    }
  }

  // ── Path.home / Path.cwd ─────────────────────────────────────────────────

  test("Path.home is non-empty") {
    assert(Path.home.toString.nonEmpty)
  }

  test("Path.cwd is non-empty") {
    assert(Path.cwd.toString.nonEmpty)
  }
}
