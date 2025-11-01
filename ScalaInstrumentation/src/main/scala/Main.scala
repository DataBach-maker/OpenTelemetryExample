package com.example

import spark.DataFrameProcessor

/**
 * Application entry point.
 */
object Main {

  def main(args: Array[String]): Unit = {
    println("Processing Spark DataFrame...")

    try {
      // Call business logic
      DataFrameProcessor.processEmployeeData()

    } catch {
      case ex: Exception =>
        throw ex
    }
  }
}