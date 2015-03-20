/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: GenerateDeleteBatchSqlEntityTest.java
 * created at: 2014年9月29日
 */
package com.hikvision.dbproxy.services.support.generate;

import org.junit.Test
import com.hikvision.dbproxy.services.init.InitDataService
import org.junit.Before
import com.hikvision.dbproxy.entities.Delete
import com.hikvision.dbproxy.services.StringUtils
import com.hikvision.dbproxy.services.ProxySettingCacheService
import com.hikvision.dbproxy.entities.Table
import org.junit.Assert

/**
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年9月29日 上午11:11:44
 * @version: $Rev: 3945 $
 */
class GenerateDeleteBatchSqlEntityTest {
   
  val settingCacheService = new ProxySettingCacheService("app1") with InitDataService

  @Before
  def init() {
    settingCacheService.init
    val list = List(Table("test", "user"))
    settingCacheService.setTableSettingCache(list)
  }

  /**
   * Test method for {@link com.hikvision.dbproxy.services.support.generate.GenerateDeleteBatchSqlEntity#generate(java.lang.String, java.lang.String, com.hikvision.dbproxy.entities.Query)}.
   */
  @Test
  def testGenerate() {
    val id = StringUtils.BKDRHash("app1testuser").toString
    val query = Delete(s"${id}_1")
    val gibse = new GenerateDeleteBatchSqlEntity(query)
    val batchSqlEntity = gibse.generate("app1", "user")
    val listSqlEntity = batchSqlEntity.list
    val sqlEntity = listSqlEntity(0)
    val sql = sqlEntity.sql
    Assert.assertEquals("delete from user where id = ?", sql)
  }

}
