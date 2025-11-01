package com.example.spark.listener

import io.opentelemetry.api.trace.Span
import org.apache.spark.sql.SparkSession

class SparkListenerManager(spark: SparkSession) {
  def registerJobListener(span: Span): Unit = {
    spark.sparkContext.addSparkListener(new JobStartListener(span))
  }
}