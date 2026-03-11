package nslib

import nslib.http.{HttpException, Response}

import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse => JHttpResponse}
import java.time.Duration
import scala.jdk.CollectionConverters._

/** Simple Http client.
  *
  * All methods are synchronous. Errors at the transport level throw [[nslib.http.HttpException]].
  * Http error codes (4xx / 5xx) do **not** throw — check [[nslib.http.Response.isSuccess]].
  *
  * Requires Java 11+.
  *
  * ==Quick start==
  * {{{
  * import nslib._
  *
  * // GET
  * val resp = Http.get("https://httpbin.org/get")
  * println(resp.status)   // 200
  * println(resp.body)     // response body
  *
  * // POST Json
  * val data = Json.stringify(Json.obj("name" -> Json.str("Alice")))
  * val resp2 = Http.post(
  *   "https://httpbin.org/post",
  *   body    = data,
  *   headers = Map("Content-Type" -> "application/json"),
  * )
  *
  * // Parse response as Json
  * val json = Http.get("https://httpbin.org/json").json
  * json("slideshow")("title").asString
  * }}}
  */
object Http {

  /** Default connect timeout (30 s). Override with [[withTimeout]]. */
  private val DefaultConnectTimeout: Duration = Duration.ofSeconds(30)

  /** Default request timeout (60 s). */
  private val DefaultRequestTimeout: Duration = Duration.ofSeconds(60)

  private val sharedClient: HttpClient = HttpClient
    .newBuilder()
    .connectTimeout(DefaultConnectTimeout)
    .followRedirects(HttpClient.Redirect.NORMAL)
    .build()

  // ── Public API ────────────────────────────────────────────────────────────

  /** Http GET request.
    *
    * @param url
    *   the URL to fetch
    * @param headers
    *   optional additional request headers
    */
  def get(url: String, headers: Map[String, String] = Map.empty): Response =
    send(buildRequest(url, headers, HttpRequest.BodyPublishers.noBody()).GET().build())

  /** Http POST request.
    *
    * @param url
    *   the target URL
    * @param body
    *   request body string (default: empty)
    * @param headers
    *   optional additional request headers
    */
  def post(
      url: String,
      body: String = "",
      headers: Map[String, String] = Map.empty
  ): Response = {
    val pub =
      if (body.isEmpty) HttpRequest.BodyPublishers.noBody()
      else HttpRequest.BodyPublishers.ofString(body)
    send(buildRequest(url, headers, pub).POST(pub).build())
  }

  /** Http PUT request. */
  def put(
      url: String,
      body: String = "",
      headers: Map[String, String] = Map.empty
  ): Response = {
    val pub =
      if (body.isEmpty) HttpRequest.BodyPublishers.noBody()
      else HttpRequest.BodyPublishers.ofString(body)
    send(buildRequest(url, headers, pub).PUT(pub).build())
  }

  /** Http DELETE request. */
  def delete(url: String, headers: Map[String, String] = Map.empty): Response =
    send(buildRequest(url, headers, HttpRequest.BodyPublishers.noBody()).DELETE().build())

  /** Http PATCH request. */
  def patch(
      url: String,
      body: String = "",
      headers: Map[String, String] = Map.empty
  ): Response = {
    val pub =
      if (body.isEmpty) HttpRequest.BodyPublishers.noBody()
      else HttpRequest.BodyPublishers.ofString(body)
    send(
      buildRequest(url, headers, pub)
        .method("PATCH", pub)
        .build()
    )
  }

  /** POST with `Content-Type: application/json` set automatically.
    *
    * @param url
    *   the target URL
    * @param json
    *   a [[JsonValue]] to serialise as the request body
    */
  def postJson(url: String, json: JsonValue, headers: Map[String, String] = Map.empty): Response =
    post(url, Json.stringify(json), headers + ("Content-Type" -> "application/json"))

  /** POST a raw JSON string with `Content-Type: application/json` set automatically.
    *
    * Use this when you already have a serialised JSON string and don't want an extra parse +
    * stringify round-trip.
    *
    * {{{
    * Http.postJsonRaw("https://api.example.com/items", """{"name":"widget"}""")
    * }}}
    *
    * @param body
    *   a pre-serialised JSON string
    */
  def postJsonRaw(url: String, body: String, headers: Map[String, String] = Map.empty): Response =
    post(url, body, headers + ("Content-Type" -> "application/json"))

  /** GET and immediately parse the response body as Json. Throws [[nslib.JsonParseException]] if
    * the body is not valid Json.
    */
  def getJson(url: String, headers: Map[String, String] = Map.empty): JsonValue =
    get(url, headers).json

  // ── Internals ─────────────────────────────────────────────────────────────

  private def buildRequest(
      url: String,
      headers: Map[String, String],
      body: HttpRequest.BodyPublisher
  ): HttpRequest.Builder = {
    var builder = HttpRequest
      .newBuilder(URI.create(url))
      .timeout(DefaultRequestTimeout)
    for ((k, v) <- headers) builder = builder.header(k, v)
    builder
  }

  private def send(request: HttpRequest): Response = {
    val jResp =
      try sharedClient.send(request, JHttpResponse.BodyHandlers.ofString())
      catch {
        case e: java.io.IOException =>
          throw HttpException(s"Http request failed: ${e.getMessage}", e)
        case e: InterruptedException =>
          Thread.currentThread().interrupt()
          throw HttpException("Http request interrupted", e)
      }
    val headers: Map[String, String] = jResp
      .headers()
      .map()
      .asScala
      .map { case (k, vs) => k -> vs.asScala.mkString(", ") }
      .toMap
    Response(jResp.statusCode(), jResp.body(), headers)
  }
}
