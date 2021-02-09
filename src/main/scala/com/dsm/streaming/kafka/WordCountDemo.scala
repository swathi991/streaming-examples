package com.dsm.streaming.kafka

import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{explode, split}

object WordCountDemo {
  def main(args: Array[String]): Unit = {
    val sparkSession = SparkSession.builder().master("local[*]").appName("Crime Data Stream").getOrCreate()
    sparkSession.sparkContext.setLogLevel("ERROR")
    import sparkSession.implicits._

    val rootConfig = ConfigFactory.load("application.conf").getConfig("conf")
    val kafkaConfig = rootConfig.getConfig("kafka_conf")

    val inputDf = sparkSession
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", s"${kafkaConfig.getString("kafka_bootstrap_servers")}:9092")
      .option("subscribe", kafkaConfig.getString("topic"))
      .option("startingOffsets", "earliest")
      .load()

    val consoleOutput = inputDf
      .selectExpr("CAST(value AS STRING)")
      .withColumn("value", split($"value", " "))
      .withColumn("value", explode($"value"))
      .groupBy("value").agg("value" -> "count")
      .writeStream
      .outputMode("complete")
      .format("console")
      .start()

    consoleOutput.awaitTermination()
  }
}
