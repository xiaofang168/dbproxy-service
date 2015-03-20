/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: DbproxyServiceCacheServiceTest.java
 * created at: 2014年8月7日
 */
package com.hikvision.dbproxy.services.support;

import org.junit.Test
import junit.framework.Assert
import com.hikvision.dbproxy.cache.CacheService
import com.hikvision.dbproxy.services.init.InitDataService
import com.hikvision.dbproxy.services.exception.ModuleServiceException
import com.hikvision.dbproxy.services.InfoExchangeType

/**
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年8月7日 下午1:04:55
 * @version: $Rev: 3248 $
 */
class DbproxyServiceCacheServiceTest {

  @Test
  def test() {
    val service = new DbproxyServiceCacheServiceTest with DbproxyServiceCacheService
    val startTime = System.currentTimeMillis()
    val result = service.select("app1", "event", Map("UsrName" -> "ehome702"))
    val endTime = System.currentTimeMillis()
    println("execute time:" + (endTime - startTime))
    println(result.size)
  }

  @Test
  def testSplitBatchSelect() {
    val service = new DbproxyServiceCacheServiceTest with DbproxyServiceCacheService with CacheService
    val startTime = System.currentTimeMillis()
    //Map("Evtdatetime" -> "2012-08-21 02:21:40")
    val list = service.select("app1", "user", Map("sort" -> "id"))
    val endTime = System.currentTimeMillis()
    println("execute time:" + (endTime - startTime))
    println(list)
  }

  @Test
  def testSelect() {
    try {
      val service = new DbproxyServiceCacheService with CacheService
      val list = service.select("app1", "user", Map("Evtdatetime" -> "2012-08-21 02:21:40"))
    } catch {
      case e: ModuleServiceException => Assert.assertEquals(InfoExchangeType.C_101, e.code)
    }
  }

}
