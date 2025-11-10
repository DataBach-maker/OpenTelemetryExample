package com.example

import com.example.config.SparkSessionProvider
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.apache.spark.sql.{DataFrame, SparkSession}

class DataFrameMetricsSpec extends AnyWordSpec
  with Matchers
  with BeforeAndAfterAll
  with BeforeAndAfterEach {

  private lazy val spark: SparkSession = SparkSessionProvider.get
  
  import spark.implicits._

  override def beforeAll(): Unit = {
    super.beforeAll()
    spark.sparkContext.setLogLevel("ERROR")
  }

  override def afterAll(): Unit = {
    SparkSessionProvider.stop()
    super.afterAll()
  }

  "DataFrameMetrics.calculate" should {

    "return correct metrics for a non-empty DataFrame" in {
      val testData = Seq(
        ("Alice", 30, "Engineer"),
        ("Bob", 25, "Analyst")
      )

      val df = testData.toDF("name", "age", "role")
      val metrics = DataFrameMetrics.calculate(df)

      metrics.rowCount shouldBe 2
      metrics.columnCount shouldBe 3
    }
  }
}