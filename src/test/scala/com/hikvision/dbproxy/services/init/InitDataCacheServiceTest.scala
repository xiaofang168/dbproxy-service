/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: InitDataCacheServiceTest.scala
 * created at: 2014年7月21日
 */
package com.hikvision.dbproxy.services.init

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import com.hikvision.dbproxy.entities.Database
import com.hikvision.dbproxy.entities.ProxySetting
import com.hikvision.dbproxy.entities.Resource
import com.hikvision.dbproxy.entities.Table
import com.hikvision.dbproxy.services.ResourceDataCacheService
import com.hikvision.dbproxy.services.executor.DataSourceExecutor
import com.hikvision.dbproxy.services.factory.DataSourceFactory
import com.hikvision.dbproxy.services.invoker.ExecutorInvoker
import com.hikvision.dbproxy.services.init.JsonImplicits.impProxySetting
import scala.io.Source
import spray.json.JsonParser
/**
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年7月21日 上午11:38:35
 * @version: $Rev: 3431 $
 */
class CacheTest

class InitDataCacheServiceTest {

  var service: InitDataService = _

  @Before
  def init() {
    service = new CacheTest with InitDataService
  }

  @Test
  def testGetDbproxySettingList() {
    val list: List[ProxySetting] = service.getDbproxySettingList
    Assert.assertTrue(!list.isEmpty)
  }

  @Test
  def testInitDataSource() {
    val database1 = new Database("test", "localhost", "mysqlserver", "mysql", "3306", "root", "root", "jdbc:mysql://localhost:3306/test", "com.mysql.jdbc.Driver")
    val database2 = new Database("DBMT70", "localhost", "mssqlserver", "mssql", "", "DBMT70_ADM", "mtaADM12345*", "jdbc:sqlserver://10.192.32.72;DatabaseName=DBMT70", "com.microsoft.sqlserver.jdbc.SQLServerDriver")
    val list = List[Database](database1, database2)
    val proxySetting = new ProxySetting(1, "app1", "", "1", list, null)
    new ExecutorInvoker(proxySetting).init(new DataSourceExecutor)
    val dataSource = DataSourceFactory.get("app1", "mysqlserver")
    val dataSource2 = DataSourceFactory.get("app2", "test")
    Assert.assertNotNull(dataSource)
    Assert.assertNull(dataSource2)
  }

  @Test
  def testInitResourcesCacheDatas() {
    // 初始化数据源
    testInitDataSource
    val resource1Tables = List(new Table("user", "mysqlserver"))
    val resource1 = new Resource("user", resource1Tables, Option("1"), None, None)
    val list = List(resource1)
    val proxySetting = new ProxySetting(1, "app1", "", "1", null, list)
    new ExecutorInvoker(proxySetting).init(new DataSourceExecutor)

    val cacheService = new ResourceDataCacheService("app1", "user")
    val datas = cacheService.list
    Assert.assertTrue(!datas.isEmpty)
  }

  @Test
  def testParseJson() {
    // 查询本地数据库代理配置
    val proxy = Source.fromURL(getClass.getResource("/proxy.json")).mkString
    val jsonObj = JsonParser(proxy)
    jsonObj.convertTo[ProxySetting]
    Assert.assertTrue(1 == 1)
  }

}