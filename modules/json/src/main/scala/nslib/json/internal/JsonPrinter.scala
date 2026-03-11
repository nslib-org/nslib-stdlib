package nslib.json.internal

import nslib._

private[nslib] object JsonPrinter {

  def compact(value: JsonValue): String = {
    val sb = new StringBuilder
    writeCompact(value, sb)
    sb.toString
  }

  def pretty(value: JsonValue, indent: Int): String = {
    val sb = new StringBuilder
    writePretty(value, indent, 0, sb)
    sb.toString
  }

  // ── compact ──────────────────────────────────────────────────────────────────

  private def writeCompact(v: JsonValue, sb: StringBuilder): Unit = v match {
    case JsonNull       => sb ++= "null"
    case JsonBool(b)    => sb ++= (if (b) "true" else "false")
    case JsonNum(n)     => sb ++= numberToString(n)
    case JsonStr(s)     => sb += '"'; appendEscaped(s, sb); sb += '"'
    case JsonArr(items) =>
      sb += '['
      var first = true
      for (item <- items) {
        if (!first) sb += ','
        writeCompact(item, sb)
        first = false
      }
      sb += ']'
    case JsonObj(fields) =>
      sb += '{'
      var first = true
      for ((k, v) <- fields) {
        if (!first) sb += ','
        sb += '"'; appendEscaped(k, sb); sb ++= "\":"
        writeCompact(v, sb)
        first = false
      }
      sb += '}'
  }

  // ── pretty ───────────────────────────────────────────────────────────────────

  private def writePretty(v: JsonValue, indent: Int, level: Int, sb: StringBuilder): Unit = v match {
    case JsonNull    => sb ++= "null"
    case JsonBool(b) => sb ++= (if (b) "true" else "false")
    case JsonNum(n)  => sb ++= numberToString(n)
    case JsonStr(s)  => sb += '"'; appendEscaped(s, sb); sb += '"'

    case JsonArr(Nil)   => sb ++= "[]"
    case JsonArr(items) =>
      val pad  = " " * (indent * (level + 1))
      val cpad = " " * (indent * level)
      sb ++= "[\n"
      var first = true
      for (item <- items) {
        if (!first) sb ++= ",\n"
        sb ++= pad
        writePretty(item, indent, level + 1, sb)
        first = false
      }
      sb += '\n'; sb ++= cpad; sb += ']'

    case JsonObj(fields) if fields.isEmpty => sb ++= "{}"
    case JsonObj(fields) =>
      val pad  = " " * (indent * (level + 1))
      val cpad = " " * (indent * level)
      sb ++= "{\n"
      var first = true
      for ((k, fv) <- fields) {
        if (!first) sb ++= ",\n"
        sb ++= pad
        sb += '"'; appendEscaped(k, sb); sb ++= "\": "
        writePretty(fv, indent, level + 1, sb)
        first = false
      }
      sb += '\n'; sb ++= cpad; sb += '}'
  }

  // ── helpers ──────────────────────────────────────────────────────────────────

  private def numberToString(n: Double): String =
    if (n == n.toLong.toDouble && !n.isInfinite) n.toLong.toString
    else n.toString

  private def appendEscaped(s: String, sb: StringBuilder): Unit =
    for (c <- s) c match {
      case '"'  => sb ++= "\\\""
      case '\\' => sb ++= "\\\\"
      case '\b' => sb ++= "\\b"
      case '\f' => sb ++= "\\f"
      case '\n' => sb ++= "\\n"
      case '\r' => sb ++= "\\r"
      case '\t' => sb ++= "\\t"
      case c if c < 0x20 => sb ++= f"\\u${c.toInt}%04x"
      case c    => sb += c
    }
}
