/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: DataSourceExecutor.scala
 * created at: 2014年9月23日
 */
package com.hikvision.dbproxy.services.executor

import com.hikvision.dbproxy.entities.ProxySetting
import com.hikvision.dbproxy.services.factory.DataSourceFactory

/**
 * 数据源执行者
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年9月23日 下午1:02:59
 * @version: $Rev: 3248 $
 */
class DataSourceExecutor extends DbproxyExecutor {

  def init(setting: ProxySetting) {
    val list = setting.databases
    DataSourceFactory.clean
    list foreach (db => DataSourceFactory.put(setting.app_name, db.alias, db.username, db.password, db.jdbc_url, db.jdbc_driver))
  }

}