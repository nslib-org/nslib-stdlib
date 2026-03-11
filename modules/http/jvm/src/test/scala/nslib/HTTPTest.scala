package nslib

import munit.FunSuite
import nslib.http.Response

class HTTPTest extends FunSuite {

  // ── Unit tests: Response ──────────────────────────────────────────────────

  test("Response.isSuccess: 200 is success") {
    assert(Response(200, "", Map.empty).isSuccess)
  }

  test("Response.isSuccess: 201 is success") {
    assert(Response(201, "", Map.empty).isSuccess)
  }

  test("Response.isSuccess: 404 is not success") {
    assert(!Response(404, "", Map.empty).isSuccess)
  }

  test("Response.isError: 400 is error") {
    assert(Response(400, "", Map.empty).isError)
  }

  test("Response.isError: 200 is not error") {
    assert(!Response(200, "", Map.empty).isError)
  }

  test("Response.json parses body as Json") {
    val resp = Response(200, """{"ok":true}""", Map.empty)
    assertEquals(resp.json("ok").asBoolean, true)
  }

  test("Response.header is case-insensitive") {
    val resp = Response(200, "", Map("Content-Type" -> "application/json"))
    assertEquals(resp.header("content-type"), Some("application/json"))
    assertEquals(resp.header("CONTENT-TYPE"), Some("application/json"))
  }

  test("Response.header returns None for absent header") {
    assertEquals(Response(200, "", Map.empty).header("x-missing"), None)
  }

  test("Response.contentType returns content-type or empty string") {
    val resp = Response(200, "", Map("content-type" -> "text/html"))
    assertEquals(resp.contentType, "text/html")
    assertEquals(Response(200, "", Map.empty).contentType, "")
  }

  // ── Integration tests (skipped unless INTEGRATION_TESTS env var is set) ──

  private val runIntegration = sys.env.contains("INTEGRATION_TESTS")

  test("Http.get returns 200 from httpbin") {
    assume(runIntegration, "Set INTEGRATION_TESTS=1 to run Http integration tests")
    val resp = Http.get("https://httpbin.org/get")
    assertEquals(resp.status, 200)
    assert(resp.body.nonEmpty)
  }

  test("Http.post sends body") {
    assume(runIntegration, "Set INTEGRATION_TESTS=1 to run Http integration tests")
    val resp = Http.post(
      "https://httpbin.org/post",
      body    = """{"hello":"world"}""",
      headers = Map("Content-Type" -> "application/json"),
    )
    assertEquals(resp.status, 200)
    assert(resp.body.contains("hello"))
  }

  test("Http.getJson parses response directly") {
    assume(runIntegration, "Set INTEGRATION_TESTS=1 to run Http integration tests")
    val json = Http.getJson("https://httpbin.org/json")
    assert(json.get("slideshow").isDefined)
  }

  test("Http.get follows redirects") {
    assume(runIntegration, "Set INTEGRATION_TESTS=1 to run Http integration tests")
    val resp = Http.get("https://httpbin.org/redirect/1")
    assertEquals(resp.status, 200)
  }

  test("Http 404 does not throw") {
    assume(runIntegration, "Set INTEGRATION_TESTS=1 to run Http integration tests")
    val resp = Http.get("https://httpbin.org/status/404")
    assertEquals(resp.status, 404)
    assert(!resp.isSuccess)
    assert(resp.isError)
  }
}
