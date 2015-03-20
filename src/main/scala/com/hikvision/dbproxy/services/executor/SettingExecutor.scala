/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: SettingExecutor.scala
 * created at: 2014年9月23日
 */
package com.hikvision.dbproxy.services.executor

import com.hikvision.dbproxy.entities.ProxySetting
import com.hikvision.dbproxy.services.ProxySettingCacheService

/**
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年9月23日 上午11:14:17
 * @version: $Rev: 3458 $
 */
class SettingExecutor extends DbproxyExecutor {

  def init(setting: ProxySetting) {
    val pscs = new ProxySettingCacheService(setting.app_name)
    pscs.clean
    val databases = setting.databases
    val resources = setting.resources
    pscs.setDatabaseSettingCache(databases)
    pscs.setResourceSettingCache(resources)
    // 设置table hash 缓存
    resources.foreach(resource => {
      pscs.setTableSettingCache(resource.tables)
    })
  }

}