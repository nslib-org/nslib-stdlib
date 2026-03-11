package nslib

/** Thrown when a JSON string cannot be parsed. */
final class JsonParseException(message: String, cause: Throwable = null)
    extends RuntimeException(message, cause)

object JsonParseException {
  def apply(message: String): JsonParseException = new JsonParseException(message)
  def apply(message: String, cause: Throwable): JsonParseException =
    new JsonParseException(message, cause)
}

/** Thrown when accessing a JSON value in an incompatible way (e.g. `.asString` on a number). */
final class JsonAccessException(message: String)
    extends RuntimeException(message)

object JsonAccessException {
  def apply(message: String): JsonAccessException = new JsonAccessException(message)
}
