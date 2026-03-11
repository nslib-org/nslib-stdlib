package nslib.examples

import nslib._

/** Run with: sbt "examples/runMain nslib.examples.HTTPExample"
  *
  * Requires internet access. Uses https://httpbin.org as a public echo server.
  */
object HTTPExample extends App {

  // ── GET ───────────────────────────────────────────────────────────────

  val resp = Http.get("https://httpbin.org/get")
  println(s"Status: ${resp.status}")     // 200
  println(s"Success: ${resp.isSuccess}") // true
  println(s"Content-Type: ${resp.contentType}")

  // ── GET + Json parsing ────────────────────────────────────────────────

  val json = Http.getJson("https://httpbin.org/json")
  println(json("slideshow")("title").asString) // Sample Slide Show

  // ── POST Json ─────────────────────────────────────────────────────────

  val payload = Json.obj(
    "name"  -> Json.str("Alice"),
    "score" -> Json.num(99)
  )
  val postResp = Http.postJson("https://httpbin.org/post", payload)
  println(s"POST status: ${postResp.status}") // 200

  // ── POST form data ────────────────────────────────────────────────────

  val formResp = Http.post(
    "https://httpbin.org/post",
    body = "name=Bob&age=25",
    headers = Map("Content-Type" -> "application/x-www-form-urlencoded")
  )
  println(s"Form POST: ${formResp.status}")

  // ── Custom headers ────────────────────────────────────────────────────

  val authed = Http.get(
    "https://httpbin.org/headers",
    headers = Map("X-Api-Key" -> "my-secret-key")
  )
  println(authed.json("headers")("X-Api-Key").asString) // my-secret-key

  // ── Error codes don't throw ───────────────────────────────────────────

  val notFound = Http.get("https://httpbin.org/status/404")
  println(s"404 isSuccess: ${notFound.isSuccess}") // false
  println(s"404 isError:   ${notFound.isError}")   // true
}
