/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: InitDataCache.scala
 * created at: 2014年7月21日
 */
package com.hikvision.dbproxy.services.init

import akka.actor.ActorLogging
import akka.actor.Actor
import com.hikvision.dbproxy.entities.Init
import com.hikvision.dbproxy.cache.CacheService
import com.hikvision.dbproxy.entities.Init
import com.hikvision.dbproxy.entities.Destroy
import com.hikvision.dbproxy.entities.Reset
import com.hikvision.dbproxy.services.Boot

/**
 * 初始化数据缓存
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年7月21日 上午10:32:07
 * @version: $Rev: 3973 $
 */
class InitDataCacheActor extends Actor with InitDataService with CacheService with ActorLogging {
  def receive = {
    case Init => {
      try {
        this.init
      } catch {
        case e: Exception => {
          logger.error(e.getMessage())
          logger.info("服务启动失败!")
          Boot.system.shutdown
        }
      }
    }
    case Destroy => {
      // 销毁缓存里的信息
      this.flushDB
    }
    case Reset => {
      // 重新读取配置信息
      this.flushDB
      this.init
    }
    case _ => log.info("not have this command")
  }

}