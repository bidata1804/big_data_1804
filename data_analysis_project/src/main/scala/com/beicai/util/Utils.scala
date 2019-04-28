package com.beicai.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.regex.Pattern

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
}
