package nslib

import nslib.json.internal.{JsonParser, JsonPrinter}

/** Simple JSON parsing and serialisation.
  *
  * == Quick start ==
  * {{{
  * import nslib._
  *
  * // Parse
  * val json = JSON.parse("""{"name": "Alice", "age": 30, "tags": ["admin"]}""")
  * json("name").asString          // "Alice"
  * json("age").asInt              // 30
  * json("tags")(0).asString       // "admin"
  * json.get("missing")            // None
  *
  * // Build
  * val data = JSON.obj(
  *   "name"   -> JSON.str("Bob"),
  *   "scores" -> JSON.arr(JSON.num(95), JSON.num(87)),
  *   "active" -> JSON.bool(true),
  *   "note"   -> JSON.`null`,
  * )
  *
  * // Serialise
  * JSON.stringify(data)           // compact
  * JSON.stringify(data, indent=2) // pretty-printed
  *
  * // Pattern matching (advanced)
  * import JSON._
  * data match {
  *   case Obj(fields) => fields.keys.toList
  *   case Arr(items)  => items
  *   case Str(s)      => List(s)
  *   case Num(n)      => Nil
  *   case Bool(b)     => Nil
  *   case Null        => Nil
  * }
  * }}}
  */
object JSON {

  // ── Parse ─────────────────────────────────────────────────────────────────

  /** Parse a JSON string.  Throws [[JsonParseException]] on invalid input. */
  def parse(text: String): JsonValue = new JsonParser(text).parse()

  /** Parse a JSON string and return None on failure instead of throwing. */
  def tryParse(text: String): Option[JsonValue] =
    try Some(parse(text))
    catch { case _: JsonParseException => None }

  // ── Serialise ──────────────────────────────────────────────────────────────

  /** Serialise to compact JSON (no extra whitespace). */
  def stringify(value: JsonValue): String = JsonPrinter.compact(value)

  /** Serialise to pretty-printed JSON.
    *
    * @param indent number of spaces per indentation level
    */
  def stringify(value: JsonValue, indent: Int): String = JsonPrinter.pretty(value, indent)

  // ── Constructors ──────────────────────────────────────────────────────────

  /** Build a JSON object from key-value pairs. */
  def obj(fields: (String, JsonValue)*): JsonValue = JsonObj(fields.toMap)

  /** Build a JSON array from values. */
  def arr(values: JsonValue*): JsonValue = JsonArr(values.toList)

  /** Wrap a number as a JSON value. */
  def num(n: Double): JsonValue = JsonNum(n)

  /** Wrap an Int as a JSON value. */
  def num(n: Int): JsonValue = JsonNum(n.toDouble)

  /** Wrap a Long as a JSON value. */
  def num(n: Long): JsonValue = JsonNum(n.toDouble)

  /** Wrap a string as a JSON value. */
  def str(s: String): JsonValue = JsonStr(s)

  /** Wrap a boolean as a JSON value. */
  def bool(b: Boolean): JsonValue = JsonBool(b)

  /** JSON null value. */
  val `null`: JsonValue = JsonNull

  // ── Extractors (for pattern matching) ─────────────────────────────────────

  /** Use as `case JSON.Null =>` in pattern matches. */
  val Null: JsonNull.type = JsonNull

  /** Use as `case JSON.Bool(b) =>` in pattern matches. */
  val Bool: JsonBool.type = JsonBool

  /** Use as `case JSON.Num(n) =>` in pattern matches. */
  val Num: JsonNum.type = JsonNum

  /** Use as `case JSON.Str(s) =>` in pattern matches. */
  val Str: JsonStr.type = JsonStr

  /** Use as `case JSON.Arr(items) =>` in pattern matches. */
  val Arr: JsonArr.type = JsonArr

  /** Use as `case JSON.Obj(fields) =>` in pattern matches. */
  val Obj: JsonObj.type = JsonObj

  // ── Conversions ────────────────────────────────────────────────────────────

  /** Convert a Scala value to a JsonValue.
    *
    * Supports: String, Int, Long, Double, Boolean, null/None,
    * Seq[_], Map[String,_], Option[_].
    */
  def from(value: Any): JsonValue = value match {
    case null         => JsonNull
    case None         => JsonNull
    case Some(v)      => from(v)
    case v: Boolean   => JsonBool(v)
    case v: Int       => JsonNum(v.toDouble)
    case v: Long      => JsonNum(v.toDouble)
    case v: Double    => JsonNum(v)
    case v: Float     => JsonNum(v.toDouble)
    case v: String    => JsonStr(v)
    case v: Seq[_]    => JsonArr(v.map(from).toList)
    case v: Map[_, _] => JsonObj(v.map { case (k, mv) => k.toString -> from(mv) })
    case other        => JsonStr(other.toString)
  }
}
