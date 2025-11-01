package com.example.spark

import com.example.config.SparkSessionProvider
import com.example.service.{EmployeeDataService, TracingService}
import com.example.spark.listener.SparkListenerManager

object DataFrameProcessor {

  def processEmployeeData(): Unit = SparkSessionProvider.withSession { spark =>
    val tracingService = new TracingService("DataFrameProcessor")
    val dataService = new EmployeeDataService(spark)
    val listenerManager = new SparkListenerManager(spark)  // Add this

    tracingService.traceOperation("spark-dataframe-processing") { span =>
      listenerManager.registerJobListener(span)  // Add this

      val employeesDF = dataService.createEmployeeDataFrame()
      val metrics = DataFrameMetrics.calculate(employeesDF)

      tracingService.recordMetrics(span, metrics)
      dataService.displayResults(employeesDF, metrics)
    }
  }
}