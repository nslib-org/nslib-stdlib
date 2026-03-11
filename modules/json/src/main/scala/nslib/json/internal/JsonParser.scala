package nslib.json.internal

import nslib._
import scala.collection.mutable

/** Recursive-descent JSON parser.  Not thread-safe; create one per parse call. */
private[nslib] final class JsonParser(input: String) {
  private var pos: Int = 0

  def parse(): JsonValue = {
    skipWs()
    val v = parseValue()
    skipWs()
    if (pos != input.length)
      throw JsonParseException(
        s"Unexpected content at position $pos: '${input.charAt(pos)}'"
      )
    v
  }

  // ── whitespace ──────────────────────────────────────────────────────────────

  private def skipWs(): Unit =
    while (pos < input.length && isWs(input.charAt(pos))) pos += 1

  private def isWs(c: Char): Boolean = c == ' ' || c == '\t' || c == '\n' || c == '\r'

  // ── expect ───────────────────────────────────────────────────────────────────

  private def expect(c: Char): Unit = {
    if (pos >= input.length)
      throw JsonParseException(s"Expected '$c' but reached end of input")
    if (input.charAt(pos) != c)
      throw JsonParseException(
        s"Expected '$c' but found '${input.charAt(pos)}' at position $pos"
      )
    pos += 1
  }

  // ── dispatch ─────────────────────────────────────────────────────────────────

  private def parseValue(): JsonValue = {
    skipWs()
    if (pos >= input.length) throw JsonParseException("Unexpected end of input")
    input.charAt(pos) match {
      case 'n'                           => parseLiteral("null", JsonNull)
      case 't'                           => parseLiteral("true", JsonBool(true))
      case 'f'                           => parseLiteral("false", JsonBool(false))
      case '"'                           => parseString()
      case '['                           => parseArray()
      case '{'                           => parseObject()
      case c if c == '-' || c.isDigit    => parseNumber()
      case c                             =>
        throw JsonParseException(s"Unexpected character '$c' at position $pos")
    }
  }

  // ── literals ─────────────────────────────────────────────────────────────────

  private def parseLiteral(expected: String, result: JsonValue): JsonValue = {
    var i = 0
    while (i < expected.length) {
      if (pos >= input.length || input.charAt(pos) != expected.charAt(i))
        throw JsonParseException(s"Expected '$expected' at position ${pos - i}")
      pos += 1
      i += 1
    }
    result
  }

  // ── string ───────────────────────────────────────────────────────────────────

  private def parseString(): JsonStr = {
    expect('"')
    val sb = new StringBuilder
    while (pos < input.length && input.charAt(pos) != '"') {
      if (input.charAt(pos) == '\\') {
        pos += 1
        if (pos >= input.length)
          throw JsonParseException("Unexpected end of input inside string escape")
        input.charAt(pos) match {
          case '"'  => sb += '"';  pos += 1
          case '\\' => sb += '\\'; pos += 1
          case '/'  => sb += '/';  pos += 1
          case 'b'  => sb += '\b'; pos += 1
          case 'f'  => sb += '\f'; pos += 1
          case 'n'  => sb += '\n'; pos += 1
          case 'r'  => sb += '\r'; pos += 1
          case 't'  => sb += '\t'; pos += 1
          case 'u'  =>
            pos += 1
            if (pos + 4 > input.length)
              throw JsonParseException("Incomplete \\uXXXX escape")
            val hex = input.substring(pos, pos + 4)
            try sb += Integer.parseInt(hex, 16).toChar
            catch {
              case _: NumberFormatException =>
                throw JsonParseException(s"Invalid unicode escape: \\u$hex")
            }
            pos += 4
          case c =>
            throw JsonParseException(s"Invalid escape sequence: \\$c")
        }
      } else {
        sb += input.charAt(pos)
        pos += 1
      }
    }
    expect('"')
    JsonStr(sb.toString)
  }

  // ── number ───────────────────────────────────────────────────────────────────

  private def parseNumber(): JsonNum = {
    val start = pos
    if (pos < input.length && input.charAt(pos) == '-') pos += 1
    if (pos >= input.length || !input.charAt(pos).isDigit)
      throw JsonParseException(s"Invalid number at position $start")
    while (pos < input.length && input.charAt(pos).isDigit) pos += 1
    if (pos < input.length && input.charAt(pos) == '.') {
      pos += 1
      if (pos >= input.length || !input.charAt(pos).isDigit)
        throw JsonParseException(s"Invalid number at position $start")
      while (pos < input.length && input.charAt(pos).isDigit) pos += 1
    }
    if (pos < input.length && (input.charAt(pos) == 'e' || input.charAt(pos) == 'E')) {
      pos += 1
      if (pos < input.length && (input.charAt(pos) == '+' || input.charAt(pos) == '-')) pos += 1
      if (pos >= input.length || !input.charAt(pos).isDigit)
        throw JsonParseException(s"Invalid number exponent at position $start")
      while (pos < input.length && input.charAt(pos).isDigit) pos += 1
    }
    val raw = input.substring(start, pos)
    try JsonNum(raw.toDouble)
    catch {
      case _: NumberFormatException =>
        throw JsonParseException(s"Invalid number: $raw")
    }
  }

  // ── array ────────────────────────────────────────────────────────────────────

  private def parseArray(): JsonArr = {
    expect('[')
    skipWs()
    if (pos < input.length && input.charAt(pos) == ']') { pos += 1; return JsonArr(Nil) }

    val items = mutable.ListBuffer[JsonValue]()
    items += parseValue()
    skipWs()
    while (pos < input.length && input.charAt(pos) == ',') {
      pos += 1
      skipWs()
      items += parseValue()
      skipWs()
    }
    expect(']')
    JsonArr(items.toList)
  }

  // ── object ───────────────────────────────────────────────────────────────────

  private def parseObject(): JsonObj = {
    expect('{')
    skipWs()
    if (pos < input.length && input.charAt(pos) == '}') { pos += 1; return JsonObj(Map.empty) }

    val fields = mutable.LinkedHashMap[String, JsonValue]()

    def readPair(): Unit = {
      skipWs()
      val key = parseString().value
      skipWs()
      expect(':')
      skipWs()
      fields(key) = parseValue()
    }

    readPair()
    skipWs()
    while (pos < input.length && input.charAt(pos) == ',') {
      pos += 1
      readPair()
      skipWs()
    }
    expect('}')
    JsonObj(fields.toMap)
  }
}
