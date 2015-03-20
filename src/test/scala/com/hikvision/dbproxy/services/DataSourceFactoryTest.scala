/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: DataSourceFactoryTest1.scala
 * created at: 2014年7月22日
 */
package com.hikvision.dbproxy.services

import org.junit.Test
import org.junit.Assert
import org.junit.Before
import com.hikvision.dbproxy.services.factory.DataSourceFactory

/**
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年7月22日 下午1:35:48
 * @version: $Rev: 3248 $
 */
class DataSourceFactoryTest {

  @Test
  def testPut() {
    val map = DataSourceFactory.put("app1", "test1", "root", "root", "jdbc:mysql://localhost:3306/test", "com.mysql.jdbc.Driver")
    Assert.assertEquals(1, map.size)
  }

  @Test
  def testGet() {
    testPut
    val basicDataSource = DataSourceFactory.get("app1", "test1")
    Assert.assertNotNull(basicDataSource)
  }

  @Test
  def testGetConnection() {
    testPut
    val basicDataSource = DataSourceFactory.get("app1", "test1")
    val connection = basicDataSource.getConnection()
    Assert.assertNotNull(connection)
  }

}