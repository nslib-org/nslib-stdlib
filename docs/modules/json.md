# stdlib-json

Pure Scala JSON parsing and serialisation.
No external dependencies.  Works on JVM, Scala.js, and Scala Native.

## Quick start

```scala
libraryDependencies += "io.github.nslib-org" %% "stdlib-json" % "0.1.0"
```

```scala
import nslib._

val json = JSON.parse("""{"name":"Alice","age":30}""")
json("name").asString   // "Alice"
json("age").asInt       // 30
```

## API reference

### Parsing

```scala
JSON.parse(text: String): JsonValue
```
Parse a JSON string.  Throws `JsonParseException` on invalid input.

```scala
JSON.tryParse(text: String): Option[JsonValue]
```
Parse without throwing — returns `None` on failure.

### Accessing values

| Method | Returns | Throws if wrong type |
|--------|---------|----------------------|
| `v.asString` | `String` | `JsonAccessException` |
| `v.asInt` | `Int` | `JsonAccessException` |
| `v.asLong` | `Long` | `JsonAccessException` |
| `v.asDouble` | `Double` | `JsonAccessException` |
| `v.asBoolean` | `Boolean` | `JsonAccessException` |
| `v.asArray` | `List[JsonValue]` | `JsonAccessException` |
| `v.asMap` | `Map[String, JsonValue]` | `JsonAccessException` |
| `v("key")` | `JsonValue` | `JsonAccessException` if key missing |
| `v(0)` | `JsonValue` | `JsonAccessException` if out of bounds |
| `v.get("key")` | `Option[JsonValue]` | never |
| `v.isNull` | `Boolean` | never |

### Building

```scala
JSON.obj("key" -> value, ...)   // JsonValue (object)
JSON.arr(v1, v2, ...)           // JsonValue (array)
JSON.str("hello")               // JsonValue (string)
JSON.num(42)                    // JsonValue (number)
JSON.bool(true)                 // JsonValue (boolean)
JSON.`null`                     // JsonValue (null)
JSON.from(anyScalaValue)        // convert Scala → JsonValue
```

### Serialising

```scala
JSON.stringify(v)               // compact JSON string
JSON.stringify(v, indent = 2)   // pretty-printed JSON string
v.stringify                     // shorthand
v.stringify(indent = 4)
```

### Pattern matching

```scala
import JSON._
value match {
  case Obj(fields) => /* Map[String, JsonValue] */
  case Arr(items)  => /* List[JsonValue] */
  case Str(s)      => /* String */
  case Num(n)      => /* Double */
  case Bool(b)     => /* Boolean */
  case Null        => /* singleton */
}
```

## Value types

| Scala type | JSON type |
|------------|-----------|
| `JsonNull` (case object) | `null` |
| `JsonBool(value: Boolean)` | `true` / `false` |
| `JsonNum(value: Double)` | number |
| `JsonStr(value: String)` | string |
| `JsonArr(values: List[JsonValue])` | array |
| `JsonObj(fields: Map[String, JsonValue])` | object |

## Error types

| Exception | When thrown |
|-----------|-------------|
| `JsonParseException` | Invalid JSON input |
| `JsonAccessException` | Wrong type or missing key/index |

## Performance notes

The parser is a single-pass recursive descent with no dependencies.
It allocates a `StringBuilder` per string value and a `ListBuffer` per
array/object.  For very large payloads (> 10 MB), consider streaming
alternatives.
