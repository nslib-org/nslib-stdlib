package nslib.examples

import nslib._

/** Run with: sbt "examples/runMain nslib.examples.JsonExample" */
object JsonExample extends App {

  // ── Parse ──────────────────────────────────────────────────────────────

  val text = """
    {
      "name":   "Alice",
      "age":    30,
      "active": true,
      "scores": [95, 87, 100],
      "address": {
        "city":    "Tokyo",
        "country": "Japan"
      },
      "nickname": null
    }
  """

  val json = Json.parse(text)

  println(json("name").asString)           // Alice
  println(json("age").asInt)               // 30
  println(json("active").asBoolean)        // true
  println(json("scores")(0).asInt)         // 95
  println(json("address")("city").asString) // Tokyo
  println(json("nickname").isNull)         // true
  println(json.get("missing"))             // None

  val scores: List[Int] = json("scores").asArray.map(_.asInt)
  println(scores) // List(95, 87, 100)

  // ── Build ──────────────────────────────────────────────────────────────

  val data = Json.obj(
    "user"    -> Json.obj(
      "name"  -> Json.str("Bob"),
      "roles" -> Json.arr(Json.str("admin"), Json.str("editor")),
    ),
    "count"  -> Json.num(42),
    "active" -> Json.bool(true),
    "note"   -> Json.`null`,
  )

  // ── Serialise ─────────────────────────────────────────────────────────

  println(Json.stringify(data))          // compact
  println(Json.stringify(data, indent = 2)) // pretty

  // ── Convert from Scala ─────────────────────────────────────────────────

  val fromMap = Json.from(Map("x" -> 1, "y" -> 2))
  println(Json.stringify(fromMap)) // {"x":1,"y":2}

  // ── Pattern matching ───────────────────────────────────────────────────

  import Json._
  json match {
    case Obj(fields) => println(s"Object with keys: ${fields.keys.mkString(", ")}")
    case _           => println("not an object")
  }

  json("scores") match {
    case Arr(items) => println(s"${items.length} scores")
    case _          => ()
  }
}
