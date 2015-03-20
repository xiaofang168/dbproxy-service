/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: DbproxyServiceCacheActor.scala
 * created at: 2014年7月23日
 */
package com.hikvision.dbproxy.services

import com.hikvision.dbproxy.entities.SelectCache
import akka.actor.Actor
import akka.actor.ActorLogging
import com.hikvision.dbproxy.services.support.DbproxyServiceCacheService
import com.hikvision.dbproxy.entities.ServiceException
import com.hikvision.dbproxy.services.exception.ModuleServiceException
import java.net.ConnectException
/**
 * 数据库代理服务缓存actor
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年7月23日 下午1:30:16
 * @version: $Rev: 3663 $
 */
class DbproxyServiceCacheActor extends Actor with DbproxyServiceCacheService {

  def receive = {
    case SelectCache(app, resource, conditions) => {
      val startTime = System.currentTimeMillis()
      try {
        val result = select(app, resource, conditions)
        val endTime = System.currentTimeMillis()
        logger.info(s"查询缓存应用:$app 资源:$resource 结果集大小:${result.size} 耗时:${endTime - startTime}毫秒")
        sender ! result
      } catch {
        case e: ModuleServiceException => {
          logger.error(e.message)
          sender ! ServiceException(InfoExchangeType.C_101, e.message)
        }
        case e: RuntimeException => {
          if (e.getCause.isInstanceOf[ConnectException]) {
            logger.error(e.getMessage())
            sender ! ServiceException(InfoExchangeType.C_405, "Redis数据库未连接!")
          }
        }
      }
    }
  }

}