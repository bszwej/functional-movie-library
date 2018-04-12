organization := "com.example"
name := "functional-movie-library"
version := "0.0.1"
scalaVersion := "2.12.5"

val Http4sVersion = "0.18.8"
val circeVersion = "0.9.3"
val scanamoVersion = "1.0.0-M3"
val pureConfigVersion = "0.9.1"
val refinedPureConfigVersion = "0.8.7"
val logbackVersion = "1.2.3"

val scalaTestVersion = "3.0.5"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
  "org.http4s" %% "http4s-circe" % Http4sVersion,
  "org.http4s" %% "http4s-dsl" % Http4sVersion,
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "com.gu" %% "scanamo" % scanamoVersion,
  "com.github.pureconfig" %% "pureconfig" % pureConfigVersion,
  "eu.timepit" %% "refined-pureconfig" % refinedPureConfigVersion,
  "ch.qos.logback" % "logback-classic" % logbackVersion,

  "org.scalatest" %% "scalatest" % scalaTestVersion % Test
)

dependencyOverrides ++=
  Seq(
    "org.log4s" %% "log4s" % "1.5.0",
    "org.typelevel" %% "cats-effect" % "0.10",
    "org.typelevel" %% "cats-core" % "1.1.0",
    "org.typelevel" %% "cats-macros" % "1.1.0"
  )

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "utf-8",
  "-explaintypes",
  "-feature",
  "-language:existentials",
  "-language:experimental.macros",
  "-language:implicitConversions",
  "-unchecked",
  "-Xcheckinit",
  "-Xfatal-warnings",
  "-Xfuture",
  "-Ypartial-unification",
)
