package com.example.config

import org.apache.spark.sql.SparkSession

object SparkSessionProvider {

  private var _spark: Option[SparkSession] = None

  private def createSession(): SparkSession = {
    SparkSession.builder()
      .appName("SimpleTracingApp")
      .master("local[*]")
      .getOrCreate()
  }

  /**
   * Get the current SparkSession, creating it if it doesn't exist
   */
  def get: SparkSession = {
    _spark.getOrElse {
      val session = createSession()
      _spark = Some(session)
      session
    }
  }

  /**
   * Stop the current SparkSession if it exists
   */
  def stop(): Unit = {
    _spark.foreach { session =>
      session.stop()
      _spark = None
    }
  }

  def withSession[T](block: SparkSession => T): T = {
    val session = get
    try {
      block(session)
    } finally {
      stop()
    }
  }
}