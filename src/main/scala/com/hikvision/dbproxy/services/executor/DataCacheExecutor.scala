/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: CacheDataExecutor.scala
 * created at: 2014年9月23日
 */
package com.hikvision.dbproxy.services.executor

import com.hikvision.dbproxy.entities.ProxySetting
import com.hikvision.dbproxy.services.ProxySettingCacheService
import com.hikvision.dbproxy.services.ServiceConstant
import com.hikvision.dbproxy.services.ResourceDataCacheService
import org.slf4j.LoggerFactory

/**
 * 数据缓存执行者
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年9月23日 下午1:04:46
 * @version: $Rev: 3248 $
 */
class DataCacheExecutor extends DbproxyExecutor {

  val logger = LoggerFactory.getLogger(classOf[DataCacheExecutor])

  def init(setting: ProxySetting) {
    import spray.json._
    import DefaultJsonProtocol._
    import com.hikvision.dbproxy.services.support.ExecuteControlService
    import com.hikvision.dbproxy.services.support.QueryConverter

    val service = new ProxySettingCacheService(setting.app_name) with ExecuteControlService
    val queryConverter = new QueryConverter
    val list = setting.resources

    list.par.foreach { resource =>
      if (ServiceConstant.IS_TRUE_1 == resource.is_cache.getOrElse(null)) {
        val rdcs = new ResourceDataCacheService(setting.app_name, resource.name)
        rdcs.clean
        resource.tables.par.foreach {
          table =>
            {
              // 查询所属数据库类型,获取其方言对象
              val databaseObj = service.getDatabaseSettingCache(table.belong_db)
              val dialect = ServiceConstant.DB_DIALECT(databaseObj.dbtype)
              // 查询缓存结果集数
              val count = service.count(setting.app_name, table.belong_db, dialect, table.name)
              // 批处理
              val listPaging = service.page(count, ServiceConstant.batchSize)
              val startTime = System.currentTimeMillis()
              listPaging.par.foreach { page =>
                // 分页查询结果集
                val list = service.pageSelect(setting.app_name, table.belong_db, dialect, s"select * from ${table.name}", Array(), page("offset"), page("limit"))
                // 增加结果集唯一列(_id)
                val addUniqueKeyList = queryConverter.parAddResultUniqueKey(setting.app_name, table.belong_db, table.name, ServiceConstant.TABLE_PRIMARY_KEY, list)
                // 结果集序列化为json对象进行缓存
                val cacheList = queryConverter.toJson(setting.app_name, table.belong_db, table.name, addUniqueKeyList)
                logger.debug(s"应用:${setting.app_name} 资源:${resource.name} 表:${table.name} 缓存集合数据大小:${list.size}")
                rdcs.save(cacheList)
              }
              val endTime = System.currentTimeMillis()
              logger.info(s"应用:${setting.app_name} 资源 :${resource.name} 表:${table.name} 写入缓存耗时:${endTime - startTime}毫秒")
            }
        }
      }
    }
  }

}