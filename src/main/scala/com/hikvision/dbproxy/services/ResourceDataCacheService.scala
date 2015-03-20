/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: ResourceDataCacheService.scala
 * created at: 2014年7月22日
 */
package com.hikvision.dbproxy.services

import com.hikvision.dbproxy.cache.services.ListCacheService

/**
 * 资源数据缓存服务
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年7月22日 上午8:50:34
 * @version: $Rev: 3514 $
 */
class ResourceDataCacheService(app: String, resource: String) {

  val listCacheService: ListCacheService = new ListCacheService

  /**保存资源缓存数据列表*/
  def save(datas: List[String]) = datas.par.map(data => listCacheService.rpush(s"$app$resource", data))

  /**保存资源对象*/
  def save(data: String) = listCacheService.rpush(s"$app$resource", data)

  /**资源缓存数据列表*/
  def list(): List[Option[String]] = listCacheService.lrange(s"$app$resource", 0, -1) getOrElse(null)

  /**统计资源缓存数*/
  def count(): Long = listCacheService.count(s"$app$resource") getOrElse (0)

  /**根据索引查询List*/
  def lrange(startIndex: Int, endIndex: Int): List[Option[String]] = listCacheService.lrange(s"$app$resource", startIndex, endIndex) getOrElse(null)

  /**根据索引查询对象*/
  def lindex(index: Int): String = listCacheService.lindex(s"$app$resource", index) getOrElse (null)

  /**清除缓存数据*/
  def clean() = listCacheService.del(s"$app$resource")

}