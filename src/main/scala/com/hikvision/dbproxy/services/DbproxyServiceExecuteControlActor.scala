/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: DbproxyServiceExecuteControlActor.scala
 * created at: 2014年8月5日
 */
package com.hikvision.dbproxy.services

import akka.actor.ActorLogging
import akka.actor.Actor
import com.hikvision.dbproxy.entities.BatchSqlEntity
import com.hikvision.dbproxy.entities.Dml
import com.hikvision.dbproxy.entities.SqlEntity
import com.hikvision.dbproxy.services.support.ExecuteControlService
import com.hikvision.dbproxy.services.exception.ModuleServiceException
import com.hikvision.dbproxy.entities.ServiceException
import java.sql.SQLException
import scala.collection.parallel.CompositeThrowable
import org.slf4j.LoggerFactory
import org.apache.commons.dbcp.SQLNestedException

/**
 * 数据库代理服务执行管控actor
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年8月5日 下午4:53:53
 * @version: $Rev: 3976 $
 */
class DbproxyServiceExecuteControlActor extends Actor with ExecuteControlService {

  def receive = {
    case BatchSqlEntity(Dml.SELECT, list: List[SqlEntity]) => {
      import com.hikvision.dbproxy.services.support.QueryConverter

      val queryConverter = new QueryConverter
      try {
        // 查询数据库
        val result = list.par.map {
          case SqlEntity(appName, databaseName, tableName, sql, params) => {
            logger.info(s"select params: ${params mkString ";"}")
            logger.info(s"select sql: $sql")
            val result = select(appName, databaseName, tableName, sql, params)
            if (result != null) {
              queryConverter.addResultUniqueKey(appName, databaseName, tableName, ServiceConstant.TABLE_PRIMARY_KEY, result)
            } else {
              List()
            }
          }
        }
        val resultFlatten = result.seq.toList.flatten
        sender ! resultFlatten
      } catch {
        case e: ModuleServiceException => sender ! ServiceException(e.code, e.message)
        case e: CompositeThrowable => e.throwables.toList(0) match {
          case _: SQLNestedException => sender ! ServiceException(InfoExchangeType.C_109, "被代理数据库连接异常!")
          case _: SQLException => sender ! ServiceException(InfoExchangeType.C_106, "查询sql语句错误!")
        }
        case _: SQLNestedException => sender ! ServiceException(InfoExchangeType.C_109, "被代理数据库连接异常!")
        case e: SQLException => sender ! ServiceException(InfoExchangeType.C_106, "查询sql语句错误!")
        case e: Exception => sender ! ServiceException(InfoExchangeType.C_999, "数据查询错误!")
      }
    }

    case BatchSqlEntity(dml @ (Dml.INSERT | Dml.UPDATE | Dml.DELETE), list: List[SqlEntity]) => {
      // 根据数据库获取同事务sql语句
      val map: Map[String, List[SqlEntity]] = list.groupBy(_.databaseName)
      try {
        val result = map.map {
          case (k, v) => {
            val tableNameList = v.map(_.tableName)
            val sqlList = v.map(_.sql)
            val paramsList = v.map(_.params)
            transactionUpdate(v(0).appName, k, tableNameList, sqlList, paramsList)
          }
        }
        sender ! result.flatten
      } catch {
        case e: ModuleServiceException => sender ! ServiceException(e.code, e.message)
        case e: Exception => sender ! ServiceException(InfoExchangeType.C_999, "数据更新错误!")
      }
    }
  }

}