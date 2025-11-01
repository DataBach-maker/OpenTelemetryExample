package com.example.spark

import org.apache.spark.sql.DataFrame

case class DataFrameMetrics(rowCount: Long, columnCount: Int)

object DataFrameMetrics {
  def calculate(df: DataFrame): DataFrameMetrics = {
    DataFrameMetrics(
      rowCount = df.count(),
      columnCount = df.columns.length
    )
  }
}