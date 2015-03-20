/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: GenerateUpdateBatchSqlEntity.scala
 * created at: 2014年9月22日
 */
package com.hikvision.dbproxy.services.support.generate

import com.hikvision.dbproxy.entities.Add
import com.hikvision.dbproxy.entities.BatchSqlEntity
import com.hikvision.dbproxy.entities.Delete
import com.hikvision.dbproxy.entities.Dml
import com.hikvision.dbproxy.entities.Query
import com.hikvision.dbproxy.entities.SqlEntity
import com.hikvision.dbproxy.entities.Update
import com.hikvision.dbproxy.services.ProxySettingCacheService
import com.hikvision.dbproxy.services.ServiceConstant
import com.hikvision.dbproxy.services.sharding.DatabaseShardingHelper

/**
 * 生产修改批量sql实体对象
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年9月22日 下午3:25:14
 * @version: $Rev: 3944 $
 */
class GenerateUpdateBatchSqlEntity(updateObj: Update) extends GenerateDmlBatchSqlEntity {

  def generate(appName: String, resource: String): BatchSqlEntity = {
    
    // 获取修改的主键id值
    val id = updateObj.id
    // 获取table对象及其对应的Id
    val (table, tableId) = GenerateHelp.getTableAndId(appName, id)
    // 构造修改的列语句
    val updateColumns = updateObj.entity map {
      case (k, v) => s"$k = ?"
    } mkString (", ")
    // 构造修改的sql语句
    val updateSql = s"update ${table.name} set $updateColumns where id = ?"
    // 修改语句占位符对应的值
    val updateValues: Array[Object] = updateObj.entity.values.toArray :+ tableId

    // 修改sql实体
    val updateSqlEntity = SqlEntity(appName, table.belong_db, table.name, updateSql, updateValues)

    // 分库业务（判断分库key对应的值是否变化）
    val proxySettingCacheService = new ProxySettingCacheService(appName)
    val cacheResourceObj = proxySettingCacheService.getResourceSettingCache(resource)
    val sqlEntityList: List[SqlEntity] = cacheResourceObj.sharding match {
      case Some(sharding) => {
        updateObj.entity.get(sharding.column) match {
          case Some(value) => {
            // 获取当前数据库对应的value及分库表达式(调用执行管控)
            import akka.pattern.ask
            import akka.util.Timeout
            import scala.concurrent.duration._
            import scala.concurrent.Await
            import akka.actor.Props
            import spray.json.JsonParser
            import spray.json._
            import DefaultJsonProtocol._
            import com.hikvision.dbproxy.services.Boot
            import com.hikvision.dbproxy.services.DbproxyServiceExecuteControlActor

            implicit val timeout = Timeout(5 seconds)

            val dbproxyServiceExecuteControlActor = Boot.system.actorOf(Props[DbproxyServiceExecuteControlActor])
            val selectSqlEntityList = List(SqlEntity(appName, table.belong_db, table.name, s"select * from ${table.name} where id=?", Array[Object](tableId)))
            val selectBatchSqlEntity = BatchSqlEntity(Dml.SELECT, selectSqlEntityList)
            val executeFuture = (dbproxyServiceExecuteControlActor ? selectBatchSqlEntity).mapTo[List[Map[String, String]]]
            val result = Await.result(executeFuture, timeout.duration)
            if (result.size == 0) {
              List(updateSqlEntity)
            } else {
              val currentDbValue = result(0)(sharding.column)
              // 相等不需要分库
              if (value.equals(currentDbValue)) {
                List(updateSqlEntity)
              } else {
                /*---------------------调用插入sql生成器(分库sql)-----------------------*/
                // 去掉返回值中额外的_id和id列
                val entityMap = result(0).filter(e => !Set(ServiceConstant.TABLE_PRIMARY_KEY, ServiceConstant.RESOURCE_PRIMARY_KEY).contains(e._1))
                // 更新记录
                val newEntityMap = entityMap.map {
                  case (k, v) => k -> updateObj.entity.getOrElse(k, null)
                }
                val query = Add(newEntityMap)
                val gibse = new GenerateInsertBatchSqlEntity(query)
                val insertBatchSqlEntity = gibse.generate(appName, resource)
                // 根据分片key值获取对应的分库规则
                val rules = DatabaseShardingHelper.getMathcShardingRules(value, sharding)
                // “currentDbValue” 对应的分库表达式
                val currentDbValueRule = DatabaseShardingHelper.getMathcShardingRules(currentDbValue, sharding).head
                // “修改值” 对应的分库表达式
                if (currentDbValueRule.expression.equals(rules(0).expression)) {
                  List(updateSqlEntity)
                } else {
                  val query = Delete(id)
                  // 删除sql语句 和 插入sql语句
                  val gdbse = new GenerateDeleteBatchSqlEntity(query)
                  val deleteBatchSqlEntity = gdbse.generate(appName, resource)
                  List(deleteBatchSqlEntity.list(0), insertBatchSqlEntity.list(0))
                }
              }
            }
          }
          case None => List(updateSqlEntity)
        }
      }
      case None => List(updateSqlEntity)
    }

    BatchSqlEntity(Dml.UPDATE, sqlEntityList)

  }

}