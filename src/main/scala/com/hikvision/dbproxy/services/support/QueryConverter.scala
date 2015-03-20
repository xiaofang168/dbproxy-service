/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: QueryConverter.scala
 * created at: 2014年8月6日
 */
package com.hikvision.dbproxy.services.support

import com.hikvision.dbproxy.services.StringUtils
import com.hikvision.dbproxy.services.ServiceConstant

/**
 * 查询转换
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年8月6日 下午3:32:33
 * @version: $Rev: 3248 $
 */
class QueryConverter {

  /**
   * 查询结果集转换为json
   */
  def toJson(appName: String, databaseName: String, tableName: String, list: List[Map[String, _]]): List[String] = {
    import spray.json._
    import DefaultJsonProtocol._

    list.par.map(e => {
      // TODO 涉及到类型转换,如:时间类型转化为String等
      val mapObj2String = e.map(e => e._1 -> {
        if (e._2 == null) "" else e._2.toString
      })
      mapObj2String.toJson.toString
    }).seq.toList

  }

  /**
   * 添加结果集唯一key值
   */
  def addResultUniqueKey(appName: String, databaseName: String, tableName: String, pk: String, list: List[Map[String, _]]): List[Map[String, _]] = {
    val hash = StringUtils.BKDRHash(s"$appName$databaseName$tableName")
    list.map(e => e + (ServiceConstant.RESOURCE_PRIMARY_KEY -> s"${hash}_${e.getOrElse(pk, "")}")) toList
  }

  /**
   * par方式添加结果集唯一key值
   */
  def parAddResultUniqueKey(appName: String, databaseName: String, tableName: String, pk: String, list: List[Map[String, _]]): List[Map[String, _]] = {
    val hash = StringUtils.BKDRHash(s"$appName$databaseName$tableName")
    list.par.map(e => e + (ServiceConstant.RESOURCE_PRIMARY_KEY -> s"${hash}_${e.getOrElse(pk, "")}")).seq toList
  }

}