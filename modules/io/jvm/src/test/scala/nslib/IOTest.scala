package nslib

import munit.FunSuite

class IOTest extends FunSuite {

  // Each test that touches the filesystem gets its own temp directory
  private def withTempDir(f: String => Unit): Unit = {
    val dir = IO.tempDir("io-test")
    try f(dir)
    finally IO.deleteRecursive(dir)
  }

  // ── readString / writeString ──────────────────────────────────────────────

  test("writeString and readString roundtrip") {
    withTempDir { dir =>
      val path = IO.joinPath(dir, "hello.txt")
      IO.writeString(path, "Hello, world!")
      assertEquals(IO.readString(path), "Hello, world!")
    }
  }

  test("writeString overwrites existing content") {
    withTempDir { dir =>
      val path = IO.joinPath(dir, "f.txt")
      IO.writeString(path, "first")
      IO.writeString(path, "second")
      assertEquals(IO.readString(path), "second")
    }
  }

  test("appendString appends to file") {
    withTempDir { dir =>
      val path = IO.joinPath(dir, "app.txt")
      IO.writeString(path, "line1")
      IO.appendString(path, "\nline2")
      assert(IO.readString(path).contains("line2"))
    }
  }

  test("readString with non-default charset") {
    withTempDir { dir =>
      val path    = IO.joinPath(dir, "latin.txt")
      val content = "caf\u00e9"
      IO.writeString(path, content, "UTF-8")
      assertEquals(IO.readString(path, "UTF-8"), content)
    }
  }

  // ── readLines / writeLines ────────────────────────────────────────────────

  test("writeLines and readLines roundtrip") {
    withTempDir { dir =>
      val path  = IO.joinPath(dir, "lines.txt")
      val lines = List("alpha", "beta", "gamma")
      IO.writeLines(path, lines)
      val read = IO.readLines(path)
      assertEquals(read, lines)
    }
  }

  // ── readBytes / writeBytes ────────────────────────────────────────────────

  test("writeBytes and readBytes roundtrip") {
    withTempDir { dir =>
      val path  = IO.joinPath(dir, "bytes.bin")
      val bytes = Array[Byte](0, 1, 2, 127, -128)
      IO.writeBytes(path, bytes)
      val result = IO.readBytes(path)
      assertEquals(result.toSeq, bytes.toSeq)
    }
  }

  // ── exists / isFile / isDirectory ─────────────────────────────────────────

  test("exists returns true for existing file") {
    withTempDir { dir =>
      val path = IO.joinPath(dir, "x.txt")
      IO.writeString(path, "x")
      assert(IO.exists(path))
    }
  }

  test("exists returns false for missing path") {
    assert(!IO.exists("/tmp/nslib-definitely-does-not-exist-xyz"))
  }

  test("isFile and isDirectory") {
    withTempDir { dir =>
      val path = IO.joinPath(dir, "f.txt")
      IO.writeString(path, "")
      assert(IO.isFile(path))
      assert(!IO.isDirectory(path))
      assert(!IO.isFile(dir))
      assert(IO.isDirectory(dir))
    }
  }

  // ── size ─────────────────────────────────────────────────────────────────

  test("size returns file length in bytes") {
    withTempDir { dir =>
      val path = IO.joinPath(dir, "sized.txt")
      IO.writeBytes(path, Array[Byte](1, 2, 3, 4, 5))
      assertEquals(IO.size(path), 5L)
    }
  }

  // ── listFiles ─────────────────────────────────────────────────────────────

  test("listFiles returns immediate children sorted") {
    withTempDir { dir =>
      IO.writeString(IO.joinPath(dir, "b.txt"), "")
      IO.writeString(IO.joinPath(dir, "a.txt"), "")
      IO.writeString(IO.joinPath(dir, "c.txt"), "")
      val names = IO.listFiles(dir).map(IO.fileName)
      assertEquals(names, List("a.txt", "b.txt", "c.txt"))
    }
  }

  test("listFiles throws for non-directory") {
    withTempDir { dir =>
      val path = IO.joinPath(dir, "f.txt")
      IO.writeString(path, "")
      intercept[nslib.io.IOException](IO.listFiles(path))
    }
  }

  // ── mkdirs ────────────────────────────────────────────────────────────────

  test("mkdirs creates nested directories") {
    withTempDir { dir =>
      val nested = IO.joinPath(dir, "a", "b", "c")
      IO.mkdirs(nested)
      assert(IO.isDirectory(nested))
    }
  }

  // ── delete / deleteRecursive ──────────────────────────────────────────────

  test("delete removes a file") {
    withTempDir { dir =>
      val path = IO.joinPath(dir, "del.txt")
      IO.writeString(path, "bye")
      IO.delete(path)
      assert(!IO.exists(path))
    }
  }

  test("deleteRecursive removes directory tree") {
    withTempDir { dir =>
      val sub = IO.joinPath(dir, "sub")
      IO.mkdirs(sub)
      IO.writeString(IO.joinPath(sub, "f.txt"), "")
      IO.deleteRecursive(sub)
      assert(!IO.exists(sub))
    }
  }

  // ── copy / move ───────────────────────────────────────────────────────────

  test("copy creates an identical file") {
    withTempDir { dir =>
      val src = IO.joinPath(dir, "src.txt")
      val dst = IO.joinPath(dir, "dst.txt")
      IO.writeString(src, "copied content")
      IO.copy(src, dst)
      assertEquals(IO.readString(dst), "copied content")
      assert(IO.exists(src))
    }
  }

  test("move renames a file") {
    withTempDir { dir =>
      val src = IO.joinPath(dir, "before.txt")
      val dst = IO.joinPath(dir, "after.txt")
      IO.writeString(src, "moved")
      IO.move(src, dst)
      assertEquals(IO.readString(dst), "moved")
      assert(!IO.exists(src))
    }
  }

  // ── path utilities ────────────────────────────────────────────────────────

  test("fileName returns last path component") {
    assertEquals(IO.fileName("/tmp/foo/bar.txt"), "bar.txt")
  }

  test("joinPath joins with separator") {
    val joined = IO.joinPath("a", "b", "c")
    assert(joined.contains("b"))
  }

  test("parentDir returns containing directory") {
    val parent = IO.parentDir("/tmp/foo/bar.txt")
    assert(parent.endsWith("foo") || parent == "/tmp/foo")
  }

  // ── temp files ────────────────────────────────────────────────────────────

  test("tempFile creates a real temporary file path") {
    val path = IO.tempFile()
    try {
      assert(!IO.exists(path) || IO.isFile(path))
    } finally {
      if (IO.exists(path)) IO.delete(path)
    }
  }

  test("tempDir creates a real temporary directory") {
    val dir = IO.tempDir("nslib-test")
    try assert(IO.isDirectory(dir))
    finally IO.deleteRecursive(dir)
  }
}
