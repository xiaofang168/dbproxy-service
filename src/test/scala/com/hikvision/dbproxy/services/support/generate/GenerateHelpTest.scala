/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: GenerateHelpTest.java
 * created at: 2014年10月10日
 */
package com.hikvision.dbproxy.services.support.generate;

import org.junit.Test
import com.hikvision.dbproxy.services.exception.ModuleServiceException
import org.junit.Assert

/**
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年10月10日 下午3:54:37
 * @version: $Rev: 3340 $
 */
class GenerateHelpTest {

  /**
   * Test method for {@link com.hikvision.dbproxy.services.support.generate.GenerateHelp#getTable(java.lang.String, java.lang.String)}.
   */
  @Test
  def getTableAndId1() {
    try {
      GenerateHelp.getTableAndId("app1", "11")
    } catch {
      case e: ModuleServiceException => Assert.assertEquals("id格式不正确!", e.message)
    }
  }

  @Test
  def getTableAndId2() = {
    try {
      val result = GenerateHelp.getTableAndId("app1", "11_323")
    } catch {
      case e: ModuleServiceException => Assert.assertEquals("id值TableHash错误!", e.message)
    }
  }

  @Test
  def getTableAndId3() = {
    try {
      val result = GenerateHelp.getTableAndId("app1", "1d1_32d3")
    } catch {
      case e: ModuleServiceException => Assert.assertEquals("id类型错误,应为数字类型!", e.message)
    }
  }

}
