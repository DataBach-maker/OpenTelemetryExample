package com.example

import com.example.config.SparkSessionProvider
import com.example.listener.SparkListenerManager
import com.example.service.{EmployeeDataService, TracingService}

object Job {

  def process(): Unit = SparkSessionProvider.withSession { spark =>
    val tracingService = new TracingService("Job")
    val dataService = new EmployeeDataService(spark)
    val listenerManager = new SparkListenerManager(spark)

    tracingService.traceOperation("spark-dataframe-processing") { span =>
      listenerManager.registerJobListener(span)

      val employeesDF = dataService.createEmployeeDataFrame()
      val metrics = DataFrameMetrics.calculate(employeesDF)

      tracingService.recordMetrics(span, metrics)
    }
  }
}