package com.beicai.common

import java.net.URLDecoder

import com.beicai.bean.IPRule
import com.beicai.constants.LogConstants
import org.apache.commons.lang.StringUtils

import scala.collection.mutable

/**
  * Created by 任景阳 on 2019/4/25.
  */
object AnalysisLog {
  /**
    * 将ip对应的地域信息，封装到logMap中
    *
    * @param logMap
    * @param ipRuleArray
    */
  private def handleIP(logMap:mutable.Map[String,String],ipRuleArray: Array[IPRule])={
      val ip = logMap(LogConstants.LOG_COLUMNS_NAME_IP)
      val regionInfo = AnalysisIP.getRegionInfoByIP(ip,ipRuleArray)
      logMap.put(LogConstants.LOG_COLUMNS_NAME_COUNTRY,regionInfo.country)
      logMap.put(LogConstants.LOG_COLUMNS_NAME_PROVINCE,regionInfo.province)
      logMap.put(LogConstants.LOG_COLUMNS_NAME_CITY,regionInfo.city)
  }

  /**
    * 解析收集的用户行为数据，添加到logMap中
    */

  def handleRequestParams(logMap: mutable.Map[String, String], requestParams: String) = {
      val fields = requestParams.split("[?]")
      if(fields.length == 2 && StringUtils.isNotBlank(fields(1))){
        val paramsText = fields(1)
        val items = paramsText.split("[&]")
        for(item <- items){
            val kv = item.split("[=]")
            val key = URLDecoder.decode(kv(0), "utf-8")
            val value = URLDecoder.decode(kv(1), "utf-8")
          logMap.put(key,value)
        }
      }
  }

  /**
    * 解析一条日志，返回一个Map
    */
  def analysisLog(logText: String, ipRuleArray: Array[IPRule]) = {
    var logMap : mutable.Map[String,String] = null
    if(StringUtils.isNotBlank(logText)){
      val fields = logText.split("[|]")
      if(fields.length == 4){
        logMap = mutable.Map[String,String]()
        logMap.put(LogConstants.LOG_COLUMNS_NAME_IP,fields(0))
        logMap.put(LogConstants.LOG_COLUMNS_NAME_ACCESS_TIME,fields(1))
        //将ip对应的地域信息，封装到logMap中
        handleIP(logMap, ipRuleArray)
        //解析收集的用户行为数据，添加到logMap中
        handleRequestParams(logMap, fields(3))
      }
    }
    logMap
  }
}
