/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: ExecuteControlServiceTest.java
 * created at: 2014年8月5日
 */
package com.hikvision.dbproxy.services.support;

import org.junit.Assert
import org.junit.Test
import com.hikvision.dbproxy.core.dialect.MySQLDialect
import com.hikvision.dbproxy.core.dialect.SQLServer2008Dialect
import com.hikvision.dbproxy.services.executor.DataSourceExecutor
import com.hikvision.dbproxy.services.init.InitDataService
import com.hikvision.dbproxy.services.invoker.ExecutorInvoker
import org.slf4j.LoggerFactory
import com.hikvision.dbproxy.entities.SqlEntity
import com.hikvision.dbproxy.entities.SqlEntity

/**
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年8月5日 下午7:46:59
 * @version: $Rev: 4006 $
 */
class ExecuteControlServiceTest {

  @Test
  def testCount() {

    val service = new ExecuteControlServiceTest with ExecuteControlService with InitDataService {
      override def logger = LoggerFactory.getLogger(classOf[ExecuteControlServiceTest])
    }

    val proxySettingList = service.getDbproxySettingList
    proxySettingList.map { proxySetting =>
      // 初始化数据库代理数据源
      new ExecutorInvoker(proxySetting).init(new DataSourceExecutor)
    }
    val count = service.count("app1", "test", new MySQLDialect, "user")
    Assert.assertNotNull(count)

  }

  @Test
  def testPageSelect() {
    val service = new ExecuteControlServiceTest with ExecuteControlService with InitDataService {
      override def logger = LoggerFactory.getLogger(classOf[ExecuteControlServiceTest])
    }

    val proxySettingList = service.getDbproxySettingList
    proxySettingList.map { proxySetting =>
      // 初始化数据库代理数据源
      new ExecutorInvoker(proxySetting).init(new DataSourceExecutor)
    }
    val list = service.pageSelect("app1", "DBMT72", new SQLServer2008Dialect, "select * from mta_AlmEvt", Array(), 435000, 1000)
    println(list.size)
  }

  @Test
  def testPage() {
    val service = new ExecuteControlServiceTest with ExecuteControlService with InitDataService {
      override def logger = LoggerFactory.getLogger(classOf[ExecuteControlServiceTest])
    }
    val proxySettingList = service.getDbproxySettingList
    proxySettingList.map { proxySetting =>
      // 初始化数据库代理数据源
      new ExecutorInvoker(proxySetting).init(new DataSourceExecutor)
    }
    val count = service.count("app1", "DBMT72", new SQLServer2008Dialect, "mta_AlmEvt")
    println("count:" + count)
    val listPaging = service.page(count.toString().toInt, 25000)
    val startTime = System.currentTimeMillis()
    println(s"total page: ${listPaging.size}")
    listPaging.par.map { page =>
      val list = service.pageSelect("app1", "DBMT72", new SQLServer2008Dialect, "select * from mta_AlmEvt", Array(), page("offset"), page("limit"))
      println("return list size:" + list.size)
    }
    val endTime = System.currentTimeMillis()
    println(endTime - startTime)
  }

  @Test
  def testSelect() {
    val service = new ExecuteControlServiceTest with ExecuteControlService with InitDataService {
      override def logger = LoggerFactory.getLogger(classOf[ExecuteControlServiceTest])
    }
    val proxySettingList = service.getDbproxySettingList
    val converter = new QueryConverter
    proxySettingList.map { proxySetting =>
      // 初始化数据库代理数据源
      new ExecutorInvoker(proxySetting).init(new DataSourceExecutor)
    }
    val list = service.select("app1", "test", "score", "select this_.* from score this_ limit ?", Array(new Integer(20)))
    val l = converter.addResultUniqueKey("app1", "test", "score", "id", list)
    val l2 = converter.parAddResultUniqueKey("app1", "test", "score", "id", list)
    println(l)
    println(l2)
  }

  @Test
  def testSelect2() {
    val service = new ExecuteControlServiceTest with ExecuteControlService with InitDataService {
      override def logger = LoggerFactory.getLogger(classOf[ExecuteControlServiceTest])
    }
    val proxySettingList = service.getDbproxySettingList
    val converter = new QueryConverter
    proxySettingList.map { proxySetting =>
      // 初始化数据库代理数据源
      new ExecutorInvoker(proxySetting).init(new DataSourceExecutor)
    }
    val list: List[Map[String, AnyRef]] = service.select("app1", "test", "user", "select this_.* from user this_ limit ?", Array(new Integer(20)))
    list.foreach { e =>
      e.map {
        case (k, v) => println(v)
      }

    }
  }

  @Test
  def testGroupbydbName() {
    val sqlEntityList: List[SqlEntity] = List(SqlEntity("app1", "test", "user", "delete from user where id =?", Array[Object]("1")), SqlEntity("app1", "test", "user2", "insert into user2(name,username,realname) values (?,?,?)", Array[Object]("aa", "aa", "aa")))
    val map: Map[String, List[SqlEntity]] = sqlEntityList.groupBy(_.databaseName)
    Assert.assertEquals(2, map("test").size)
  }

}
