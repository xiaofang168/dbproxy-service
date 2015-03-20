/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: InitDataCacheService.scala
 * created at: 2014年7月21日
 */
package com.hikvision.dbproxy.services

import com.hikvision.dbproxy.cache.services.HashCacheService
import com.hikvision.dbproxy.cache.services.StringCacheService
import com.hikvision.dbproxy.entities.Database
import com.hikvision.dbproxy.entities.Resource
import com.hikvision.dbproxy.entities.Table
import com.hikvision.dbproxy.services.init.JsonImplicits.impDatabase
import com.hikvision.dbproxy.services.init.JsonImplicits.impResource
import com.hikvision.dbproxy.services.init.JsonImplicits.impTable

import spray.json.JsonParser
import spray.json.pimpAny

/**
 * 数据缓存服务
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年7月21日 上午11:03:38
 * @version: $Rev: 3513 $
 */
class ProxySettingCacheService(app: String) {

  /**数据库hash缓存前缀*/
  private val DB_HASH_CHACHE_KEY_PREFIX = "db_"

  /**资源hash缓存前缀*/
  private val RESOURCE_HASH_CHACHE_KEY_PREFIX = "resource_"

  val hashCacheService: HashCacheService = new HashCacheService

  /**设置资源配置缓存*/
  def setResourceSettingCache(list: List[Resource]): List[String] = {
    for {
      resource <- list
      if hashCacheService.hset(app, s"$RESOURCE_HASH_CHACHE_KEY_PREFIX${resource.name}", resource.toJson.toString)
    } yield (resource.toJson.toString)
  }

  /**获取资源配置缓存*/
  def getResourceSettingCache(resource: String): Resource = {
    import spray.json.JsonParser
    hashCacheService.hget(app, s"$RESOURCE_HASH_CHACHE_KEY_PREFIX$resource") match {
      case Some(jsonStr) => JsonParser(jsonStr).convertTo[Resource]
      case None => null
    }
  }

  /**设置表资源缓存,key值为hash表名取值*/
  def setTableSettingCache(list: List[Table]): List[String] = {
    val stringCacheService = new StringCacheService
    for {
      table <- list
      hash = StringUtils.BKDRHash(s"$app${table.belong_db}${table.name}")
      if (stringCacheService.set(s"$hash", table.toJson.toString))
    } yield (table.toJson.toString)
  }

  /**
   * 获取 table缓存
   * @ hash 表名的hash值
   */
  def getTableSettingCache(hash: Int): Table = {
    val stringCacheService = new StringCacheService
    stringCacheService.get(s"$hash") match {
      case Some(jsonStr) => JsonParser(jsonStr).convertTo[Table]
      case None => null
    }
  }

  /**
   * 获取 table缓存
   * @ hash 表名的hash值
   */
  def getTableSettingCache(database: String, table: String): Table = {
    val hash = StringUtils.BKDRHash(s"$app$database$table")
    val stringCacheService = new StringCacheService
    stringCacheService.get(s"$hash") match {
      case Some(jsonStr) => JsonParser(jsonStr).convertTo[Table]
      case None => null
    }
  }

  /**设置数据库配置缓存*/
  def setDatabaseSettingCache(list: List[Database]): List[String] = {
    for {
      database <- list
      if hashCacheService.hset(app, s"$DB_HASH_CHACHE_KEY_PREFIX${database.alias}", database.toJson.toString)
    } yield (database.toJson.toString)
  }

  /**获取数据库缓存配置*/
  def getDatabaseSettingCache(database: String): Database = {
    hashCacheService.hget(app, s"$DB_HASH_CHACHE_KEY_PREFIX$database") match {
      case Some(jsonDatabase) => JsonParser(jsonDatabase).convertTo[Database]
      case None => null
    }
  }

  /**清除缓存*/
  def clean() = hashCacheService.del(app)

}