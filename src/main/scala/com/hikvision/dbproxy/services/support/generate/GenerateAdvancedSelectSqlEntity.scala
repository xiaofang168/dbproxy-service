/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: GenerateSelectSql.scala
 * created at: 2014年9月22日
 */
package com.hikvision.dbproxy.services.support.generate

import com.hikvision.dbproxy.entities.Query
import com.hikvision.dbproxy.entities.BatchSqlEntity
import com.hikvision.dbproxy.services.support.GenerateSqlService
import com.hikvision.dbproxy.services.ServiceConstant
import com.hikvision.dbproxy.entities.Search
import com.hikvision.dbproxy.entities.Criterion
import com.hikvision.dbproxy.entities.Paging
import com.hikvision.dbproxy.entities.Dml

/**
 * 高级查询
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年9月22日 下午3:24:29
 * @version: $Rev: 3983 $
 */
class GenerateAdvancedSelectSqlEntity(selectObj: Search) extends GenerateDmlBatchSqlEntity with GenerateSqlService {

  def generate(appName: String, resource: String): BatchSqlEntity = {

    // 分页对象
    val paging = selectObj.paging
    // 展示对象
    val display = selectObj.display
    // 条件过滤Map
    val filter = selectObj.filter
    // 排序Map
    val sort = selectObj.sort

    // 解析查询字段函数
    val parseSelectFields: (List[String], List[String], Map[String, String]) => List[String] = (include, exclude, additional) => {
      // 实现include
      val list = include
      val headSelectFields = if (exclude == null) include else list.filterNot(exclude.contains(_))
      if (additional != null) {
        // 连接查询字段(as)
        val joinSelectField: Map[String, String] => Iterable[String] = for ((k: String, v: String) <- _) yield (v + " as " + k)
        // 添加的额外的查询字段数组
        val additionalSelectFields = joinSelectField(additional)
        headSelectFields union additionalSelectFields.toList
      } else
        headSelectFields
    }

    // 设置真实分页参数
    val offset = paging.offset
    val limit = if (paging.limit == 0 || paging.limit > ServiceConstant.selectSize) ServiceConstant.selectSize else paging.limit

    // 真实查询数据库的字段
    val selectFields = parseSelectFields(display.include, display.exclude.getOrElse(null), display.additionalFields.getOrElse(null))

    val sqlEntityList = produceSqlEntityList(appName, resource, selectFields.toArray, filter.values.toList.asInstanceOf[List[Criterion]], sort, Paging(offset, limit))

    BatchSqlEntity(Dml.SELECT, sqlEntityList)

  }

}