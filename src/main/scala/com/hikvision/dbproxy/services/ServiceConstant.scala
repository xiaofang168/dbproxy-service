/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: Constant.scala
 * created at: 2014年8月5日
 */
package com.hikvision.dbproxy.services

import com.hikvision.dbproxy.core.dialect.Dialect
import com.hikvision.dbproxy.core.dialect.MySQLDialect
import com.hikvision.dbproxy.core.dialect.SQLServer2008Dialect
import com.hikvision.dbproxy.core.dialect.Oracle10gDialect
import com.hikvision.dbproxy.core.dialect.PostgreSQLDialect
import com.typesafe.config.ConfigFactory

/**
 * 常量定义
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年8月5日 上午10:00:12
 * @version: $Rev: 3290 $
 */
object ServiceConstant {

  private val config = ConfigFactory.load()

  /**查询大小(不分页默认查询数目)*/
  val selectSize = config.getInt("execute.select.size")

  /**批处理默认数(查询数据库数据写入缓存数据库)*/
  val batchSize = config.getInt("execute.batch.size")

  /**id代理*/
  val idproxy = config.getString("server.idproxy")
  /**id代理的数据库*/
  val ID_PROXY_DB = "proxy"

  /**
   * 数据库方言map映射
   */
  val DB_DIALECT = Map[String, Dialect]("mysql" -> new MySQLDialect, "sqlserver" -> new SQLServer2008Dialect, "oracle" -> new Oracle10gDialect, "postgresql" -> new PostgreSQLDialect)

  /**真1*/
  val IS_TRUE_1 = "1"
  /**假0*/
  val IS_FALSE_0 = "0"

  /**表主键字段*/
  val TABLE_PRIMARY_KEY = "id"
  /**资源主键字段*/
  val RESOURCE_PRIMARY_KEY = "_id"

  /**相应码常量定义*/
  val RESPONSE_CODE = "code";
  /**相应结果集常量定义*/
  val RESPONSE_RESULT = "result"

}