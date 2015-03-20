/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: GenerateInsertSql.scala
 * created at: 2014年9月22日
 */
package com.hikvision.dbproxy.services.support.generate

import java.net.InetAddress
import scala.annotation.tailrec
import org.slf4j.LoggerFactory
import com.hikvision.dbproxy.entities.Add
import com.hikvision.dbproxy.entities.BatchSqlEntity
import com.hikvision.dbproxy.entities.Dml
import com.hikvision.dbproxy.entities.Query
import com.hikvision.dbproxy.entities.Rule
import com.hikvision.dbproxy.entities.Sharding
import com.hikvision.dbproxy.entities.SqlEntity
import com.hikvision.dbproxy.services.InfoExchangeType
import com.hikvision.dbproxy.services.ProxySettingCacheService
import com.hikvision.dbproxy.services.ServiceConstant
import com.hikvision.dbproxy.services.exception.ModuleServiceException
import com.hikvision.dbproxy.services.factory.ShardingFunFactory
import com.hikvision.dbproxy.services.remote.HttpRemoteCall
import spray.json.JsonParser
import spray.json._
import DefaultJsonProtocol._
import org.apache.http.conn.HttpHostConnectException
import com.hikvision.dbproxy.services.sharding.DatabaseShardingHelper

/**
 * 生成插入批量sql实体对象类
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年9月22日 下午3:24:50
 * @version: $Rev: 3944 $
 */
class GenerateInsertBatchSqlEntity(addObj: Add) extends GenerateDmlBatchSqlEntity {

  val log = LoggerFactory.getLogger(classOf[GenerateInsertBatchSqlEntity])

  def generate(appName: String, resource: String): BatchSqlEntity = {
    
    // 插入的列Set集合
    val columnsSet: Set[String] = addObj.entity.keySet
    // 插入的值
    val insertValues: Array[Object] = addObj.entity.values.toArray

    // 应用对应的缓存配置服务
    val proxySettingCacheService = new ProxySettingCacheService(appName)
    // 缓存资源对象
    val cacheResourceObj = proxySettingCacheService.getResourceSettingCache(resource)
    // 分库对象
    val sharding = cacheResourceObj.sharding getOrElse null
    // 插入需要的参数
    val insertParams: (String, String, String, Array[Object]) = cacheResourceObj.sharding match {
      case Some(sharding) => {
        // 分库需要的参数(分库字段的值,分库的列,分库列对应的值)
        val shardingParams: (Any, Set[String], Array[Object]) = sharding.column match {
          case column @ ServiceConstant.TABLE_PRIMARY_KEY => {
            // 按id分库,获取id值
            val rc = new HttpRemoteCall
            // 代理服务的数据库
            val proxyDbName = ServiceConstant.ID_PROXY_DB
            // 代理数据库ip
            val localhost = InetAddress.getLocalHost
            val localIpAddress = localhost.getHostAddress
            val proxyDbIp = localIpAddress
            // 调用ip代理服务
            try {
              val response = rc.call(s"${ServiceConstant.idproxy}/$proxyDbName/$localIpAddress/$resource", null)
              val map = response.toString.parseJson.convertTo[Map[String, String]]
              val code = map(ServiceConstant.RESPONSE_CODE)
              if (code == InfoExchangeType.C_444) throw ModuleServiceException(InfoExchangeType.C_444, "id代理服务未开启...")
              val id = map(ServiceConstant.RESPONSE_RESULT)
              val newInsertValue: Array[Object] = insertValues :+ id
              (id, columnsSet + column, newInsertValue)
            } catch {
              case e: HttpHostConnectException => throw ModuleServiceException(InfoExchangeType.C_404, "连接id代理服务超时...")
            }
          }
          case column @ _ => {
            (addObj.entity(column), columnsSet, insertValues)
          }
        }
        // 获取匹配的规则
        val matchRules = DatabaseShardingHelper.getMathcShardingRules(shardingParams._1, sharding)
        if (matchRules.length == 0) { //未匹配
          (null, null, null, null)
        } else if (matchRules.length > 1) { // 匹配多个
          val writeDbList = matchRules.map(e => e.write_db)
          val writeDb = writeDbList mkString ";"

          val writeTableList = matchRules.map(e => e.write_table)
          val writeTable = writeTableList mkString ";"

          val insertSqlList = writeTableList map (e => getInsertSql(e, shardingParams._2))
          val insertSql = insertSqlList mkString ";"
          (writeDb, writeTable, insertSql, shardingParams._3)
        } else {
          // 根据匹配的规则获取对应的写入的数据库和表
          val rule = matchRules(0)
          val insertSql = getInsertSql(rule.write_table, shardingParams._2)
          (rule.write_db, rule.write_table, insertSql, shardingParams._3)
        }
      }
      case None => {
        // 插入资源对应的表
        val table = cacheResourceObj.tables(0)
        val insertSql = getInsertSql(table.name, columnsSet)
        (table.belong_db, table.name, insertSql, insertValues)
      }
    }
    // 验证分库表达式与值是否匹配
    if (sharding != null) {
      val value = addObj.entity.get(sharding.column) getOrElse (insertParams._4.last)
      if (insertParams._1 == null) {
        val message = s"未找到匹配的分库表达式! app:$appName resource:$resource value:$value"
        log.error(message)
        throw ModuleServiceException(InfoExchangeType.C_204, message)
      } else if (insertParams._1.split(";").length > 1) {
        val message = s"找到多个匹配的分库表达式! app:$appName resource:$resource value:$value"
        log.error(message)
        log.debug(s"满足多个分库表达式的插入sql语句:${insertParams._3}")
        throw ModuleServiceException(InfoExchangeType.C_205, message)
      }
    }

    val sqlEntityList = List(SqlEntity(appName, insertParams._1, insertParams._2, insertParams._3, insertParams._4))
    BatchSqlEntity(Dml.INSERT, sqlEntityList)

  }

  // 获取insert sql语句
  private def getInsertSql(tableName: String, column: Set[String]): String = {
    val insertColumns = column mkString (",")
    // 参数站位符
    val repeatWildcard = "?," * (column.size)
    // 插入的sql
    s"insert into $tableName($insertColumns) values (${repeatWildcard.dropRight(1)})"
  }

}