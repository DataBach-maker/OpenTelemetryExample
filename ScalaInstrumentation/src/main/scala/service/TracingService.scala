package com.example.service

import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.trace.{Span, StatusCode, Tracer}
import com.example.spark.DataFrameMetrics

class TracingService(tracerName: String) {
  private val tracer: Tracer = GlobalOpenTelemetry.getTracer(tracerName)

  def traceOperation[T](operationName: String)(block: Span => T): T = {
    val span = tracer.spanBuilder(operationName).startSpan()

    try {
      val scope = span.makeCurrent()
      try {
        val result = block(span)
        span.setStatus(StatusCode.OK)
        result
      } finally {
        scope.close()
      }
    } catch {
      case ex: Exception =>
        span.recordException(ex)
        span.setStatus(StatusCode.ERROR, s"$operationName failed")
        throw ex
    } finally {
      span.end()
      println(s"Span '$operationName' closed")
    }
  }

  def recordMetrics(span: Span, metrics: DataFrameMetrics): Unit = {
    span.setAttribute("dataframe.row_count", metrics.rowCount)
    span.setAttribute("dataframe.columns", metrics.columnCount)
    span.addEvent("Metrics recorded")
  }
}