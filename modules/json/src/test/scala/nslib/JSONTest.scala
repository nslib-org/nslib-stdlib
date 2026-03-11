package nslib

import munit.FunSuite

class JSONTest extends FunSuite {

  // ── parse: literals ──────────────────────────────────────────────────────────

  test("parse null") {
    assertEquals(Json.parse("null"), JsonNull)
  }

  test("parse true") {
    assertEquals(Json.parse("true"), JsonBool(true))
  }

  test("parse false") {
    assertEquals(Json.parse("false"), JsonBool(false))
  }

  // ── parse: numbers ────────────────────────────────────────────────────────────

  test("parse integer") {
    assertEquals(Json.parse("42"), JsonNum(42.0))
  }

  test("parse negative integer") {
    assertEquals(Json.parse("-7"), JsonNum(-7.0))
  }

  test("parse float") {
    assertEquals(Json.parse("3.14"), JsonNum(3.14))
  }

  test("parse exponent") {
    assertEquals(Json.parse("1e2"), JsonNum(100.0))
    assertEquals(Json.parse("1.5E-3"), JsonNum(0.0015))
  }

  test("parse zero") {
    assertEquals(Json.parse("0"), JsonNum(0.0))
  }

  // ── parse: strings ────────────────────────────────────────────────────────────

  test("parse simple string") {
    assertEquals(Json.parse("\"hello\""), JsonStr("hello"))
  }

  test("parse empty string") {
    assertEquals(Json.parse("\"\""), JsonStr(""))
  }

  test("parse string with escapes") {
    assertEquals(Json.parse("\"line1\\nline2\""), JsonStr("line1\nline2"))
    assertEquals(Json.parse("\"tab\\there\""), JsonStr("tab\there"))
    assertEquals(Json.parse("\"quote\\\"here\""), JsonStr("quote\"here"))
    assertEquals(Json.parse("\"back\\\\slash\""), JsonStr("back\\slash"))
  }

  test("parse unicode escape") {
    assertEquals(Json.parse("\"\\u0041\""), JsonStr("A"))
    assertEquals(Json.parse("\"\\u4e2d\\u6587\""), JsonStr("中文"))
  }

  // ── parse: arrays ─────────────────────────────────────────────────────────────

  test("parse empty array") {
    assertEquals(Json.parse("[]"), JsonArr(Nil))
  }

  test("parse array of numbers") {
    assertEquals(Json.parse("[1, 2, 3]"), JsonArr(List(JsonNum(1), JsonNum(2), JsonNum(3))))
  }

  test("parse mixed array") {
    val v = Json.parse("""[1, "two", true, null]""")
    assertEquals(v, JsonArr(List(JsonNum(1), JsonStr("two"), JsonBool(true), JsonNull)))
  }

  test("parse nested array") {
    val v = Json.parse("[[1, 2], [3, 4]]")
    assertEquals(v(0)(1).asInt, 2)
  }

  // ── parse: objects ────────────────────────────────────────────────────────────

  test("parse empty object") {
    assertEquals(Json.parse("{}"), JsonObj(Map.empty))
  }

  test("parse simple object") {
    val v = Json.parse("""{"name": "Alice", "age": 30}""")
    assertEquals(v("name").asString, "Alice")
    assertEquals(v("age").asInt, 30)
  }

  test("parse nested object") {
    val v = Json.parse("""{"user": {"id": 1, "email": "a@b.com"}}""")
    assertEquals(v("user")("email").asString, "a@b.com")
  }

  test("parse object with array value") {
    val v = Json.parse("""{"tags": ["a", "b", "c"]}""")
    assertEquals(v("tags")(1).asString, "b")
  }

  // ── whitespace tolerance ──────────────────────────────────────────────────────

  test("parse with leading/trailing whitespace") {
    assertEquals(Json.parse("  42  "), JsonNum(42.0))
    assertEquals(Json.parse("\n{\n\"x\": 1\n}\n"), JsonObj(Map("x" -> JsonNum(1))))
  }

  // ── error cases ──────────────────────────────────────────────────────────────

  test("parse error: empty input") {
    intercept[JsonParseException](Json.parse(""))
  }

  test("parse error: trailing garbage") {
    intercept[JsonParseException](Json.parse("42 extra"))
  }

  test("parse error: unclosed array") {
    intercept[JsonParseException](Json.parse("[1, 2"))
  }

  test("parse error: unclosed object") {
    intercept[JsonParseException](Json.parse("{\"x\": 1"))
  }

  test("parse error: bad escape") {
    intercept[JsonParseException](Json.parse("\"\\q\""))
  }

  test("tryParse returns None on failure") {
    assertEquals(Json.tryParse("not json"), None)
    assert(Json.tryParse("42").isDefined)
  }

  // ── access helpers ────────────────────────────────────────────────────────────

  test("asString throws on non-string") {
    intercept[JsonAccessException](Json.parse("42").asString)
  }

  test("asInt throws on non-number") {
    intercept[JsonAccessException](Json.parse("\"hi\"").asInt)
  }

  test("get returns None for missing key") {
    assertEquals(Json.parse("{}").get("x"), None)
  }

  test("get returns Some for present key") {
    val v = Json.parse("""{"k": 99}""")
    assertEquals(v.get("k"), Some(JsonNum(99.0)))
  }

  test("apply on array out of bounds throws") {
    intercept[JsonAccessException](Json.parse("[1]").apply(5))
  }

  // ── stringify ─────────────────────────────────────────────────────────────────

  test("stringify null") {
    assertEquals(Json.stringify(JsonNull), "null")
  }

  test("stringify boolean") {
    assertEquals(Json.stringify(JsonBool(true)), "true")
    assertEquals(Json.stringify(JsonBool(false)), "false")
  }

  test("stringify integer-valued number") {
    assertEquals(Json.stringify(JsonNum(42.0)), "42")
  }

  test("stringify float") {
    assertEquals(Json.stringify(JsonNum(3.14)), "3.14")
  }

  test("stringify string with escapes") {
    assertEquals(Json.stringify(JsonStr("a\nb")), "\"a\\nb\"")
    assertEquals(Json.stringify(JsonStr("q\"q")), "\"q\\\"q\"")
  }

  test("stringify compact object") {
    val v = Json.obj("x" -> Json.num(1), "y" -> Json.str("hi"))
    val s = Json.stringify(v)
    assert(s.contains("\"x\":1"))
    assert(s.contains("\"y\":\"hi\""))
  }

  test("stringify pretty object") {
    val v   = Json.obj("a" -> Json.num(1))
    val out = Json.stringify(v, indent = 2)
    assert(out.contains("\n"))
    assert(out.contains("  \"a\": 1"))
  }

  test("stringify empty containers") {
    assertEquals(Json.stringify(Json.arr()), "[]")
    assertEquals(Json.stringify(Json.obj()), "{}")
  }

  // ── round-trip ────────────────────────────────────────────────────────────────

  test("round-trip complex Json") {
    val original = """{"name":"Alice","scores":[10,20],"meta":{"active":true,"note":null}}"""
    val parsed   = Json.parse(original)
    val reparsed = Json.parse(Json.stringify(parsed))
    assertEquals(parsed, reparsed)
  }

  // ── constructors ──────────────────────────────────────────────────────────────

  test("Json.from converts Scala types") {
    assertEquals(Json.from(42), JsonNum(42.0))
    assertEquals(Json.from("hello"), JsonStr("hello"))
    assertEquals(Json.from(true), JsonBool(true))
    assertEquals(Json.from(null), JsonNull)
    assertEquals(Json.from(None), JsonNull)
    assertEquals(Json.from(Some("x")), JsonStr("x"))
    assertEquals(Json.from(List(1, 2)), JsonArr(List(JsonNum(1), JsonNum(2))))
  }

  // ── pattern matching ──────────────────────────────────────────────────────────

  test("pattern matching with Json extractors") {
    val v = Json.parse("""{"ok":true}""")
    import Json.*
    v match {
      case Obj(fields) => assert(fields.contains("ok"))
      case _           => fail("expected object")
    }
  }
}
