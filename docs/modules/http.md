# stdlib-http

Simple synchronous HTTP client for Scala.
JVM only (Java 11+ `java.net.http`).  Does not throw on 4xx/5xx status codes.

## Quick start

```scala
libraryDependencies += "com.github.nslib" %% "stdlib-http" % "0.1.0"
```

```scala
import nslib._

val resp = HTTP.get("https://httpbin.org/get")
println(resp.status)    // 200
println(resp.body)      // JSON string
```

## API reference

### Methods

```scala
HTTP.get(url, headers = Map.empty): Response
HTTP.post(url, body = "", headers = Map.empty): Response
HTTP.put(url, body = "", headers = Map.empty): Response
HTTP.delete(url, headers = Map.empty): Response
HTTP.patch(url, body = "", headers = Map.empty): Response

// Convenience helpers
HTTP.postJson(url, jsonValue, headers = Map.empty): Response
HTTP.getJson(url, headers = Map.empty): JsonValue
```

### Response

```scala
resp.status: Int                          // HTTP status code
resp.body: String                         // response body
resp.headers: Map[String, String]         // response headers
resp.isSuccess: Boolean                   // status 2xx
resp.isError: Boolean                     // status 4xx or 5xx
resp.json: JsonValue                      // parse body as JSON
resp.header("content-type"): Option[String]  // case-insensitive lookup
resp.contentType: String                  // content-type header or ""
```

## Examples

### GET with JSON response

```scala
val json = HTTP.getJson("https://api.github.com/repos/scala/scala")
println(json("stargazers_count").asInt)
```

### POST JSON

```scala
val payload = JSON.obj(
  "title" -> JSON.str("Bug fix"),
  "body"  -> JSON.str("Fixed the thing"),
)
val resp = HTTP.postJson(
  "https://api.github.com/repos/owner/repo/issues",
  payload,
  headers = Map("Authorization" -> "Bearer token"),
)
println(resp.status)  // 201
```

### Check for errors without exceptions

```scala
val resp = HTTP.get("https://api.example.com/item/999")
if (resp.isError) {
  println(s"Error ${resp.status}: ${resp.body}")
} else {
  println(resp.json("name").asString)
}
```

## Error handling

Transport-level errors (no network, DNS failure, timeout) throw `nslib.http.HttpException`.

HTTP error codes (4xx, 5xx) **do not throw** — check `resp.isSuccess` / `resp.isError`.

```scala
try HTTP.get("https://does-not-exist.invalid")
catch { case e: nslib.http.HttpException => println(e.getMessage) }
```

## Defaults

| Setting | Default |
|---------|---------|
| Connect timeout | 30 seconds |
| Request timeout | 60 seconds |
| Redirects | Followed automatically (NORMAL) |

## Platform support

| Platform | Status | Notes |
|----------|--------|-------|
| JVM | ✅ full | Requires Java 11+ |
| Scala.js | ⚠️ stub | Throws `UnsupportedOperationException` |
| Scala Native | ⚠️ stub | Throws `UnsupportedOperationException` |
