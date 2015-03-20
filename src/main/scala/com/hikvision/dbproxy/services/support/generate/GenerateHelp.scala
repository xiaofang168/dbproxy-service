/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: GenerateHelp.scala
 * created at: 2014年9月29日
 */
package com.hikvision.dbproxy.services.support.generate

import com.hikvision.dbproxy.entities.Table
import com.hikvision.dbproxy.services.ProxySettingCacheService
import com.hikvision.dbproxy.services.exception.ModuleServiceException
import com.hikvision.dbproxy.services.InfoExchangeType

/**
 * 生产帮助类
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年9月29日 下午2:02:29
 * @version: $Rev: 3461 $
 */
private[generate] object GenerateHelp {

  /**
   * 根据主键id获取缓存的Json table对象<br/>
   * such as:getTable(104454309_23)
   */
  def getTable(appName: String, id: String): Table = {
    // 截取前半部分hash值获取table对应的数据库名称及表名称
    val tableHash = id.split("_")(0).toInt
    // 根据hash值查询缓存的table josn对象
    val settingCacheService = new ProxySettingCacheService(appName)
    settingCacheService.getTableSettingCache(tableHash)
  }

  /**
   * 根据主键id获取缓存的Json table对象及其Id值<br/>
   * such as:getTable(104454309_23)
   */
  def getTableAndId(appName: String, id: String): (Table, String) = {
    // 以“_”分隔符分割
    val hashAndId = id.split("_")
    if (hashAndId.length != 2) {
      throw ModuleServiceException(InfoExchangeType.C_104, "id格式不正确!")
    }
    // get hash value by table name
    val tableNameHashValue = hashAndId(0)
    // get delete table id
    val tableId = hashAndId(1)
    if (!(tableId matches """\d+""") || !(tableNameHashValue matches """\d+""")) {
      throw ModuleServiceException(InfoExchangeType.C_104, "id类型错误,应为数字类型!")
    }
    // 获取table对象
    val table = try {
      GenerateHelp.getTable(appName, tableNameHashValue.toInt)
    } catch {
      case e: NullPointerException => throw ModuleServiceException(InfoExchangeType.C_104, "id值TableHash错误!")
    }
    (table, tableId)
  }

  /**
   * 根据表名称的hash值获取缓存的Json table对象<br/>
   * such as:getTable(104454309)
   */
  def getTable(appName: String, tableNameHashValue: Int): Table = {
    // 根据hash值查询缓存的table josn对象
    val settingCacheService = new ProxySettingCacheService(appName)
    settingCacheService.getTableSettingCache(tableNameHashValue)
  }

}