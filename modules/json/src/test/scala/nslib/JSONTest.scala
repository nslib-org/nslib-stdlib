package nslib

import munit.FunSuite

class JSONTest extends FunSuite {

  // ── parse: literals ──────────────────────────────────────────────────────────

  test("parse null") {
    assertEquals(JSON.parse("null"), JsonNull)
  }

  test("parse true") {
    assertEquals(JSON.parse("true"), JsonBool(true))
  }

  test("parse false") {
    assertEquals(JSON.parse("false"), JsonBool(false))
  }

  // ── parse: numbers ────────────────────────────────────────────────────────────

  test("parse integer") {
    assertEquals(JSON.parse("42"), JsonNum(42.0))
  }

  test("parse negative integer") {
    assertEquals(JSON.parse("-7"), JsonNum(-7.0))
  }

  test("parse float") {
    assertEquals(JSON.parse("3.14"), JsonNum(3.14))
  }

  test("parse exponent") {
    assertEquals(JSON.parse("1e2"), JsonNum(100.0))
    assertEquals(JSON.parse("1.5E-3"), JsonNum(0.0015))
  }

  test("parse zero") {
    assertEquals(JSON.parse("0"), JsonNum(0.0))
  }

  // ── parse: strings ────────────────────────────────────────────────────────────

  test("parse simple string") {
    assertEquals(JSON.parse("\"hello\""), JsonStr("hello"))
  }

  test("parse empty string") {
    assertEquals(JSON.parse("\"\""), JsonStr(""))
  }

  test("parse string with escapes") {
    assertEquals(JSON.parse("\"line1\\nline2\""), JsonStr("line1\nline2"))
    assertEquals(JSON.parse("\"tab\\there\""), JsonStr("tab\there"))
    assertEquals(JSON.parse("\"quote\\\"here\""), JsonStr("quote\"here"))
    assertEquals(JSON.parse("\"back\\\\slash\""), JsonStr("back\\slash"))
  }

  test("parse unicode escape") {
    assertEquals(JSON.parse("\"\\u0041\""), JsonStr("A"))
    assertEquals(JSON.parse("\"\\u4e2d\\u6587\""), JsonStr("中文"))
  }

  // ── parse: arrays ─────────────────────────────────────────────────────────────

  test("parse empty array") {
    assertEquals(JSON.parse("[]"), JsonArr(Nil))
  }

  test("parse array of numbers") {
    assertEquals(JSON.parse("[1, 2, 3]"), JsonArr(List(JsonNum(1), JsonNum(2), JsonNum(3))))
  }

  test("parse mixed array") {
    val v = JSON.parse("""[1, "two", true, null]""")
    assertEquals(v, JsonArr(List(JsonNum(1), JsonStr("two"), JsonBool(true), JsonNull)))
  }

  test("parse nested array") {
    val v = JSON.parse("[[1, 2], [3, 4]]")
    assertEquals(v(0)(1).asInt, 2)
  }

  // ── parse: objects ────────────────────────────────────────────────────────────

  test("parse empty object") {
    assertEquals(JSON.parse("{}"), JsonObj(Map.empty))
  }

  test("parse simple object") {
    val v = JSON.parse("""{"name": "Alice", "age": 30}""")
    assertEquals(v("name").asString, "Alice")
    assertEquals(v("age").asInt, 30)
  }

  test("parse nested object") {
    val v = JSON.parse("""{"user": {"id": 1, "email": "a@b.com"}}""")
    assertEquals(v("user")("email").asString, "a@b.com")
  }

  test("parse object with array value") {
    val v = JSON.parse("""{"tags": ["a", "b", "c"]}""")
    assertEquals(v("tags")(1).asString, "b")
  }

  // ── whitespace tolerance ──────────────────────────────────────────────────────

  test("parse with leading/trailing whitespace") {
    assertEquals(JSON.parse("  42  "), JsonNum(42.0))
    assertEquals(JSON.parse("\n{\n\"x\": 1\n}\n"), JsonObj(Map("x" -> JsonNum(1))))
  }

  // ── error cases ──────────────────────────────────────────────────────────────

  test("parse error: empty input") {
    intercept[JsonParseException](JSON.parse(""))
  }

  test("parse error: trailing garbage") {
    intercept[JsonParseException](JSON.parse("42 extra"))
  }

  test("parse error: unclosed array") {
    intercept[JsonParseException](JSON.parse("[1, 2"))
  }

  test("parse error: unclosed object") {
    intercept[JsonParseException](JSON.parse("{\"x\": 1"))
  }

  test("parse error: bad escape") {
    intercept[JsonParseException](JSON.parse("\"\\q\""))
  }

  test("tryParse returns None on failure") {
    assertEquals(JSON.tryParse("not json"), None)
    assert(JSON.tryParse("42").isDefined)
  }

  // ── access helpers ────────────────────────────────────────────────────────────

  test("asString throws on non-string") {
    intercept[JsonAccessException](JSON.parse("42").asString)
  }

  test("asInt throws on non-number") {
    intercept[JsonAccessException](JSON.parse("\"hi\"").asInt)
  }

  test("get returns None for missing key") {
    assertEquals(JSON.parse("{}").get("x"), None)
  }

  test("get returns Some for present key") {
    val v = JSON.parse("""{"k": 99}""")
    assertEquals(v.get("k"), Some(JsonNum(99.0)))
  }

  test("apply on array out of bounds throws") {
    intercept[JsonAccessException](JSON.parse("[1]").apply(5))
  }

  // ── stringify ─────────────────────────────────────────────────────────────────

  test("stringify null") {
    assertEquals(JSON.stringify(JsonNull), "null")
  }

  test("stringify boolean") {
    assertEquals(JSON.stringify(JsonBool(true)), "true")
    assertEquals(JSON.stringify(JsonBool(false)), "false")
  }

  test("stringify integer-valued number") {
    assertEquals(JSON.stringify(JsonNum(42.0)), "42")
  }

  test("stringify float") {
    assertEquals(JSON.stringify(JsonNum(3.14)), "3.14")
  }

  test("stringify string with escapes") {
    assertEquals(JSON.stringify(JsonStr("a\nb")), "\"a\\nb\"")
    assertEquals(JSON.stringify(JsonStr("q\"q")), "\"q\\\"q\"")
  }

  test("stringify compact object") {
    val v = JSON.obj("x" -> JSON.num(1), "y" -> JSON.str("hi"))
    val s = JSON.stringify(v)
    assert(s.contains("\"x\":1"))
    assert(s.contains("\"y\":\"hi\""))
  }

  test("stringify pretty object") {
    val v   = JSON.obj("a" -> JSON.num(1))
    val out = JSON.stringify(v, indent = 2)
    assert(out.contains("\n"))
    assert(out.contains("  \"a\": 1"))
  }

  test("stringify empty containers") {
    assertEquals(JSON.stringify(JSON.arr()), "[]")
    assertEquals(JSON.stringify(JSON.obj()), "{}")
  }

  // ── round-trip ────────────────────────────────────────────────────────────────

  test("round-trip complex JSON") {
    val original = """{"name":"Alice","scores":[10,20],"meta":{"active":true,"note":null}}"""
    val parsed   = JSON.parse(original)
    val reparsed = JSON.parse(JSON.stringify(parsed))
    assertEquals(parsed, reparsed)
  }

  // ── constructors ──────────────────────────────────────────────────────────────

  test("JSON.from converts Scala types") {
    assertEquals(JSON.from(42), JsonNum(42.0))
    assertEquals(JSON.from("hello"), JsonStr("hello"))
    assertEquals(JSON.from(true), JsonBool(true))
    assertEquals(JSON.from(null), JsonNull)
    assertEquals(JSON.from(None), JsonNull)
    assertEquals(JSON.from(Some("x")), JsonStr("x"))
    assertEquals(JSON.from(List(1, 2)), JsonArr(List(JsonNum(1), JsonNum(2))))
  }

  // ── pattern matching ──────────────────────────────────────────────────────────

  test("pattern matching with JSON extractors") {
    val v = JSON.parse("""{"ok":true}""")
    import JSON._
    v match {
      case Obj(fields) => assert(fields.contains("ok"))
      case _           => fail("expected object")
    }
  }
}
