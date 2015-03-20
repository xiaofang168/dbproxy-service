/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: QueryConverterTest.java
 * created at: 2014年8月19日
 */
package com.hikvision.dbproxy.services.support

import org.junit.Test
import org.junit.Assert

/**
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年8月19日 下午3:01:51
 * @version: $Rev: 3248 $
 */
class QueryConverterTest {

  @Test
  def testAddResultUniqueKey() {
    val converter = new QueryConverter
    val list = List[Map[String, _]](Map("id" -> "1334", "name" -> "张三"), Map("id" -> "134566", "name" -> "张三"))
    val uniqueList = converter.addResultUniqueKey("app1", "test", "user", "id", list)
    Assert.assertEquals("104454309_1334", uniqueList(0)("_id"))
    Assert.assertEquals("104454309_134566", uniqueList(1)("_id"))
  }

}
