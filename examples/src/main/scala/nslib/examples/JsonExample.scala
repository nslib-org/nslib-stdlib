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

  val json = JSON.parse(text)

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

  val data = JSON.obj(
    "user"    -> JSON.obj(
      "name"  -> JSON.str("Bob"),
      "roles" -> JSON.arr(JSON.str("admin"), JSON.str("editor")),
    ),
    "count"  -> JSON.num(42),
    "active" -> JSON.bool(true),
    "note"   -> JSON.`null`,
  )

  // ── Serialise ─────────────────────────────────────────────────────────

  println(JSON.stringify(data))          // compact
  println(JSON.stringify(data, indent = 2)) // pretty

  // ── Convert from Scala ─────────────────────────────────────────────────

  val fromMap = JSON.from(Map("x" -> 1, "y" -> 2))
  println(JSON.stringify(fromMap)) // {"x":1,"y":2}

  // ── Pattern matching ───────────────────────────────────────────────────

  import JSON._
  json match {
    case Obj(fields) => println(s"Object with keys: ${fields.keys.mkString(", ")}")
    case _           => println("not an object")
  }

  json("scores") match {
    case Arr(items) => println(s"${items.length} scores")
    case _          => ()
  }
}
