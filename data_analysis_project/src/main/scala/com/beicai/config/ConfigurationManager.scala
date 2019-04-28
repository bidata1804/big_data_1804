package com.beicai.config

import org.apache.hadoop.conf.Configuration

/**
  * Created by 任景阳 on 2019/4/26.
  */
object ConfigurationManager {
  private val configuration = new Configuration()
  //加载配置文件
  configuration.addResource("project-config.xml")

  def getValue(key: String) = {
    configuration.get(key)
  }
}
