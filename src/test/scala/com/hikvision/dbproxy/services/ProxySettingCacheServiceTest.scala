/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: ProxySettingCacheServiceTest.java
 * created at: 2014年8月19日
 */
package com.hikvision.dbproxy.services

import org.junit.Test
import com.hikvision.dbproxy.entities.Table
import org.junit.Assert
/**
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年8月19日 上午11:24:19
 * @version: $Rev: 3464 $
 */
class ProxySettingCacheServiceTest {

  @Test
  def testSetTablesHashValueCahe() {
    val service = new ProxySettingCacheService("app1")
    val list = List[Table](Table("user", "db1"), Table("event", "db1"))
    val l = service.setTableSettingCache(list)
    Assert.assertEquals(2, l.size)
  }

  @Test
  def testGetTablesHashValueCahe() {
    val service = new ProxySettingCacheService("app1")
    val list = List[Table](Table("user", "db1"), Table("event", "db1"))
    val l = service.setTableSettingCache(list)
    val hash = StringUtils.BKDRHash("app1db1user")
    val s = service.getTableSettingCache(hash)
    Assert.assertEquals("user", s.name)

    val hash2 = StringUtils.BKDRHash("app1db1event")
    val s2 = service.getTableSettingCache(hash2)
    Assert.assertEquals("event", s2.name)
  }

}
