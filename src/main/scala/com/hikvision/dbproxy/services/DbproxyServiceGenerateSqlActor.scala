/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: AdvancedSearchActor.scala
 * created at: 2014年7月14日
 */
package com.hikvision.dbproxy.services

import com.hikvision.dbproxy.entities.Add
import com.hikvision.dbproxy.entities.Delete
import com.hikvision.dbproxy.entities.Display
import com.hikvision.dbproxy.entities.Paging
import com.hikvision.dbproxy.entities.Query
import com.hikvision.dbproxy.entities.Search
import com.hikvision.dbproxy.entities.SendEntity
import com.hikvision.dbproxy.entities.SimpleSearch
import com.hikvision.dbproxy.entities.Update
import com.hikvision.dbproxy.services.support.generate._
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.actorRef2Scala
import com.hikvision.dbproxy.services.support.GenerateSqlService
import com.hikvision.dbproxy.entities.ServiceException
import com.hikvision.dbproxy.services.exception.ModuleServiceException
import com.hikvision.dbproxy.entities.IdObjectSearch
import com.hikvision.dbproxy.entities.AggSearch

/**
 * sql生成器处理类
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年7月14日 上午10:44:59
 * @version: $Rev: 3946 $
 */
class DbproxyServiceGenerateSqlActor extends Actor with GenerateSqlService with ActorLogging {

  def receive = {

    case SendEntity(appName: String, resource: String, query: Query) => {
      // 根据query实例化对应的generate对象
      val generate: GenerateDmlBatchSqlEntity = query match {
        //简单查询
        case simpleSearch: SimpleSearch => new GenerateSimpleSelectBatchSqlEntity(simpleSearch)
        // 高级查询
        case search: Search => new GenerateAdvancedSelectSqlEntity(search)
        // 对象查询
        case objSearch: IdObjectSearch => new GenerateIdObjectSearchSqlEntity(objSearch)
        // 聚合查询
        case aggSearch: AggSearch => new GenerateAggSearchBatchSqlEntity(aggSearch)
        // 添加
        case obj: Add => new GenerateInsertBatchSqlEntity(obj)
        // 修改
        case obj: Update => new GenerateUpdateBatchSqlEntity(obj)
        // 删除
        case obj: Delete => new GenerateDeleteBatchSqlEntity(obj)
        // 其他
        case _ => null
      }
      try {
        // 批量sql实体对象
        val batchSqlEntity = generate.generate(appName, resource)
        // 发送批量sql实体对象给调用者
        sender ! batchSqlEntity
      } catch {
        case e: ModuleServiceException => sender ! ServiceException(e.code, e.message)
        case e: Exception => sender ! ServiceException(InfoExchangeType.C_999, "执行错误!")
      }
    }

  }

}