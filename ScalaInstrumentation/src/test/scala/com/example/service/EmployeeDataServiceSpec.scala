package com.example.service

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import com.example.config.SparkSessionProvider
import com.example.DataFrameMetrics
import org.apache.spark.sql.{DataFrame, SparkSession}

import java.io.ByteArrayOutputStream

class EmployeeDataServiceSpec extends AnyWordSpec
  with Matchers
  with BeforeAndAfterAll
  with BeforeAndAfterEach {

  private var spark: SparkSession = _
  private var service: EmployeeDataService = _
  private var df: DataFrame = _

  override def beforeAll(): Unit = {
    super.beforeAll()
    spark = SparkSessionProvider.get
    spark.sparkContext.setLogLevel("ERROR")
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    service = new EmployeeDataService(spark)
    df = service.createEmployeeDataFrame()
  }

  override def afterAll(): Unit = {
    SparkSessionProvider.stop()
    super.afterAll()
  }

  "EmployeeDataService" when {

    "createEmployeeDataFrame is called" should {
      "return a DataFrame with 5 rows" in {
        df.count() shouldBe 5
      }

      "return a DataFrame with correct columns" in {
        df.columns should contain allOf("name", "age", "role")
      }

      "return a DataFrame with correct schema types" in {
        df.schema("name").dataType.typeName shouldBe "string"
        df.schema("age").dataType.typeName shouldBe "integer"
        df.schema("role").dataType.typeName shouldBe "string"
      }
    }

    "displayResults is called" should {
      "print the row count message" in {
        val metrics = DataFrameMetrics(rowCount = 5, columnCount = 3)

        val outCapture = new ByteArrayOutputStream()
        Console.withOut(outCapture) {
          service.displayResults(df, metrics)
        }

        val output = outCapture.toString
        output should include("DataFrame created with 5 rows")
      }

      "call df.show() without errors" in {
        val metrics = DataFrameMetrics(rowCount = 5, columnCount = 3)

        noException should be thrownBy {
          service.displayResults(df, metrics)
        }
      }
    }
  }
}