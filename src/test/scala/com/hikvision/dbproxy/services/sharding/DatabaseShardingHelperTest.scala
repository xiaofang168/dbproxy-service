/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: DatabaseShardingHelper.java
 * created at: 2014年10月20日
 */
package com.hikvision.dbproxy.services.sharding

import org.junit.Test
import org.junit.Assert
import com.hikvision.dbproxy.services.factory.ShardingFunFactory
import com.hikvision.dbproxy.services.ProxySettingCacheService
import com.hikvision.dbproxy.entities.Sharding
import com.hikvision.dbproxy.entities.Rule

/**
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年10月20日 上午11:06:16
 * @version: $Rev$
 */
class DatabaseShardingHelperTest {

  @Test
  def testGetShardingRule() {
    val sharding = Sharding("id", "string", List[Rule](Rule("hash(id)%2==0", "test", "user"), Rule("hash(id)%2==1", "test", "user2")))
    sharding.rules.foreach(rule => {
      // 自定义切分验证函数
      val validator = new DatabaseShardingRuleValidator(sharding.column, sharding.`type`, rule.expression)
      ShardingFunFactory.put(sharding.column, sharding.`type`, rule.expression, validator.fun)
    })
    val ruleList = DatabaseShardingHelper.getMathcShardingRules("23", sharding)
    Assert.assertEquals("hash(id)%2==1", ruleList(0).expression)
  }

  @Test
  def testGetShardingRule2() {
    val sharding = Sharding("id", "string", List[Rule](Rule("hash(id)%2==3", "test", "user"), Rule("hash(id)%2==2", "test", "user2")))
    sharding.rules.foreach(rule => {
      // 自定义切分验证函数
      val validator = new DatabaseShardingRuleValidator(sharding.column, sharding.`type`, rule.expression)
      ShardingFunFactory.put(sharding.column, sharding.`type`, rule.expression, validator.fun)
    })
    val ruleList = DatabaseShardingHelper.getMathcShardingRules("23", sharding)
    Assert.assertEquals(0, ruleList.size)
  }

}
