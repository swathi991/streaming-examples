package com.dsm.streaming.file

import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.{StringType, StructField, StructType}

object SqlDemo {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().master("local[*]").appName("Crime Data Stream").getOrCreate()
    spark.sparkContext.setLogLevel("ERROR")

    val rootConfig = ConfigFactory.load("application.conf").getConfig("conf")
    val s3Config = rootConfig.getConfig("s3_conf")

    spark.sparkContext.hadoopConfiguration.set("fs.s3n.awsAccessKeyId", s3Config.getString("access_key"))
    spark.sparkContext.hadoopConfiguration.set("fs.s3n.awsSecretAccessKey", s3Config.getString("secret_access_key"))

    val dataPath = s"s3n://${s3Config.getString("s3_bucket")}/droplocation"
    val schema = StructType(
      StructField("city_code", StringType, true) ::
        StructField("city", StringType, true) ::
        StructField("major_category", StringType, true) ::
        StructField("minor_category", StringType, true) ::
        StructField("value", StringType, true) ::
        StructField("year", StringType, true) ::
        StructField("month", StringType, true) :: Nil)

    val fileStreamDF = spark.readStream
      .option("header", "true")
      .option("maxFilesPerTrigger", 1)  // Rate limiting
      .schema(schema)
      .csv(dataPath)

    fileStreamDF.createOrReplaceTempView("CrimeData")

    println("Is the stream ready?" + fileStreamDF.isStreaming)

    val categoryDF = spark.sql("SELECT major_category, value FROM CrimeData WHERE year = '2016'")


    val convictionsPerCategory = categoryDF.groupBy("major_category")
    .agg("value" -> "sum")
    .withColumnRenamed("sum(value)", "convictions")
    .orderBy("convictions")

    val query = convictionsPerCategory.writeStream
      .outputMode("complete")
      .format("console")
      .option("truncate", "false")
      .option("numRows", 30)
      .start()
      .awaitTermination()

    spark.close()

  }
}
