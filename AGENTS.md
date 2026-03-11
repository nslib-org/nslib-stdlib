# AGENTS.md — nslib stdlib maintenance guide

This file describes conventions and rules for maintaining this repository.
It is the primary reference for Claude Code and Claude GitHub Actions.

---

## Repository layout

```
build.sbt               — single build file, all module definitions
project/
  build.properties      — sbt version
  plugins.sbt           — sbt plugins
  Versions.scala        — all library/tool version constants
modules/
  json/                 — stdlib-json  (CrossType.Pure — shared/ only)
  io/                   — stdlib-io    (CrossType.Full — shared/ + jvm/ + js/ + native/)
  http/                 — stdlib-http  (CrossType.Full — same as io)
  all/                  — stdlib-all   (CrossType.Pure — umbrella, no logic)
  config/               — placeholder (future)
  db/                   — placeholder (future)
  csv/                  — placeholder (future)
  cli/                  — placeholder (future)
  testkit/              — placeholder (future)
examples/               — runnable JVM examples
docs/modules/           — per-module Markdown docs
.github/
  workflows/ci.yml      — CI on push / PR
  workflows/release.yml — publish on tag
  workflows/claude.yml  — Claude maintenance via @claude mentions
```

---

## Core philosophy

**Simple things must be extremely simple.**

Before adding anything, ask: *Can a user understand this from one example?*
If not, the API is too complex.

Do **not** expose:
- monads, type classes, effect systems, streams
- terms like `transactor`, `backend`, `resource`, `Kleisli`, `Free`
- complex implicit chains

Do **not** add:
- dependencies on Cats, ZIO, Monix, fs2, or similar
- heavy Java library wrappers (java.net.http on JVM is fine; the Netty stack is not)

---

## Naming conventions

| Symbol | Convention | Example |
|--------|-----------|---------|
| Package | `nslib` (top-level public) | `nslib.JSON` |
| Internal package | `nslib.<module>.internal` | `nslib.json.internal` |
| Public objects | UpperCamelCase nouns | `Json`, `IO`, `Http` |
| Public methods | lowerCamelCase verbs | `parse`, `readString`, `postJson` |
| Exception types | `<Module>Exception` | `JsonParseException`, `HttpException` |
| Test classes | `<Subject>Test` | `JSONTest`, `IOTest` |

---

## Coding conventions

- All source files use UTF-8 with Unix line endings.
- Max line length: 100 characters (enforced by Scalafmt).
- Run `sbt scalafmtAll` before committing.
- No `var` outside performance-critical internals (parsers, printers).
- No `null` in public API; use `Option` or throw a typed exception.
- All public methods must have a Scaladoc comment.
- Tests use munit `FunSuite`.
- Test file lives next to the module it tests (same package, `src/test/`).

---

## Cross-platform rules

| Module | JVM | Scala.js | Scala Native |
|--------|-----|----------|--------------|
| stdlib-json | ✅ full | ✅ full | ✅ full |
| stdlib-io   | ✅ full | ⚠️ stub | ✅ full (java.io) |
| stdlib-http | ✅ full | ⚠️ stub | ⚠️ stub |

- **Stubs** throw `UnsupportedOperationException` with a helpful message.
- Never silently swallow failures on unsupported platforms.
- When adding a platform implementation, remove the stub and add tests.

---

## Compatibility policy

- **Patch** (`0.1.x`): Bug fixes only.  No API changes.
- **Minor** (`0.x.0`): Backward-compatible additions.  No removals.
- **Major** (`x.0.0`): Breaking changes allowed, with migration guide.

Before removing or changing a public method:
1. Deprecate it with `@deprecated("use X instead", since = "0.y.0")`.
2. Keep it for at least one minor version.
3. Remove in the next major version.

---

## Testing rules

- Every public method needs at least one test.
- Tests must be deterministic (no random, no time-dependent without mocking).
- Integration tests that require network access must be gated with:
  ```scala
  assume(sys.env.contains("INTEGRATION_TESTS"), "skipped without INTEGRATION_TESTS=1")
  ```
- Run full JVM test suite: `sbt jsonJVM/test ioJVM/test httpJVM/test`
- Run Scala.js tests: `sbt jsonJS/test`
- Run all: `sbt test`

---

## Documentation policy

Every public module must have:
1. Scaladoc on the companion object with a `== Quick start ==` section.
2. A copy-paste example that works without modification.
3. An entry in `docs/modules/<module>.md`.
4. A section in `README.md`.

Examples must be realistic, not "foo/bar" placeholders.

---

## Adding a new module

1. Create `modules/<name>/` directory.
2. Add to `build.sbt` following the existing pattern (crossProject for cross-platform, project for JVM-only).
3. Implement the public object at `nslib.<Name>` (e.g. `nslib.CSV`).
4. Add a stub for unsupported platforms (throw `UnsupportedOperationException`).
5. Write tests.
6. Update `modules/all/src/main/scala/nslib/All.scala` to reference the new module.
7. Add the new module as a dependency of `stdlib-all` in `build.sbt`.
8. Add `docs/modules/<name>.md`.
9. Update `README.md`.

---

## Issue triage rules

| Label | Criteria |
|-------|----------|
| `bug` | Observed behaviour differs from documented behaviour |
| `enhancement` | New API or changed behaviour |
| `documentation` | Docs missing, wrong, or confusing |
| `platform` | Platform-specific (JS, Native) issue |
| `good first issue` | Small, self-contained, well-specified |
| `breaking` | Requires a major version bump |

Close without action if:
- The feature violates the "no FP jargon" principle.
- The feature is out of scope (frameworks, effect systems, advanced abstractions).
- Duplicate (link to original).

---

## PR expectations

All PRs must:
- Fill in the PR template completely.
- Pass CI (all checks green).
- Include tests for new or changed behaviour.
- Update Scaladoc and `docs/modules/` if applicable.
- Format code with `sbt scalafmtAll`.

---

## How Claude should approach changes

1. **Read this file first.**
2. **Run `sbt compile` to verify the build compiles** before making changes.
3. **Run the relevant test suite** (`sbt jsonJVM/test` etc.) after changes.
4. **Keep diffs minimal** — change only what is needed.
5. **Ask before breaking changes** — propose a deprecation path.
6. **Never remove tests** — only add or update them.
7. When uncertain, add a comment in the code and mention it in the PR.
