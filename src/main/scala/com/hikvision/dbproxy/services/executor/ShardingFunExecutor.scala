/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: ShardingFunExecutor.scala
 * created at: 2014年9月23日
 */
package com.hikvision.dbproxy.services.executor

import com.hikvision.dbproxy.entities.ProxySetting
import com.hikvision.dbproxy.services.sharding.DatabaseShardingRuleValidator
import com.hikvision.dbproxy.entities.Sharding
import com.hikvision.dbproxy.services.factory.ShardingFunFactory

/**
 * 水平切分函数执行者
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年9月23日 下午1:06:29
 * @version: $Rev: 3442 $
 */
class ShardingFunExecutor extends DbproxyExecutor {

  def init(setting: ProxySetting) {
    val list = setting.resources
    list.foreach(r => {
      // 分片
      r.sharding match {
        case Some(sharding) => {
          sharding.rules.foreach(rule => {
            // 自定义切分验证函数
            val validator = new DatabaseShardingRuleValidator(sharding.column, sharding.`type`, rule.expression)
            ShardingFunFactory.put(sharding.column, sharding.`type`, rule.expression, validator.fun)
          })
        }
        case None => null
      }
    })
  }

}