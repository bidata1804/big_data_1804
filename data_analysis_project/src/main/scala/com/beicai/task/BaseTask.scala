package com.beicai.task

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

/**
  * Created by 任景阳 on 2019/4/25.
  */
trait BaseTask {
       val conf = new SparkConf().setAppName(this.getClass.getSimpleName).setMaster("local")
       val spark = SparkSession.builder().config(conf).getOrCreate()
       val sc = spark.sparkContext
}
