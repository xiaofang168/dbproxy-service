/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: InitDataService.scala
 * created at: 2014年7月21日
 */
package com.hikvision.dbproxy.services.init

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source
import scala.util.Failure
import scala.util.Success
import org.slf4j.LoggerFactory
import com.hikvision.dbproxy.entities.ProxySetting
import com.hikvision.dbproxy.services.executor.DataCacheExecutor
import com.hikvision.dbproxy.services.executor.DataSourceExecutor
import com.hikvision.dbproxy.services.executor.SettingExecutor
import com.hikvision.dbproxy.services.init.JsonImplicits.impProxySetting
import com.hikvision.dbproxy.services.invoker.ExecutorInvoker
import spray.json.JsonParser
import com.hikvision.dbproxy.services.executor.ShardingFunExecutor

/**
 * 初始化数据服务
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年7月21日 下午5:31:18
 * @version: $Rev: 3977 $
 */
trait InitDataService {

  def logger = LoggerFactory.getLogger(classOf[InitDataService])

  /**初始化*/
  def init() {
    val proxySettingList = getDbproxySettingList
    try {
      proxySettingList.par.foreach { proxySetting =>
        // 初始化数据库代理配置缓存
        new ExecutorInvoker(proxySetting).init(new SettingExecutor)
        // 初始化数据库代理数据源
        new ExecutorInvoker(proxySetting).init(new DataSourceExecutor)
        // 初始化资源查询数据缓存
        new ExecutorInvoker(proxySetting).init(new DataCacheExecutor)
        // 初始化资源分库函数
        new ExecutorInvoker(proxySetting).init(new ShardingFunExecutor)
      }
      logger.info("资源数据缓存完成!")
    } catch {
      case e: Exception => throw e
    }
  }

  /**获取数据库代理配置列表*/
  def getDbproxySettingList(): List[ProxySetting] = {
    // TODO 查询本地数据库

    // 查询本地数据库代理配置
    // 使用该方式读取配置文件,防止打成jar包无法读取
    val is = getClass().getResourceAsStream("/proxy.json");
    val proxy = Source.fromInputStream(is)("UTF-8").mkString
    //val proxy = Source.fromURL(getClass.getResource("/proxy.json"))("UTF-8").mkString
    logger.info("proxy.json 配置：" + proxy)
    if (!proxy.trim().isEmpty()) {
      val jsonObj = JsonParser(proxy)
      List(jsonObj.convertTo[ProxySetting])
    } else {
      List()
    }
  }

}