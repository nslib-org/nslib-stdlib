package nslib

/** A JSON value.
  *
  * Use [[JSON]] to parse, build, and serialise JSON.
  *
  * {{{
  * val json = JSON.parse("""{"name":"Alice","scores":[10,20,30]}""")
  * json("name").asString          // "Alice"
  * json("scores")(0).asInt        // 10
  * json("scores").asArray.map(_.asInt)  // List(10, 20, 30)
  * }}}
  */
sealed abstract class JsonValue {

  /** Access an object field by key.  Throws if this is not an object or the key is absent. */
  def apply(key: String): JsonValue =
    throw JsonAccessException(s"Cannot access field '$key' on ${typeName}")

  /** Access an array element by index.  Throws if this is not an array or the index is out of bounds. */
  def apply(idx: Int): JsonValue =
    throw JsonAccessException(s"Cannot access index $idx on ${typeName}")

  /** Return value as String.  Throws if this is not a JSON string. */
  def asString: String = throw JsonAccessException(s"Expected string, got ${typeName}")

  /** Return value as Double.  Throws if this is not a JSON number. */
  def asDouble: Double = throw JsonAccessException(s"Expected number, got ${typeName}")

  /** Return value as Int.  Throws if this is not a JSON number. */
  def asInt: Int = throw JsonAccessException(s"Expected number, got ${typeName}")

  /** Return value as Long.  Throws if this is not a JSON number. */
  def asLong: Long = throw JsonAccessException(s"Expected number, got ${typeName}")

  /** Return value as Boolean.  Throws if this is not a JSON boolean. */
  def asBoolean: Boolean = throw JsonAccessException(s"Expected boolean, got ${typeName}")

  /** Return value as a Scala List.  Throws if this is not a JSON array. */
  def asArray: List[JsonValue] = throw JsonAccessException(s"Expected array, got ${typeName}")

  /** Return value as a Scala Map.  Throws if this is not a JSON object. */
  def asMap: Map[String, JsonValue] = throw JsonAccessException(s"Expected object, got ${typeName}")

  /** Safely look up a field in an object.  Returns None for non-objects or missing keys. */
  def get(key: String): Option[JsonValue] = None

  /** True only for JSON null. */
  def isNull: Boolean = false

  /** Serialise to a compact JSON string (no whitespace). */
  def stringify: String = JSON.stringify(this)

  /** Serialise to a pretty-printed JSON string with the given indent size. */
  def stringify(indent: Int): String = JSON.stringify(this, indent)

  /** Human-readable type name for error messages. */
  private[nslib] def typeName: String
}

// ─── Concrete types ───────────────────────────────────────────────────────────

/** JSON null. */
case object JsonNull extends JsonValue {
  override def isNull: Boolean                    = true
  private[nslib] override def typeName: String    = "null"
}

/** JSON boolean. */
final case class JsonBool(value: Boolean) extends JsonValue {
  override def asBoolean: Boolean              = value
  private[nslib] override def typeName: String = "boolean"
}

/** JSON number. */
final case class JsonNum(value: Double) extends JsonValue {
  override def asDouble: Double              = value
  override def asInt: Int                    = value.toInt
  override def asLong: Long                  = value.toLong
  private[nslib] override def typeName: String = "number"
}

/** JSON string. */
final case class JsonStr(value: String) extends JsonValue {
  override def asString: String              = value
  private[nslib] override def typeName: String = "string"
}

/** JSON array. */
final case class JsonArr(values: List[JsonValue]) extends JsonValue {
  override def apply(idx: Int): JsonValue = {
    if (idx < 0 || idx >= values.size)
      throw JsonAccessException(s"Index $idx out of bounds (array size=${values.size})")
    values(idx)
  }
  override def asArray: List[JsonValue]      = values
  private[nslib] override def typeName: String = "array"
}

/** JSON object. */
final case class JsonObj(fields: Map[String, JsonValue]) extends JsonValue {
  override def apply(key: String): JsonValue =
    fields.getOrElse(key, throw JsonAccessException(s"Key '$key' not found"))
  override def asMap: Map[String, JsonValue]  = fields
  override def get(key: String): Option[JsonValue] = fields.get(key)
  private[nslib] override def typeName: String     = "object"
}
