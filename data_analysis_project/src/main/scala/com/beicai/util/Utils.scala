package com.beicai.util

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
}
