package com.example

import io.opentelemetry.api.trace.Span
import org.apache.spark.scheduler.{SparkListener, SparkListenerJobStart}

/**
 * Custom Spark Listener that captures job start events and adds them to OpenTelemetry spans.
 */
class JobStartListener(span: Span) extends SparkListener {

  override def onJobStart(jobStart: SparkListenerJobStart): Unit = {
    span.addEvent("Spark Job Started")
    span.setAttribute("spark.job.id", jobStart.jobId)
    span.setAttribute("spark.job.submission_time", jobStart.time)
    span.setAttribute("spark.job.stage_count", jobStart.stageIds.length)
    println(s"JOB STARTED! Job ID: ${jobStart.jobId} at time: ${jobStart.time}")
  }
}