# stdlib-io

Simple synchronous file I/O utilities for Scala.
JVM and Scala Native (via `java.io`).

## Quick start

```scala
libraryDependencies += "com.github.nslib" %% "stdlib-io" % "0.1.0"
```

```scala
import nslib._

IO.writeString("/tmp/hello.txt", "Hello!")
IO.readString("/tmp/hello.txt")   // "Hello!"
```

## API reference

### Read

| Method | Description |
|--------|-------------|
| `IO.readString(path)` | Read file as UTF-8 string |
| `IO.readString(path, charset)` | Read file with specific charset |
| `IO.readLines(path)` | Read all lines as `List[String]` |
| `IO.readBytes(path)` | Read file as `Array[Byte]` |

### Write

| Method | Description |
|--------|-------------|
| `IO.writeString(path, content)` | Write UTF-8 string (create or overwrite) |
| `IO.writeString(path, content, charset)` | Write with specific charset |
| `IO.appendString(path, content)` | Append UTF-8 string |
| `IO.writeLines(path, lines)` | Write lines joined by line separator |
| `IO.writeBytes(path, bytes)` | Write raw bytes |

### Query

| Method | Returns | Description |
|--------|---------|-------------|
| `IO.exists(path)` | `Boolean` | File or directory exists |
| `IO.isFile(path)` | `Boolean` | Path is a regular file |
| `IO.isDirectory(path)` | `Boolean` | Path is a directory |
| `IO.size(path)` | `Long` | File size in bytes |
| `IO.lastModified(path)` | `Long` | Last modified (millis since epoch) |

### Directory listing

```scala
IO.listFiles("/tmp")  // List of absolute paths of immediate children (sorted)
```

### Manipulation

| Method | Description |
|--------|-------------|
| `IO.mkdirs(path)` | Create directory and all parents |
| `IO.delete(path)` | Delete a file (no-op if not found) |
| `IO.deleteRecursive(path)` | Delete directory tree |
| `IO.copy(from, to)` | Copy a file |
| `IO.move(from, to)` | Move / rename a file |

### Temporary files

```scala
IO.tempFile()                    // new temp file path
IO.tempFile("prefix", ".ext")
IO.tempDir()                     // new temp directory path
IO.tempDir("prefix")
```

### Path utilities

```scala
IO.fileName("/a/b/c.txt")        // "c.txt"
IO.parentDir("/a/b/c.txt")       // "/a/b"
IO.absolutePath("../foo")        // canonical absolute path
IO.joinPath("a", "b", "c")      // "a/b/c" (OS separator)
```

## Error handling

All methods throw `nslib.io.IOException` on failure.

```scala
try IO.readString("/nonexistent")
catch { case e: nslib.io.IOException => println(e.getMessage) }
```

## Platform support

| Platform | Status | Notes |
|----------|--------|-------|
| JVM | ✅ full | Requires Java 11+ |
| Scala.js | ⚠️ stub | Throws `UnsupportedOperationException` |
| Scala Native | ✅ full | Uses `java.io` |
