/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: GenerateDeleteSql.scala
 * created at: 2014年9月22日
 */
package com.hikvision.dbproxy.services.support.generate

import com.hikvision.dbproxy.entities.BatchSqlEntity
import com.hikvision.dbproxy.entities.Delete
import com.hikvision.dbproxy.entities.Dml
import com.hikvision.dbproxy.entities.Query
import com.hikvision.dbproxy.entities.SqlEntity

/**
 * 生成删除批量sql实体对象类
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年9月22日 下午3:25:02
 * @version: $Rev: 3944 $
 */
class GenerateDeleteBatchSqlEntity(deleteObj: Delete) extends GenerateDmlBatchSqlEntity {

  def generate(appName: String, resource: String): BatchSqlEntity = {
    
	// 获取删除的主键id值
    val id = deleteObj.id
    // 获取table对象及其对应的Id
    val (table, tableId) = GenerateHelp.getTableAndId(appName, id)
    // 构造删除sql语句
    val deleteSql = s"delete from ${table.name} where id = ?"
    // 删除sql语句参数对应的参数值
    val deleteValues: Array[Object] = Array(tableId)

    val list = List(SqlEntity(appName, table.belong_db, table.name, deleteSql, deleteValues))
    BatchSqlEntity(Dml.DELETE, list)

  }

}