package com.dsm.streaming.kafka

import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.SparkSession

object KafkaConsumerDemo {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .master("local[*]")
      .appName("Crime Data Stream")
      .getOrCreate()
    spark.sparkContext.setLogLevel("ERROR")

    val rootConfig = ConfigFactory.load("application.conf").getConfig("conf")
    val kafkaConfig = rootConfig.getConfig("kafka_conf")

    val inputDf = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", s"${kafkaConfig.getString("kafka_bootstrap_servers")}:9092")
      .option("subscribe", kafkaConfig.getString("topic"))
      .option("startingOffsets", "earliest")
      .load()

    val consoleOutput = inputDf
      .selectExpr("CAST(value AS STRING)")   // .selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
      .writeStream
      .outputMode("append")
      .format("console")
      .start()
    consoleOutput.awaitTermination()
  }
}
