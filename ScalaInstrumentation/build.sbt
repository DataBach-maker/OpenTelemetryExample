ThisBuild / organization := "com.example"
ThisBuild / scalaVersion := "2.13.12"

val openTelemetryVersion = "1.30.1"
val sparkVersion = "3.5.0"

lazy val root = (project in file("."))
  .settings(
    name := "simple-tracing",
    version := "0.1.0-SNAPSHOT",

    // Explicitly set main class
    Compile / mainClass := Some("com.example.SimpleTracing"),
    run / mainClass := Some("com.example.SimpleTracing"),

    libraryDependencies ++= Seq(
      // OpenTelemetry API for custom spans
      "io.opentelemetry" % "opentelemetry-api" % openTelemetryVersion,

      // Apache Spark
      "org.apache.spark" %% "spark-core" % sparkVersion,
      "org.apache.spark" %% "spark-sql" % sparkVersion,

      // Testing
      "org.scalatest" %% "scalatest" % "3.2.17" % Test
    )
  )