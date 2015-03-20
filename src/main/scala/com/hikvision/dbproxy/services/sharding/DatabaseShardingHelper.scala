/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: DatabaseShardingHelper.scala
 * created at: 2014年10月20日
 */
package com.hikvision.dbproxy.services.sharding

import com.hikvision.dbproxy.services.ProxySettingCacheService
import com.hikvision.dbproxy.entities.Sharding
import com.hikvision.dbproxy.entities.Rule
import com.hikvision.dbproxy.services.factory.ShardingFunFactory

/**
 * 数据库分片帮助类
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年10月20日 上午11:05:43
 * @version: $Rev$
 */
object DatabaseShardingHelper {

  // 是否匹配规则
  private def isMathRule(column: String, t: String, expression: String, value: Any): Boolean = {
    val validator = ShardingFunFactory.get(column, t, expression)
    validator(value)
  }

  // 获取匹配切分规则集合
  def getMathcShardingRules(value: Any, sharding: Sharding): List[Rule] = {
    val rules = sharding.rules
    rules.filter(rule => isMathRule(sharding.column, sharding.`type`, rule.expression, value))
  }

}