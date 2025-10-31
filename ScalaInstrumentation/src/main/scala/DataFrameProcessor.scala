package com.example

import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.trace.{Span, StatusCode}
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
 * Handles DataFrame operations with integrated tracing.
 */
object DataFrameProcessor {

  private val tracer = GlobalOpenTelemetry.getTracer("SimpleTracingApp")

  def processEmployeeData(): Unit = SparkSessionProvider.withSession { spark =>
    import spark.implicits._

    val span = tracer.spanBuilder("spark-dataframe-processing").startSpan()
    spark.sparkContext.addSparkListener(new JobStartListener(span))

    try {
      span.makeCurrent()
      span.addEvent("Creating DataFrame")

      val employeeData = Seq(
        ("Alice", 25, "Engineer"),
        ("Bob", 30, "Manager"),
        ("Charlie", 35, "Analyst"),
        ("Diana", 28, "Designer"),
        ("Eve", 32, "Developer")
      )

      val employeesDF: DataFrame = employeeData.toDF("name", "age", "role")
      span.addEvent("DataFrame created")

      val totalEmployees = employeesDF.count()

      span.setAttribute("dataframe.row_count", totalEmployees)
      span.setAttribute("dataframe.columns", employeesDF.columns.length)
      span.addEvent("Row count calculated")

      println(s"DataFrame created with $totalEmployees rows")
      employeesDF.show()

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
}