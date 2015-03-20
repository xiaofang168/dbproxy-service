/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: Boot.scala
 * created at: 2014年7月11日
 */
package com.hikvision.dbproxy.services

import akka.actor.ActorSystem
import akka.actor.Props
import com.hikvision.dbproxy.services.init.InitDataCacheActor
import com.hikvision.dbproxy.entities.Init
import ch.qos.logback.classic.LoggerContext
import org.slf4j.LoggerFactory
import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.core.util.StatusPrinter

/**
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年7月11日 下午5:27:59
 * @version: $Rev: 3566 $
 */
object Boot {

  implicit val system = ActorSystem("ServiceSystem")

  def main(args: Array[String]) {
    // logback配置
    val lc = LoggerFactory.getILoggerFactory().asInstanceOf[LoggerContext]
    val configurator = new JoranConfigurator()
    configurator.setContext(lc)
    lc.reset()
    configurator.doConfigure(getClass.getResource("/logback.xml"))
    StatusPrinter.printInCaseOfErrorsOrWarnings(lc)

    // 数据库代理服务sql生成器actor
    val dbproxyServiceGenerateSqlActor = system.actorOf(Props[DbproxyServiceGenerateSqlActor], "dbproxyServiceGenerateSqlActor")
    // 初始化缓存actor
    val dbproxyServiceInitDataCacheActor = system.actorOf(Props[InitDataCacheActor], "dbproxyServiceInitDataCacheActor")
    // 数据库代理缓存actor
    val dbproxyServiceCacheActor = system.actorOf(Props[DbproxyServiceCacheActor], "dbproxyServiceCacheActor")
    // 数据库代理执行管控actor
    val dbproxyServiceExecuteControlActor = system.actorOf(Props[DbproxyServiceExecuteControlActor], "dbproxyServiceExecuteControlActor")
    // 初始化
    dbproxyServiceInitDataCacheActor ! Init
  }

}