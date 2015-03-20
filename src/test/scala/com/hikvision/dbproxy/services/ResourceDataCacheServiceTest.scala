/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: ResourceDataCacheServiceTest.java
 * created at: 2014年8月19日
 */
package com.hikvision.dbproxy.services

import org.junit.Test
import org.junit.Assert

/**
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年8月19日 下午4:16:01
 * @version: $Rev: 3248 $
 */
class ResourceDataCacheServiceTest {

  @Test
  def testList() {
    val service = new ResourceDataCacheService("app1", "test")
    val list = service.list
    Assert.assertEquals(0, list.size)
  }

}
