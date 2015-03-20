/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: GenerateAggsSearchBatchSqlEntity.scala
 * created at: 2014年10月16日
 */
package com.hikvision.dbproxy.services.support.generate

import com.hikvision.dbproxy.entities.AggSearch
import com.hikvision.dbproxy.entities.BatchSqlEntity
import com.hikvision.dbproxy.entities.Criterion
import com.hikvision.dbproxy.entities.Dml
import com.hikvision.dbproxy.entities.Query
import com.hikvision.dbproxy.services.support.GenerateSqlService

/**
 * 生成聚合查询批量sql实体对象类
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年10月16日 下午4:51:52
 * @version: $Rev$
 */
class GenerateAggSearchBatchSqlEntity(as: AggSearch) extends GenerateDmlBatchSqlEntity with GenerateSqlService {

  def generate(appName: String, resource: String): BatchSqlEntity = {
    
	val filter = as.agg.filter match {
      case Some(map) => map.values.toList.asInstanceOf[List[Criterion]]
      case None => null
    }

    val sqlEntityList = produceAggSqlEntityList(appName, resource, filter, as.agg.terms.getOrElse(null), as.agg.funs)

    BatchSqlEntity(Dml.SELECT, sqlEntityList)

  }
}