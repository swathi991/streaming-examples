package com.dsm.streaming.db

import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.SparkSession

object Test {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder.master("local").appName("Test").getOrCreate()

    val rootConfig = ConfigFactory.load("application.conf").getConfig("conf")
    val s3Config = rootConfig.getConfig("s3_conf")

    spark.sparkContext.hadoopConfiguration.set("fs.s3n.awsAccessKeyId", s3Config.getString("access_key"))
    spark.sparkContext.hadoopConfiguration.set("fs.s3n.awsSecretAccessKey", s3Config.getString("secret_access_key"))

    //spark.sparkContext.parallelize(List(1, 2, 3)).collect().foreach(println)
    val df = spark.read.csv("s3n://sid1234567sid/droplocation")
    println(df.count())
    spark.close()
  }
}
