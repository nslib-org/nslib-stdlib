package nslib.http

import nslib.{Json, JsonValue}

/** An Http response.
  *
  * {{{
  * val resp = Http.get("https://httpbin.org/get")
  * resp.status       // 200
  * resp.body         // response body as String
  * resp.json         // parse body as Json
  * resp.isSuccess    // true if status 2xx
  * resp.header("content-type")  // option of first matching header
  * }}}
  */
final case class Response(
    status: Int,
    body: String,
    headers: Map[String, String]
) {

  /** True if the status code is 2xx. */
  def isSuccess: Boolean = status >= 200 && status < 300

  /** True if the status code is 4xx or 5xx. */
  def isError: Boolean = status >= 400

  /** Parse the response body as Json.  Throws [[nslib.JsonParseException]] on invalid Json. */
  def json: JsonValue = Json.parse(body)

  /** First value of the given header name (case-insensitive lookup). */
  def header(name: String): Option[String] = {
    val lower = name.toLowerCase
    headers.collectFirst { case (k, v) if k.toLowerCase == lower => v }
  }

  /** Content-Type header value, or empty string if absent. */
  def contentType: String = header("content-type").getOrElse("")

  override def toString: String =
    s"Response($status, body=${body.take(80)}${if (body.length > 80) "…" else ""})"
}
