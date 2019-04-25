package com.beicai.bean

import com.beicai.constants.GlobalConstants

/**
  * Created by 任景阳 on 2019/4/25.
  */
//这里是给一个地域的默认值
case class RegionInfo(var country : String = GlobalConstants.DEFAULT_VALUE,var province: String=GlobalConstants.DEFAULT_VALUE, var city: String=GlobalConstants.DEFAULT_VALUE)
