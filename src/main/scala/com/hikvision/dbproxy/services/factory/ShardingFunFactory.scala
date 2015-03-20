/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: ShardingFunFactory.scala
 * created at: 2014年9月23日
 */
package com.hikvision.dbproxy.services.factory

import com.hikvision.dbproxy.services.StringUtils
import scala.collection.mutable.Map
import com.hikvision.dbproxy.services.sharding.DatabaseShardingRuleValidator

/**
 * 数据库水平切分函数工厂类
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年9月23日 下午3:58:32
 * @version: $Rev: 3248 $
 */
object ShardingFunFactory {

  // 函数map集合
  private val funMap: Map[String, Any => Boolean] = Map[String, Any => Boolean]()

  /**
   * 存放分库函数
   */
  def put(column: String, `type`: String, expression: String, f: Any => Boolean) {
    val key = getManyFieldHashKey(column, `type`, expression)
    if (!ShardingFunFactory.funMap.contains(key)) {
      // 存放验证函数
      ShardingFunFactory.funMap += (key.toString -> f)
    }
  }

  /**
   * 获取分库函数
   */
  def get(column: String, `type`: String, expression: String): Any => Boolean = {
    val key = getManyFieldHashKey(column, `type`, expression)
    ShardingFunFactory.funMap(key)
  }

  /**
   * 清除所有分库函数
   */
  def clean() {
    ShardingFunFactory.funMap.clear
  }

  // 获取多个字段hash值
  private def getManyFieldHashKey(column: String, `type`: String, expression: String): String = {
    // 字符串连接取hash值作为key
    val keyStr = s"$column${`type`}$expression"
    StringUtils.BKDRHash(keyStr).toString
  }

}