package com.beicai.task.etl

import com.beicai.bean.IPRule
import com.beicai.common.AnalysisLog
import com.beicai.config.ConfigurationManager
import com.beicai.constants.{GlobalConstants, LogConstants}
import com.beicai.task.BaseTask
import com.beicai.util.Utils
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapred.TableOutputFormat
import org.apache.hadoop.mapred.JobConf
import org.apache.spark.SparkException
import org.apache.spark.rdd.RDD

import scala.collection.mutable

/**
  * Created by 任景阳 on 2019/4/25.
  */
object AnalysisLogTask extends  BaseTask{
  var inputDate: String = null
  var inputPath: String = null
  //输入记录数的累加器
  val inputRecordAccumulator = sc.longAccumulator("inputRecordAccumulator")
  //过滤记录数累加器
  val filterRecordAccumulator = sc.longAccumulator("filterRecordAccumulator")
  /**
    * 验证参数是否正确
    * 1,验证参数的个数  >=1
    * 2,验证参数的各是 yyyy-MM-dd
    *
    * @param args
    */
  def validateInputArgs(args: Array[String]) = {
    if(args.length == 0){
      throw new SparkException(
        """
          |Usage:com.daoke360.task.etl.AnalysisLogTask
          |errorMessage:任务至少需要有一个日期参数
        """.stripMargin)
    }
    if(!Utils.validateInputDate(args(0))){
      throw new SparkException(
        """
          |Usage:com.daoke360.task.etl.AnalysisLogTask
          |errorMessage:任务第一个参数是一个日期，日期的格式是：yyyy-MM-dd
        """.stripMargin)
    }
    inputDate = args(0)


  }
  /**
    * 2,验证当天是否存在用户行为日志
    * /logs/2019/04/24/xx.log
    * 2019-04-24===Long类型时间戳===>2019/04/24==>/logs/logs/2019/04/24==>验证这个路径在hdfs上是否存在
    */
  def validateExistsLog() : Unit ={
    inputPath = ConfigurationManager.getValue(GlobalConstants.CONFIG_LOG_PATH_PREFIX) +
                 Utils.formatDate(Utils.parseDate(inputDate,"yyyy-MM-dd"),"yyyy/MM/dd")
    var fileSystem: FileSystem = null
    try{
    fileSystem = FileSystem.newInstance(configuration)
      if(!fileSystem.exists(new Path(inputPath))){
        throw new SparkException(
          s"""
             |Usage:com.daoke360.task.etl.AnalysisLogTask
             |errorMessage:指定的日期${inputDate},不存在需要解析的用户行为日志
         """.stripMargin)
      }
    }catch {
      case e: Exception => e.printStackTrace()
    }finally {
      if(fileSystem!=null){
        fileSystem.close()
      }
    }

  }



  /**
    * 3.使用spark加载ip规则
    */
  def loadIPRule()  ={
    val ipRuleArray: Array[IPRule] = sc.textFile(ConfigurationManager.getValue(GlobalConstants.CONFIG_IP_RULE_DATA_PATH),2).map(line =>{
      //1.0.1.0|1.0.3.255|16777472|16778239|亚洲|中国|福建|福州||电信|350100|China|CN|119.306239|26.075302
      val fields = line.split("[|]")
      IPRule(fields(2).toLong, fields(3).toLong, fields(5), fields(6), fields(7))

    }).collect()
    ipRuleArray
  }

  /**
    * 4,使用spark加载用户行为日志，进行解析
    *
    */
  private def loadLogFromHdfs(ipRuleArray: Array[IPRule]) = {
    val iPRulesBroadcast = sc.broadcast(ipRuleArray)
    val logRDD =  sc.textFile(inputPath,4).map(logText =>{
      inputRecordAccumulator.add(1)
      AnalysisLog.analysisLog(logText,iPRulesBroadcast.value)
    }).filter(x => {
      if(x != null){
        true
      }else{
        filterRecordAccumulator.add(1)
        false
      }
    })
    logRDD
  }

  /**
    * 将解析好的日志，保存到hbase上
    *
    *
    */
  private def saveLogToHbase(logRDD: RDD[mutable.Map[String, String]]): Unit = {
      val jobConf = new JobConf(configuration)
    //指定使用那个类将数据写入到hbase中
    jobConf.setOutputFormat(classOf[TableOutputFormat])
    //指定目标表
    jobConf.set(TableOutputFormat.OUTPUT_TABLE,LogConstants.HBASE_LOG_TABLE_NAME)
    logRDD.map(f = map => {
      //map==>put
      //构建rowKey 唯一，散列，不能过长，满足业务查询需要 accessTime+"_"+(uid+eventName).hascode
      val accessTime = map(LogConstants.LOG_COLUMNS_NAME_ACCESS_TIME)
      val uid = map(LogConstants.LOG_COLUMNS_NAME_UID)
      val event_name = map(LogConstants.LOG_COLUMNS_NAME_EVENT_NAME)
      val rowKey = accessTime + "_" + Math.abs((uid + event_name).hashCode)
      val put = new Put(rowKey.getBytes())
      map.foreach(t2 => {
        val key = t2._1
        val value = t2._2
        put.addColumn(LogConstants.HBASE_LOG_TABLE_FAMILY.getBytes(),key.getBytes(),value.getBytes())
      })
      (new ImmutableBytesWritable(),put)
    }).saveAsHadoopDataset(jobConf)
  }

  def main(args: Array[String]): Unit = {
    // 1,验证参数是否正确
    validateInputArgs(args)
    // 2,验证当天是否存在用户行为日志
    validateExistsLog()
    // 3,使用spark加载ip规则
    val ipRuleArray = loadIPRule()
    // 4,使用spark加载用户行为日志，进行解析[
    val logRDD = loadLogFromHdfs(ipRuleArray)
    logRDD.cache()
    // 5,将解析好的日志，保存到hbase上
    saveLogToHbase(logRDD)
    println(s"本次输入日志记录数：${inputRecordAccumulator.value}条，过滤日志记录数：${filterRecordAccumulator.value}条")
    sc.stop()
  }
}
