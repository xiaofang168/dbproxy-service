/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: DataSourceService.scala
 * created at: 2014年7月22日
 */
package com.hikvision.dbproxy.services.factory

import java.util.Properties
import scala.collection.mutable.Map
import org.apache.commons.dbcp.BasicDataSource

/**
 * 数据源工厂
 * <p></p>
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年7月22日 上午11:04:42
 * @version: $Rev: 3567 $
 */
object DataSourceFactory {

  private val dataSourceMap: Map[String, BasicDataSource] = Map[String, BasicDataSource]()

  /**
   * 添加数据源
   */
  def put(app: String, db: String, username: String, password: String, url: String, driver: String): Map[String, BasicDataSource] = {
    //加载连接池配置文件
    val prop = new Properties()
    val is = getClass().getResourceAsStream("/jdbc_pool.properties");
    prop.load(is)
    //设置数据源配置
    val dataSource = new BasicDataSource()
    dataSource.setUsername(username)
    dataSource.setPassword(password)
    dataSource.setUrl(url)
    dataSource.setDriverClassName(driver)
    dataSource.setInitialSize(prop.getProperty("initialSize").toInt)
    dataSource.setMaxActive(prop.getProperty("maxActive").toInt)
    dataSource.setMaxIdle(prop.getProperty("maxIdle").toInt)
    dataSource.setMinIdle(prop.getProperty("minIdle").toInt)
    DataSourceFactory.dataSourceMap += (s"$app$db" -> dataSource)
  }

  /**
   * 获取数据源
   */
  def get(app: String, db: String): BasicDataSource = {
    DataSourceFactory.dataSourceMap(s"$app$db")
  }

  def clean() = this.dataSourceMap.clear

}