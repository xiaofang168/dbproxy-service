/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: GenerateIdObjectSearchSqlEntity.scala
 * created at: 2014年10月10日
 */
package com.hikvision.dbproxy.services.support.generate

import com.hikvision.dbproxy.entities.BatchSqlEntity
import com.hikvision.dbproxy.entities.Dml
import com.hikvision.dbproxy.entities.IdObjectSearch
import com.hikvision.dbproxy.entities.Query
import com.hikvision.dbproxy.entities.SqlEntity

/**
 * 生成通过id查询对象sql实体对象类
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年10月10日 下午3:22:28
 * @version: $Rev: 3944 $
 */
class GenerateIdObjectSearchSqlEntity(obj: IdObjectSearch) extends GenerateDmlBatchSqlEntity {

  def generate(appName: String, resource: String): BatchSqlEntity = {
    
    // 获取删除的主键id值
    val id = obj.id
    // 获取table对象及其对应的Id
    val (table, tableId) = GenerateHelp.getTableAndId(appName, id)
    // 构造查询sql语句
    val selectSql = s"select * from ${table.name} where id = ?"
    // 查询sql语句参数对应的参数值
    val selectValues: Array[Object] = Array(tableId)

    val list = List(SqlEntity(appName, table.belong_db, table.name, selectSql, selectValues))
    BatchSqlEntity(Dml.SELECT, list)

  }
}