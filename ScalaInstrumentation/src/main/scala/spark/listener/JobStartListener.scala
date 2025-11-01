package com.example.spark.listener

import io.opentelemetry.api.trace.Span
import org.apache.spark.scheduler.{SparkListener, SparkListenerJobStart}

/**
 * Custom Spark Listener that captures job start events and adds them to OpenTelemetry spans.
 *
 * This listener adds events to the provided span, which could be a parent or child span
 * depending on how it's used. This makes it flexible for different tracing hierarchies.
 */
class JobStartListener(span: Span) extends SparkListener {

  /**
   * Called automatically by Spark when a job starts executing.
   * Adds job metadata as events and attributes to the associated span.
   *
   * @param jobStart Contains metadata about the starting job
   */
  override def onJobStart(jobStart: SparkListenerJobStart): Unit = {
    // Add event to the span (works for both parent and child spans)
    span.addEvent("Spark Job Started")

    // Capture job metadata as span attributes
    span.setAttribute("spark.job.id", jobStart.jobId)
    span.setAttribute("spark.job.submission_time", jobStart.time)
    span.setAttribute("spark.job.stage_count", jobStart.stageIds.length)

    println(s"JOB STARTED! Job ID: ${jobStart.jobId} at time: ${jobStart.time}")
  }
}