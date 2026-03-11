package nslib.io

/** Thrown when a file-system operation fails.
  *
  * @param message
  *   human-readable description of the failure
  * @param cause
  *   underlying exception, if any
  */
final class IOException(message: String, cause: Throwable = null)
    extends RuntimeException(message, cause)

object IOException {
  def apply(message: String): IOException                   = new IOException(message)
  def apply(message: String, cause: Throwable): IOException = new IOException(message, cause)
}
