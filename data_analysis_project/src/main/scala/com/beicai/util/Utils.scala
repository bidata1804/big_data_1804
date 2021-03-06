package com.beicai.util

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}
import java.util.regex.Pattern
import scala.util.control.Breaks._

/**
  * Created by 任景阳 on 2019/4/25.
  */
object Utils {
  /**
    * 将ip转换成完整的32位2机制对应的十进制数字
    */
    def ip2Long(ip : String) = {
      var numIp:Long = 0
      val items = ip.split(".")
      for(item <- items){
        numIp = (numIp << 8 | item.toLong)
      }
      numIp
    }

  /**
    * 验证日期是否是yyyy-MM-dd这种格式
    *
    * @param inputDate 输入的需要验证的日期 2019-01-01
    */
  def validateInputDate(inputDate: String) = {
    val reg = "^(?:(?!0000)[0-9]{4}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1[0-9]|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[0-9]{2}(?:0[48]|[2468][048]|[13579][26])|(?:0[48]|[2468][048]|[13579][26])00)-02-29)$"
    Pattern.compile(reg).matcher(inputDate).matches()
  }

  /**
    * 将日期转换成时间戳
    *
    * @param inputDate
    * @param pattern
    */
  def parseDate(inputDate: String, pattern: String) = {
    val sdf = new SimpleDateFormat(pattern)
    sdf.parse(inputDate).getTime
  }

  /**
    * 格式化日期
    *
    * @param longTime 时间戳
    * @param pattern  需要格式化的格式
    */
  def formatDate(longTime: Long, pattern: String) = {
    val sdf = new SimpleDateFormat(pattern)
    sdf.format(new Date(longTime))
  }

  /**
    * 获取指定日期第二天的日期
    */
  def getNextDate(longTime: Long): Long = {
    val calendar = Calendar.getInstance()
    calendar.setTimeInMillis(longTime)
    calendar.add(Calendar.DAY_OF_MONTH,1)
    calendar.getTimeInMillis
  }

  /**
    * 获取字符串中指定字段的值
    *
    * @param value
    * session_count=0|1s_3s=0|4s_6s=0|7s_9s=0|10s_30s=0|30s_60s=0|1m_3m=0|3m_10m=0|10m_30m=0|30m=0|1_3=0|4_6=0|7_9=0|10_30=0|30_60=0|60=0
    * @param fieldName
    * session_count
    */
  def getFieldValue(value: String, fieldName: String) = {
    var fieldValue : String = null
    //Array(session_count=0,1s_3s=0,...)
    breakable({
      val items = value.split("[|]")
      for (item <- items){
        val kv = item.split("[=]")
        if(kv(0).equals(fieldName)){
          fieldValue = kv(1)
          break()
        }
      }
    })
    fieldValue
  }
  /**
    * 设置字符串中指定字段的值
    *
    * @param value
    * session_count=0|1s_3s=0|4s_6s=0|7s_9s=0|10s_30s=0|30s_60s=0|1m_3m=0|3m_10m=0|10m_30m=0|30m=0|1_3=0|4_6=0|7_9=0|10_30=0|30_60=0|60=0
    * @param fieldName
    * session_count
    * @param fieldNewValue
    * 190
    * @return
    * session_count=190|1s_3s=0|4s_6s=0|7s_9s=0|10s_30s=0|30s_60s=0|1m_3m=0|3m_10m=0|10m_30m=0|30m=0|1_3=0|4_6=0|7_9=0|10_30=0|30_60=0|60=0
    */
  def setFieldValue(value: String, fieldName: String, fieldNewValue: String) = {
    //Array(session_count=0,1s_3s=0,4s_6s=0,...)
    val items = value.split("[|]")
    breakable({
    for(i <- 0 until(items.length)) {
         val item = items(i)
         val kv = item.split("[=]")
         if(kv(0).equals(fieldName)){
          items(i) = fieldName + "=" + fieldNewValue
           break()
        }
      }
    })
      items.mkString("|")
  }
}
