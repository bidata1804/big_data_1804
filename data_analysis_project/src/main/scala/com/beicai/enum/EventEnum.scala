package com.beicai.enum

/**
  * Created by 任景阳 on 2019/4/28.
  */
object EventEnum extends  Enumeration{
  val LAUNCH_EVENT = Value(0, "e_l")
  val PAGE_VIEW_EVENT = Value(1, "e_pv")
  val BROWSER_PRODUCT_EVENT = Value(2, "e_bp")
}
