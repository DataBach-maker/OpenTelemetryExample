package com.example

/**
 * Application entry point.
 */
object Main {
  def main(args: Array[String]): Unit = {
    println("Processing Spark DataFrame with OpenTelemetry tracing...")
    DataFrameProcessor.processEmployeeData()
  }
}