package nslib.examples

import nslib._

/** Run with: sbt "examples/runMain nslib.examples.HTTPExample"
  *
  * Requires internet access.  Uses https://httpbin.org as a public echo server.
  */
object HTTPExample extends App {

  // ── GET ───────────────────────────────────────────────────────────────

  val resp = HTTP.get("https://httpbin.org/get")
  println(s"Status: ${resp.status}")           // 200
  println(s"Success: ${resp.isSuccess}")        // true
  println(s"Content-Type: ${resp.contentType}")

  // ── GET + JSON parsing ────────────────────────────────────────────────

  val json = HTTP.getJson("https://httpbin.org/json")
  println(json("slideshow")("title").asString)  // Sample Slide Show

  // ── POST JSON ─────────────────────────────────────────────────────────

  val payload = JSON.obj(
    "name"  -> JSON.str("Alice"),
    "score" -> JSON.num(99),
  )
  val postResp = HTTP.postJson("https://httpbin.org/post", payload)
  println(s"POST status: ${postResp.status}")   // 200

  // ── POST form data ────────────────────────────────────────────────────

  val formResp = HTTP.post(
    "https://httpbin.org/post",
    body    = "name=Bob&age=25",
    headers = Map("Content-Type" -> "application/x-www-form-urlencoded"),
  )
  println(s"Form POST: ${formResp.status}")

  // ── Custom headers ────────────────────────────────────────────────────

  val authed = HTTP.get(
    "https://httpbin.org/headers",
    headers = Map("X-Api-Key" -> "my-secret-key"),
  )
  println(authed.json("headers")("X-Api-Key").asString) // my-secret-key

  // ── Error codes don't throw ───────────────────────────────────────────

  val notFound = HTTP.get("https://httpbin.org/status/404")
  println(s"404 isSuccess: ${notFound.isSuccess}") // false
  println(s"404 isError:   ${notFound.isError}")   // true
}
