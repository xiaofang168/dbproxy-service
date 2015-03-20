/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: JdbcClientPool.scala
 * created at: 2014年7月22日
 */
package com.hikvision.dbproxy.services

import java.sql.Connection
import java.util.Properties
import java.io.FileInputStream
import com.hikvision.dbproxy.services.factory.DataSourceFactory

/**
 * jdbc 客户端连接池
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年7月22日 上午10:16:32
 * @version: $Rev: 3248 $
 */

object JdbcClientPool {
  def getConnection(app: String, db: String): Connection = {
    DataSourceFactory.get(app, db).getConnection()
  }
}