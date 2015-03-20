/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: JdbcClientPoolTest.scala
 * created at: 2014年7月22日
 */
package com.hikvision.dbproxy.services

import org.junit.Test
import org.junit.Before
import java.sql.DriverManager
import org.junit.Assert
import com.hikvision.dbproxy.services.factory.DataSourceFactory

/**
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年7月22日 下午1:57:29
 * @version: $Rev: 3248 $
 */
class JdbcClientPoolTest {

  @Before
  def init() {
    DataSourceFactory.put("app1", "test1", "root", "root", "jdbc:mysql://localhost:3306/test", "com.mysql.jdbc.Driver")
    DataSourceFactory.put("app2", "test2", "DBMT70_ADM", "mtaADM12345*", "jdbc:sqlserver://10.192.32.72;DatabaseName=DBMT70", "com.microsoft.sqlserver.jdbc.SQLServerDriver")
  }

  @Test
  def testGetConnection() {
    val connection1 = JdbcClientPool.getConnection("app1", "test1")
    val connection2 = JdbcClientPool.getConnection("app2", "test2")
    Assert.assertNotNull(connection1)
    Assert.assertNotNull(connection2)
  }

}