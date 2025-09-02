package com.example

import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.trace.{Span, StatusCode}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.scheduler.{SparkListener, SparkListenerJobStart}

/**
 * Demonstrates integration between Apache Spark and OpenTelemetry tracing using Spark Listeners.
 *
 * This example shows how to:
 * - Create custom OpenTelemetry spans for Spark operations
 * - Use Spark Listeners to capture job execution events
 * - Combine automatic instrumentation with manual span creation
 * - Add custom attributes and events to traces
 */
object SimpleTracing {

  private val tracer = GlobalOpenTelemetry.getTracer("SimpleTracingApp")

  /**
   * Custom Spark Listener that captures job start events and adds them to OpenTelemetry spans.
   *
   * Key Concepts:
   * - Spark Listeners API: Provides hooks into Spark's internal execution lifecycle
   * - Event-driven architecture: Listeners respond automatically to Spark events
   * - Observability integration: Bridges Spark metrics with distributed tracing
   *
   * Available Listener Events:
   * - onJobStart/onJobEnd: Triggered when Spark jobs begin/complete
   * - onStageSubmitted/onStageCompleted: Stage-level execution events
   * - onTaskStart/onTaskEnd: Individual task execution events
   * - onApplicationStart/onApplicationEnd: Application lifecycle events
   *
   * @param span The OpenTelemetry span to add events and attributes to
   */
  class JobStartListener(span: Span) extends SparkListener {

    /**
     * Called automatically by Spark when a job starts executing.
     *
     * Note: This method is invoked by Spark's internal event system, not directly by user code.
     * The SparkListener pattern follows the Observer design pattern for loose coupling.
     *
     * @param jobStart Contains metadata about the starting job (ID, timestamp, stage info)
     */
    override def onJobStart(jobStart: SparkListenerJobStart): Unit = {
      // Add structured event to the trace timeline
      span.addEvent("Spark Job Started")

      // Capture job metadata as span attributes for filtering and analysis
      span.setAttribute("spark.job.id", jobStart.jobId)
      span.setAttribute("spark.job.submission_time", jobStart.time)
      span.setAttribute("spark.job.stage_count", jobStart.stageIds.length)

      // Console output for immediate visibility during development
      println(s"🚀 JOB STARTED! Job ID: ${jobStart.jobId} at time: ${jobStart.time}")
    }
  }

  /**
   * Spark session configured for local execution.
   *
   * Configuration Notes:
   * - master("local[*]"): Uses all available CPU cores on local machine
   * - For production: use cluster managers like YARN, Kubernetes, or Spark Standalone
   * - Session is singleton pattern - reuse across operations for efficiency
   */
  private val spark = SparkSession.builder()
    .appName("SimpleTracingApp")
    .master("local[*]")
    .getOrCreate()

  import spark.implicits._

  /**
   * Demonstrates DataFrame operations with integrated tracing and monitoring.
   *
   * Tracing Strategy:
   * 1. Create span before any Spark operations begin
   * 2. Register listener to capture automatic Spark events
   * 3. Add manual events for business logic milestones
   * 4. Include custom attributes for operational metrics
   * 5. Ensure proper span lifecycle management (start -> end)
   */
  def createAndCountDataFrame(): Unit = {
    // Create trace span to track the entire DataFrame operation
    val span = tracer.spanBuilder("spark-dataframe-processing").startSpan()

    // Register listener AFTER span creation to ensure proper context
    spark.sparkContext.addSparkListener(new JobStartListener(span))

    try {
      // Make span active for this thread (enables automatic context propagation)
      span.makeCurrent()

      // Manual event: Business logic milestone
      span.addEvent("Creating DataFrame")

      // Sample dataset representing employee information
      val employeeData = Seq(
        ("Alice", 25, "Engineer"),
        ("Bob", 30, "Manager"),
        ("Charlie", 35, "Analyst"),
        ("Diana", 28, "Designer"),
        ("Eve", 32, "Developer")
      )

      // Convert to DataFrame with typed columns
      val employeesDF: DataFrame = employeeData.toDF("name", "age", "role")

      span.addEvent("DataFrame created")

      // Trigger Spark action - this will fire the JobStartListener
      // Note: count() is an "action" (not "transformation") so it executes immediately
      val totalEmployees = employeesDF.count()

      // Custom business metrics as span attributes
      // These enable filtering and aggregation in observability platforms
      span.setAttribute("dataframe.row_count", totalEmployees)
      span.setAttribute("dataframe.columns", employeesDF.columns.length)
      span.addEvent("Row count calculated")

      // Display results
      println(s"DataFrame created with $totalEmployees rows")
      employeesDF.show()

      // Mark span as successful
      span.setStatus(StatusCode.OK)

    } catch {
      case ex: Exception =>
        // Capture exception details for debugging
        span.recordException(ex)
        span.setStatus(StatusCode.ERROR, "DataFrame processing failed")
        throw ex
    } finally {
      // Critical: Always end spans to prevent memory leaks and ensure data export
      span.end()
    }
  }

  /**
   * Application entry point with proper resource management.
   *
   * Resource Management Notes:
   * - Spark sessions consume significant memory and should be properly closed
   * - Use try-finally blocks to ensure cleanup even if exceptions occur
   * - In production, consider using connection pools or session management frameworks
   */
  def main(args: Array[String]): Unit = {
    println("Processing Spark DataFrame with OpenTelemetry tracing...")

    try {
      createAndCountDataFrame()
    } finally {
      // Essential cleanup: prevents resource leaks and hanging processes
      spark.stop()
    }
  }
}

/*
 * LEARNING NOTES - Spark Listeners and OpenTelemetry Integration
 *
 * 1. SPARK LISTENERS ARCHITECTURE:
 *    - Event-driven system for monitoring Spark applications
 *    - Extends SparkListener abstract class and override specific event methods
 *    - Registered with SparkContext.addSparkListener()
 *    - Runs asynchronously to avoid impacting Spark performance
 *
 * 2. OPENTELEMETRY CONCEPTS:
 *    - Span: Represents a single operation in a trace (has start/end times)
 *    - Trace: Collection of spans representing a complete request flow
 *    - Attributes: Key-value metadata attached to spans for filtering/analysis
 *    - Events: Timestamped log entries within a span's lifecycle
 *
 * 3. INTEGRATION PATTERNS:
 *    - Manual Instrumentation: Explicitly create spans around business operations
 *    - Automatic Instrumentation: Use agents/libraries to capture framework events
 *    - Hybrid Approach: Combine both for comprehensive observability
 *
 * 4. BEST PRACTICES:
 *    - Always end spans to prevent memory leaks
 *    - Use structured attributes instead of string concatenation in event names
 *    - Register listeners before triggering operations they should monitor
 *    - Include business-relevant metrics alongside technical metrics
 *    - Use consistent naming conventions for spans and attributes
 *
 * 5. PRODUCTION CONSIDERATIONS:
 *    - Listener overhead: Keep processing lightweight to avoid performance impact
 *    - Sampling: Use trace sampling to control data volume in high-throughput systems
 *    - Error handling: Ensure listener failures don't crash Spark applications
 *    - Security: Avoid logging sensitive data in span attributes or events
 */