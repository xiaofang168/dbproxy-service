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
import com.hikvision.dbproxy.jdbc.support.ReadRunnerImpl
import com.hikvision.dbproxy.jdbc.handlers.ArrayHandler
import com.hikvision.dbproxy.services.factory.DataSourceFactory

/**
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年7月22日 下午1:35:48
 * @version: $Rev: 3248 $
 */
class SqlServerDataSourceFactoryTest {

  @Test
  def testPut() {
    val map = DataSourceFactory.put("app1", "test1", "DBMT70_ADM", "mtaADM12345*", "jdbc:sqlserver://10.192.32.201;DatabaseName=DBMT70", "com.microsoft.sqlserver.jdbc.SQLServerDriver")
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
    val readRunner = new ReadRunnerImpl()
    // 使用map List 查询才可以包含键值对column->value
    val rsh = new ArrayHandler()
    val list = readRunner.query(connection, "select count(*) from mta_AlmEvt", rsh)
    Assert.assertNotNull(list)
  }

}