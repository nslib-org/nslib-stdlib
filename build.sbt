import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}

// ─── Scala versions ────────────────────────────────────────────────────────────
val scala213 = Versions.scala213
val scala3   = Versions.scala3Lts
val supportedScalaVersions = List(scala213, scala3)

// ─── Project-wide settings ────────────────────────────────────────────────────
ThisBuild / organization     := "io.github.nslib-org"
ThisBuild / organizationName := "nslib-org"
ThisBuild / scalaVersion     := scala3
ThisBuild / homepage         := Some(url("https://github.com/nslib-org/nslib-stdlib"))
ThisBuild / licenses         := List("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0"))
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/nslib-org/nslib-stdlib"),
    "scm:git@github.com:nslib-org/nslib-stdlib.git"
  )
)
ThisBuild / developers := List(
  Developer("nslib-org", "nslib contributors", "", url("https://github.com/nslib-org"))
)
ThisBuild / versionScheme := Some("early-semver")

// ─── Shared settings ──────────────────────────────────────────────────────────
lazy val commonSettings = Seq(
  crossScalaVersions := supportedScalaVersions,
  scalacOptions ++= Seq(
    "-deprecation",
    "-unchecked",
    "-feature",
    "-encoding", "utf8",
  ),
  scalacOptions ++= {
    if (scalaVersion.value.startsWith("3."))
      Seq("-Wunused:all")
    else
      Seq("-Xlint:_,-missing-interpolator", "-Wunused:imports,privates,locals,implicits")
  },
  libraryDependencies += "org.scalameta" %%% "munit" % Versions.munit % Test,
  testFrameworks += new TestFramework("munit.Framework"),
)

lazy val publishSettings = Seq(
  publishMavenStyle          := true,
  Test / publishArtifact     := false,
  pomIncludeRepository       := { _ => false },
)

lazy val noPublish = Seq(
  publish / skip    := true,
  publishArtifact   := false,
)

// ─── stdlib-json ──────────────────────────────────────────────────────────────
// Pure Scala JSON: fully cross-platform (JVM / Scala.js / Scala Native)
lazy val json = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("modules/json"))
  .settings(commonSettings, publishSettings)
  .settings(
    name        := "stdlib-json",
    description := "Simple JSON parsing and serialisation for Scala",
  )
  .jsSettings(
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
  )

lazy val jsonJVM    = json.jvm
lazy val jsonJS     = json.js
lazy val jsonNative = json.native

// ─── stdlib-io ────────────────────────────────────────────────────────────────
// File I/O: full JVM + Native (java.io), stub on JS
lazy val io = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Full)
  .in(file("modules/io"))
  .settings(commonSettings, publishSettings)
  .settings(
    name        := "stdlib-io",
    description := "Simple file I/O utilities for Scala",
  )
  .jsSettings(
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
  )

lazy val ioJVM    = io.jvm
lazy val ioJS     = io.js
lazy val ioNative = io.native

// ─── stdlib-http ──────────────────────────────────────────────────────────────
// HTTP client: full JVM (java.net.http), stub on JS / Native
lazy val http = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Full)
  .in(file("modules/http"))
  .settings(commonSettings, publishSettings)
  .settings(
    name        := "stdlib-http",
    description := "Simple HTTP client for Scala",
  )
  .dependsOn(json)
  .jsSettings(
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
  )

lazy val httpJVM    = http.jvm
lazy val httpJS     = http.js
lazy val httpNative = http.native

// ─── stdlib-all ───────────────────────────────────────────────────────────────
// Umbrella: depend on this one artifact to get everything
lazy val all = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("modules/all"))
  .settings(commonSettings, publishSettings)
  .settings(
    name        := "stdlib-all",
    description := "All nslib stdlib modules in one dependency",
  )
  .dependsOn(json, io, http)
  .jsSettings(
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
  )

lazy val allJVM    = all.jvm
lazy val allJS     = all.js
lazy val allNative = all.native

// ─── Placeholder modules (future work) ────────────────────────────────────────
lazy val config = project.in(file("modules/config"))
  .settings(commonSettings, noPublish)
  .settings(name := "stdlib-config-placeholder", publish / skip := true)

lazy val db = project.in(file("modules/db"))
  .settings(commonSettings, noPublish)
  .settings(name := "stdlib-db-placeholder", publish / skip := true)

lazy val csv = project.in(file("modules/csv"))
  .settings(commonSettings, noPublish)
  .settings(name := "stdlib-csv-placeholder", publish / skip := true)

lazy val cli = project.in(file("modules/cli"))
  .settings(commonSettings, noPublish)
  .settings(name := "stdlib-cli-placeholder", publish / skip := true)

lazy val testkit = project.in(file("modules/testkit"))
  .settings(commonSettings, noPublish)
  .settings(name := "stdlib-testkit-placeholder", publish / skip := true)

// ─── Examples ─────────────────────────────────────────────────────────────────
lazy val examples = project
  .in(file("examples"))
  .dependsOn(allJVM)
  .settings(noPublish)
  .settings(
    name         := "nslib-examples",
    scalaVersion := scala3,
    // Examples are runnable as scripts
    run / fork := true,
  )

// ─── Root ─────────────────────────────────────────────────────────────────────
lazy val root = project
  .in(file("."))
  .aggregate(
    jsonJVM, jsonJS, jsonNative,
    ioJVM,   ioJS,   ioNative,
    httpJVM, httpJS, httpNative,
    allJVM,  allJS,  allNative,
    examples,
  )
  .settings(noPublish)
  .settings(
    name               := "nslib-stdlib-root",
    crossScalaVersions := Nil,
  )
