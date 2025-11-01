package com.example.service

import org.apache.spark.sql.{DataFrame, SparkSession}
import com.example.spark.DataFrameMetrics

class EmployeeDataService(spark: SparkSession) {
  import spark.implicits._

  def createEmployeeDataFrame(): DataFrame = {
    val employeeData = Seq(
      ("Alice", 25, "Engineer"),
      ("Bob", 30, "Manager"),
      ("Charlie", 35, "Analyst"),
      ("Diana", 28, "Designer"),
      ("Eve", 32, "Developer")
    )

    employeeData.toDF("name", "age", "role")
  }

  def displayResults(df: DataFrame, metrics: DataFrameMetrics): Unit = {
    println(s"DataFrame created with ${metrics.rowCount} rows")
    df.show()
  }
}