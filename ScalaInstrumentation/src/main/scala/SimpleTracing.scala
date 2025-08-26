package com.example

import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.trace.{Span, StatusCode}
import org.apache.spark.sql.{SparkSession, DataFrame}

object SimpleTracing {

  private val tracer = GlobalOpenTelemetry.getTracer("spark-app")

  // Initialize Spark session
  private val spark = SparkSession.builder()
    .appName("SimpleTracingApp")
    .master("local[*]")
    .getOrCreate()

  import spark.implicits._

  def createAndCountDataFrame(): Unit = {
    val span = tracer.spanBuilder("spark-dataframe-processing").startSpan()

    try {
      span.makeCurrent()

      span.addEvent("Creating DataFrame")

      // Create a simple DataFrame with sample data
      val data = Seq(
        ("Alice", 25, "Engineer"),
        ("Bob", 30, "Manager"),
        ("Charlie", 35, "Analyst"),
        ("Diana", 28, "Designer"),
        ("Eve", 32, "Developer")
      )

      val df: DataFrame = data.toDF("name", "age", "role")

      span.addEvent("DataFrame created")

      // Count the rows
      val rowCount = df.count()

      // Add the row count to the trace
      span.setAttribute("dataframe.row_count", rowCount)
      span.addEvent("Row count calculated")

      println(s"DataFrame created with $rowCount rows")
      df.show()

      span.setStatus(StatusCode.OK)

    } catch {
      case ex: Exception =>
        span.recordException(ex)
        span.setStatus(StatusCode.ERROR, "DataFrame processing failed")
        throw ex
    } finally {
      span.end()
    }
  }

  def main(args: Array[String]): Unit = {
    println("Processing Spark DataFrame...")

    try {
      createAndCountDataFrame()
    } finally {
      // Clean up Spark session
      spark.stop()
    }
  }
}