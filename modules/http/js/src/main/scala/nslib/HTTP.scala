package nslib

import nslib.http.{HttpException, Response}

/** HTTP client — Scala.js stub.
  *
  * Asynchronous browser/Node.js HTTP support is planned.
  * Track progress at https://github.com/nslib/stdlib
  */
object HTTP {

  private def notSupported(op: String): Nothing =
    throw new UnsupportedOperationException(
      s"HTTP.$op is not yet supported on Scala.js. " +
        "Track progress: https://github.com/nslib/stdlib"
    )

  def get(url: String, headers: Map[String, String] = Map.empty): Response =
    notSupported("get")

  def post(url: String, body: String = "", headers: Map[String, String] = Map.empty): Response =
    notSupported("post")

  def put(url: String, body: String = "", headers: Map[String, String] = Map.empty): Response =
    notSupported("put")

  def delete(url: String, headers: Map[String, String] = Map.empty): Response =
    notSupported("delete")

  def patch(url: String, body: String = "", headers: Map[String, String] = Map.empty): Response =
    notSupported("patch")

  def postJson(url: String, json: JsonValue, headers: Map[String, String] = Map.empty): Response =
    notSupported("postJson")

  def getJson(url: String, headers: Map[String, String] = Map.empty): JsonValue =
    notSupported("getJson")
}
