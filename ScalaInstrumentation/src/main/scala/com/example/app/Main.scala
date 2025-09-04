package com.example.app

import com.example.tracing.Tracing
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.trace.{Span, SpanKind, StatusCode}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.scheduler.{SparkListener, SparkListenerJobStart}

/**
 * Main application demonstrating OpenTelemetry integration with Apache Spark.
 * 
 * This application creates a simple Spark job with OpenTelemetry tracing enabled,
 * showing how to instrument Spark operations and capture job execution events.
 */
object Main extends App with Tracing {

  override protected val tracer = GlobalOpenTelemetry.getTracer("example-use-case-name")
  
  // Initialize tracing with application span
  startApplicationSpan("example-use-case-service-name")
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
      addSpanEvent("spark.job.started")

      // Capture job metadata as span attributes for filtering and analysis
      addSpanAttribute("spark.job.id", jobStart.jobId)
      addSpanAttribute("spark.job.submission_time", jobStart.time)
      addSpanAttribute("spark.job.stage_count", jobStart.stageIds.length)
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
    .appName("MainApp")
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
  def job(): Unit = withSpan("create-and-count-dataframe") {
    // Register listener to track Spark job events
    spark.sparkContext.addSparkListener(new JobStartListener(currentSpan.get))
    
    // Add application info as span attributes
    addSpanEvent("dataframe.processing.started")

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

    addSpanEvent("dataframe.created")

    // Trigger Spark action - this will fire the JobStartListener
    // Note: count() is an "action" (not "transformation") so it executes immediately
    val totalEmployees = employeesDF.count()

    addSpanEvent("row.count.calculated")

    // Add custom attributes to the current span
    addSpanAttribute("dataframe.row_count", totalEmployees)
    addSpanAttribute("dataframe.column_count", employeesDF.columns.length)
  }

  /**
   * Application entry point with proper resource management.
   *
   * Resource Management Notes:
   * - Spark sessions consume significant memory and should be properly closed
   * - Use try-finally blocks to ensure cleanup even if exceptions occur
   * - In production, consider using connection pools or session management frameworks
   */
  withSpan("example-use-case-service-name") {
    job()
  }

  // Cleanup resources
  spark.stop()
  endApplicationSpan()
}