package nslib.http

/** Thrown when an Http request fails at the transport level (connection error, timeout, etc.).
  *
  * Use [[Response.isError]] to detect 4xx/5xx Http status codes without exceptions.
  */
final class HttpException(message: String, cause: Throwable = null)
    extends RuntimeException(message, cause)

object HttpException {
  def apply(message: String): HttpException                   = new HttpException(message)
  def apply(message: String, cause: Throwable): HttpException = new HttpException(message, cause)
}
