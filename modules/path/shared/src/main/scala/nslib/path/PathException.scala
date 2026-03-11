package nslib.path

/** Thrown when a path-related operation fails.
  *
  * @param message
  *   human-readable description of the failure
  * @param cause
  *   underlying exception, if any
  */
final class PathException(message: String, cause: Throwable = null)
    extends RuntimeException(message, cause)

object PathException {
  def apply(message: String): PathException                   = new PathException(message)
  def apply(message: String, cause: Throwable): PathException = new PathException(message, cause)
}
