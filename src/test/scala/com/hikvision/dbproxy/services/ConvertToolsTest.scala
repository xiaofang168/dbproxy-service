/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: ConvertToolsTest.java
 * created at: 2014年10月10日
 */
package com.hikvision.dbproxy.services;

import org.junit.Test;
import org.junit.Assert

/**
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年10月10日 下午2:50:57
 * @version: $Rev: 3340 $
 */
class ConvertToolsTest {

  /**
   * Test method for {@link com.hikvision.dbproxy.services.ConvertTools#javaList2ScalaList(java.util.List)}.
   */
  @Test
  def testJavaList2ScalaList() {
    val jlist = new java.util.ArrayList[String]()
    jlist.add("a");
    val slist = ConvertTools.javaList2ScalaList(jlist)
    Assert.assertEquals(slist(0), "a")
  }

  @Test
  def testJavaList2ScalaList2() {
    val jlist = null
    try {
      val slist = ConvertTools.javaList2ScalaList(jlist)
    } catch {
      case e: Exception => Assert.assertTrue(1 == 1)
    }
  }

}
