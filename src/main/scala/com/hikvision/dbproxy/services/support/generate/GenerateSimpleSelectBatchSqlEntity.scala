/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: GenerateSimpleSelectSql.scala
 * created at: 2014年9月22日
 */
package com.hikvision.dbproxy.services.support.generate

import com.hikvision.dbproxy.entities.BatchSqlEntity
import com.hikvision.dbproxy.entities.Query
import com.hikvision.dbproxy.entities.SimpleSearch
import com.hikvision.dbproxy.services.ServiceConstant
import com.hikvision.dbproxy.entities.Paging
import com.hikvision.dbproxy.services.support.GenerateSqlService
import com.hikvision.dbproxy.entities.Criterion
import com.hikvision.dbproxy.entities.SimpleExpression
import com.hikvision.dbproxy.entities.Dml

/**
 * 生成简单查询批量sql实体类
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年9月22日 下午3:41:37
 * @version: $Rev: 3944 $
 */
class GenerateSimpleSelectBatchSqlEntity(simpleSearch: SimpleSearch) extends GenerateDmlBatchSqlEntity with GenerateSqlService {

  def generate(appName: String, resource: String): BatchSqlEntity = {
    
    // 查询条件
    val conditions = simpleSearch.conditions

    // 获取分页参数
    val offset = conditions.get("$offset") getOrElse ("0")
    val limit = conditions.get("$limit") getOrElse (ServiceConstant.selectSize.toString)
    // 真实分页限制(参数大于配置的分页大小，采用配置的页数)
    val trueLimit = if (limit.toInt > ServiceConstant.selectSize) ServiceConstant.selectSize else limit.toInt

    // 构造分页对象
    val paging = new Paging(offset.toInt, trueLimit)

    // 可where条件的字段
    val whereField: Map[String, String] => Map[String, String] = _.filterKeys(!_.contains("$"))
    val simpleExpressionList = for ((k, v) <- whereField(conditions)) yield (new SimpleExpression(k, v, "$eq"))

    // 排序函数
    val sortItemFun: String => Map[String, String] = in => Map(in.replaceAll("[+-]$", "") -> ((if (in.endsWith("-")) "desc" else "asc")))
    // 排序项数组函数
    val sortItemsFun: String => List[Map[String, String]] = _.split(",") map sortItemFun toList
    // 排序项
    val sortMap = conditions.get("$sort") match {
      case Some(e) => sortItemsFun(e).reduceLeft(_ ++ _)
      case None => null
    }

    val sqlEntityList = produceSqlEntityList(appName, resource, Array("*"), simpleExpressionList.asInstanceOf[List[Criterion]], sortMap, paging)

    BatchSqlEntity(Dml.SELECT, sqlEntityList)

  }

}