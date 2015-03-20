/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: QueryService.scala
 * created at: 2014年8月5日
 */
package com.hikvision.dbproxy.services.support

import java.sql.SQLException
import java.util.HashMap
import scala.runtime.ZippedTraversable3.zippedTraversable3ToTraversable
import org.slf4j.LoggerFactory
import com.hikvision.dbproxy.core.criterion.Projections
import com.hikvision.dbproxy.core.dialect.Dialect
import com.hikvision.dbproxy.core.engine.RowSelection
import com.hikvision.dbproxy.core.impl.CriteriaImpl
import com.hikvision.dbproxy.core.loader.Loader
import com.hikvision.dbproxy.jdbc.DbUtils
import com.hikvision.dbproxy.jdbc.handlers.ArrayHandler
import com.hikvision.dbproxy.jdbc.handlers.MapListHandler
import com.hikvision.dbproxy.jdbc.support.ReadRunnerImpl
import com.hikvision.dbproxy.jdbc.support.WriteRunnerImpl
import com.hikvision.dbproxy.services.ConvertTools
import com.hikvision.dbproxy.services.InfoExchangeType
import com.hikvision.dbproxy.services.JdbcClientPool
import com.hikvision.dbproxy.services.ServiceConstant
import com.hikvision.dbproxy.services.exception.ModuleServiceException
import com.hikvision.dbproxy.services.StringUtils

/**
 * 执行管控服务
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年8月5日 下午5:02:52
 * @version: $Rev: 4010 $
 */
trait ExecuteControlService {

  def logger = LoggerFactory.getLogger(classOf[ExecuteControlService])

  /**
   * 统计查询数目
   */
  def count(appName: String, databaseName: String, dialect: Dialect, tableName: String): Int = {
    val conn = JdbcClientPool.getConnection(appName, databaseName)
    // 生成统计sql语句
    val criteria = new CriteriaImpl(dialect, tableName)
    criteria.setProjection(Projections.rowCount())
    val sql = criteria.getSQLString()
    //查询总数
    val readRunner = new ReadRunnerImpl()
    val rsh = new ArrayHandler()
    val objs = readRunner.query(conn, sql, rsh)
    DbUtils.close(conn)
    objs(0).toString().toInt
  }

  /**
   * 用未分页的sql语句来实现分页查询
   * @sql 为未分页的sql语句
   */
  def pageSelect(appName: String, databaseName: String, dialect: Dialect, sql: String, params: Array[Object], offset: Int, limit: Int): List[Map[String, _]] = {
    val conn = JdbcClientPool.getConnection(appName, databaseName)
    val loader = new Loader(dialect) {
      override def getSQLString(): String = {
        null
      }
    }
    val selection = new RowSelection()
    selection.setFirstRow(offset)
    selection.setMaxRows(limit)
    val pagingSql = loader.getPagingSQLString(sql, selection)
    val parameterValues = loader.getParameterValues(params, selection)
    val readRunner = new ReadRunnerImpl(true)
    val rsh = new MapListHandler()
    val list = readRunner.query(conn, pagingSql, rsh, parameterValues: _*)
    DbUtils.close(conn)
    val scalaList: List[java.util.Map[String, _]] = scala.collection.JavaConversions.asScalaBuffer(list).toList.asInstanceOf[List[HashMap[String, _]]]
    val mapCacheList = scalaList.par.map(e => {
      ConvertTools.javaMap2ScalaMap(e)
    })
    mapCacheList.toList
  }

  /**
   * 查询
   */
  def select(appName: String, databaseName: String, tableName: String, sql: String, params: Array[Object]): List[Map[String, AnyRef]] = {
    val readRunner = new ReadRunnerImpl(true)
    val rsh = new MapListHandler()
    val conn = JdbcClientPool.getConnection(appName, databaseName)
    val javaList = readRunner.query(conn, sql, rsh, params: _*)
    DbUtils.close(conn)
    if (javaList != null && !javaList.isEmpty()) {
      val scalaList = ConvertTools.javaList2ScalaList(javaList)
      scalaList.map(e => {
        ConvertTools.javaMap2ScalaMap(e)
      }) toList
    } else {
      null
    }
  }

  /**
   * 手动提交事务更新
   */
  def transactionUpdate(appName: String, databaseName: String, tableNameList: List[String], sqlList: List[String], paramsList: List[Array[Object]]): List[Map[String, AnyRef]] = {
    val writeRunner = new WriteRunnerImpl(true)
    val conn = JdbcClientPool.getConnection(appName, databaseName)
    conn.setAutoCommit(false)
    try {
      val result = (tableNameList, sqlList, paramsList).zipped.toList map {
        case (tableName, sql, params) => {
          logger.info(s"params: ${params mkString ";"}")
          val id = if (sql.startsWith("insert")) {
            logger.info(s"insert sql: $sql")
            // 获取数据库自动生成的id
            writeRunner.generateIdByInsert(conn, sql, params: _*)
          } else {
            logger.info(s"update sql: $sql")
            writeRunner.update(conn, sql, params: _*)
            0
          }
          // 真实的id
          val tureId = if (id == 0) s"${params.last}" else s"$id"
          val hash = StringUtils.BKDRHash(s"$appName$databaseName$tableName")
          // 生成的资源伪id
          val _id = s"${hash}_$tureId"
          Map(sql.substring(0, 6) -> Map(ServiceConstant.TABLE_PRIMARY_KEY -> tureId, ServiceConstant.RESOURCE_PRIMARY_KEY -> _id))
        }
      }
      conn.commit()
      result
    } catch {
      case e: SQLException => {
        conn.rollback()
        logger.error(e.getMessage())
        throw ModuleServiceException(InfoExchangeType.C_302, "更新失败")
      }
      case e: Exception => {
        logger.error(e.getMessage())
        throw e
      }
    }
  }

  /**
   * 对总数进行分页
   */
  def page(rowCount: Int, pageSize: Int): List[Map[String, Int]] = {
    val pageCount = if (rowCount % pageSize == 0) rowCount / pageSize else rowCount / pageSize + 1
    val listPaging = for (i <- 1 to pageCount) yield {
      val startRow = pageSize * (i - 1)
      val endRow = if (pageSize * i > rowCount) rowCount else pageSize * i
      Map("offset" -> startRow, "limit" -> (endRow - startRow))
    }
    listPaging.toList
  }

}