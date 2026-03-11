# nslib stdlib

A batteries-included standard-library toolkit for everyday Scala programming.

**Simple.  Practical.  No FP jargon required.**

---

## One dependency to rule them all

```scala
libraryDependencies += "io.github.nslib-org" %% "stdlib-all" % "0.1.0"
```

```scala
import nslib._
```

Supports **Scala 2.13** and **Scala 3**,
cross-compiled for **JVM**, **Scala.js**, and **Scala Native**.

---

## Modules

| Module | Artifact | Description |
|--------|----------|-------------|
| [JSON](#json) | `stdlib-json` | Parse and build JSON |
| [IO](#io)     | `stdlib-io`   | Read and write files |
| [HTTP](#http) | `stdlib-http` | Make HTTP requests |
| `stdlib-all`  | all of the above in one |

---

## JSON

```scala
import nslib._

// Parse
val json = JSON.parse("""{"name":"Alice","scores":[95,87,100]}""")
json("name").asString          // "Alice"
json("scores")(0).asInt        // 95
json.get("missing")            // None

// Build
val data = JSON.obj(
  "user"   -> JSON.str("Alice"),
  "active" -> JSON.bool(true),
  "count"  -> JSON.num(42),
  "tags"   -> JSON.arr(JSON.str("admin"), JSON.str("user")),
)

// Serialise
JSON.stringify(data)              // compact
JSON.stringify(data, indent = 2)  // pretty-printed

// Safe parse
JSON.tryParse("bad json")         // None
JSON.tryParse("{}")               // Some(JsonObj(...))

// Pattern matching
import JSON._
json match {
  case Obj(fields) => fields.keys.toList
  case Arr(items)  => items.map(_.asString)
  case Str(s)      => List(s)
  case Num(n)      => Nil
  case Bool(b)     => Nil
  case Null        => Nil
}
```

**Platform support:** JVM ✅ · Scala.js ✅ · Scala Native ✅

---

## IO

```scala
import nslib._

// Read / Write
IO.writeString("/tmp/hello.txt", "Hello, world!")
IO.readString("/tmp/hello.txt")            // "Hello, world!"

IO.writeLines("/tmp/items.txt", List("a", "b", "c"))
IO.readLines("/tmp/items.txt")             // List("a", "b", "c")

// Query
IO.exists("/tmp/hello.txt")                // true
IO.isFile("/tmp/hello.txt")                // true
IO.isDirectory("/tmp")                     // true
IO.size("/tmp/hello.txt")                  // bytes

// Directory
IO.mkdirs("/tmp/a/b/c")
IO.listFiles("/tmp")                       // List(...)

// Copy / Move / Delete
IO.copy("/tmp/hello.txt", "/tmp/copy.txt")
IO.move("/tmp/copy.txt", "/tmp/moved.txt")
IO.delete("/tmp/moved.txt")
IO.deleteRecursive("/tmp/a")

// Temporary files
val tmpFile = IO.tempFile()
val tmpDir  = IO.tempDir()

// Paths
IO.fileName("/tmp/foo/bar.txt")    // "bar.txt"
IO.parentDir("/tmp/foo/bar.txt")   // "/tmp/foo"
IO.joinPath("a", "b", "c")        // "a/b/c" (or a\b\c on Windows)
```

**Platform support:** JVM ✅ · Scala.js ⚠️ planned · Scala Native ✅

---

## HTTP

```scala
import nslib._

// GET
val resp = HTTP.get("https://httpbin.org/get")
resp.status       // 200
resp.body         // response body string
resp.isSuccess    // true
resp.isError      // false

// POST
HTTP.post(
  "https://api.example.com/items",
  body    = """{"name":"widget"}""",
  headers = Map("Content-Type" -> "application/json"),
)

// GET + JSON in one step
val json = HTTP.getJson("https://httpbin.org/json")
json("slideshow")("title").asString

// POST a JsonValue directly
HTTP.postJson(
  "https://api.example.com/items",
  JSON.obj("name" -> JSON.str("widget"), "qty" -> JSON.num(10)),
)

// Error codes don't throw — check the status
val notFound = HTTP.get("https://httpbin.org/status/404")
notFound.isSuccess    // false
notFound.isError      // true
notFound.status       // 404
```

**Platform support:** JVM ✅ (Java 11+) · Scala.js ⚠️ planned · Scala Native ⚠️ planned

---

## Individual module dependencies

```scala
// Pick only what you need
libraryDependencies += "io.github.nslib-org" %% "stdlib-json" % "0.1.0"
libraryDependencies += "io.github.nslib-org" %% "stdlib-io"   % "0.1.0"
libraryDependencies += "io.github.nslib-org" %% "stdlib-http" % "0.1.0"
```

For Scala.js or Scala Native, use `%%%`:
```scala
libraryDependencies += "io.github.nslib-org" %%% "stdlib-json" % "0.1.0"
```

---

## Roadmap

| Module | Status |
|--------|--------|
| `stdlib-json`   | ✅ available |
| `stdlib-io`     | ✅ available (JVM + Native) |
| `stdlib-http`   | ✅ available (JVM) |
| `stdlib-csv`    | 🔜 planned |
| `stdlib-config` | 🔜 planned |
| `stdlib-cli`    | 🔜 planned |
| `stdlib-db`     | 🔜 planned |

---

## Design principles

1. **Everyday tasks first** — JSON, files, HTTP, config, CSV.
2. **One example is enough** — the API should be obvious on first sight.
3. **No vocabulary barrier** — no monads, effects, streams, or type class imports required.
4. **Pure Scala preferred** — minimal dependencies, no heavy frameworks.
5. **Cross-platform** — JVM, Scala.js, and Scala Native from day one.

---

## Contributing

See [AGENTS.md](AGENTS.md) for coding conventions, naming rules, and the PR process.

Found a bug? Open a [bug report](.github/ISSUE_TEMPLATE/bug_report.yml).
Have an idea? Open a [feature request](.github/ISSUE_TEMPLATE/feature_request.yml).

---

## License

Apache-2.0 © nslib contributors
