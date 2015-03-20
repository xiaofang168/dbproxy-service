/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: JsonImplicits.scala
 * created at: 2014年7月21日
 */
package com.hikvision.dbproxy.services.init

import com.hikvision.dbproxy.entities.Database
import com.hikvision.dbproxy.entities.ProxySetting
import com.hikvision.dbproxy.entities.Resource
import com.hikvision.dbproxy.entities.Table
import spray.json.DefaultJsonProtocol
import com.hikvision.dbproxy.entities.Sharding
import com.hikvision.dbproxy.entities.Biz
import com.hikvision.dbproxy.entities.Rule

/**
 * 对象json隐式转换处理
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年7月21日 下午1:30:47
 * @version: $Rev: 3248 $
 */
object JsonImplicits extends DefaultJsonProtocol {
  import spray.json._
  import DefaultJsonProtocol._

  implicit val impDatabase = jsonFormat9(Database)
  implicit val impTable = jsonFormat2(Table)
  implicit val impBiz = jsonFormat2(Biz)
  implicit val impRule = jsonFormat3(Rule)
  implicit val impShardingRule = jsonFormat3(Sharding)
  implicit val impResource = jsonFormat5(Resource)
  implicit val impProxySetting = jsonFormat6(ProxySetting)

}