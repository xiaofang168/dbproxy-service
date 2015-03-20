/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: GenerateSqlService.scala
 * created at: 2014年8月8日
 */
package com.hikvision.dbproxy.services.support

import com.hikvision.dbproxy.core.criterion.{ Criterion => CoreCriterion }
import com.hikvision.dbproxy.services.ProxySettingCacheService
import com.hikvision.dbproxy.entities.SqlEntity
import com.hikvision.dbproxy.entities.Paging
import com.hikvision.dbproxy.core.criterion.Restrictions
import com.hikvision.dbproxy.entities.Criterion
import com.hikvision.dbproxy.entities.LogicalExpression
import com.hikvision.dbproxy.entities.SimpleExpression
import com.hikvision.dbproxy.core.impl.CriteriaImpl
import com.hikvision.dbproxy.services.ServiceConstant
import com.hikvision.dbproxy.entities.Operation._
import com.hikvision.dbproxy.core.criterion.Order
import com.hikvision.dbproxy.entities.Terms
import com.hikvision.dbproxy.entities.Fun
import com.hikvision.dbproxy.core.criterion.projection.ProjectionList
import com.hikvision.dbproxy.core.criterion.Projections
import com.hikvision.dbproxy.entities.AggFunsEnum
import com.hikvision.dbproxy.core.{ Criteria => CoreCriteria }
import com.hikvision.dbproxy.core.dialect.Dialect
import com.hikvision.dbproxy.entities.Table
import com.hikvision.dbproxy.services.ArrayUtils
import com.hikvision.dbproxy.services.sharding.DatabaseShardingHelper
import com.hikvision.dbproxy.services.exception.ModuleServiceException
import com.hikvision.dbproxy.services.InfoExchangeType

/**
 * sql生成器特质
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年8月8日 下午3:11:44
 * @version: $Rev: 3985 $
 */
trait GenerateSqlService {

  /**
   * 生成sql实体列表
   */
  def produceSqlEntityList(appName: String, resource: String, selectFields: Array[String], expressions: List[Criterion], sort: Map[String, String], paging: Paging): List[SqlEntity] = {
    produce(appName, resource, expressions, (dialect, tableName) => produceSearchSqlAndParams(dialect, tableName, selectFields, expressions, sort, paging, null, null))
  }

  /**
   * 生成聚合查询sql实体列表
   */
  def produceAggSqlEntityList(appName: String, resource: String, expressions: List[Criterion], terms: Terms, funs: Map[String, Fun]): List[SqlEntity] = {
    produce(appName, resource, expressions, (dialect, tableName) => produceSearchSqlAndParams(dialect, tableName, Array("*"), expressions, null, null, terms, funs))
  }

  /**
   * 检查是否是and eq 表达式
   */
  def checkIsAndEqExpression(criterion: Criterion): Boolean = criterion match {
    case s: SimpleExpression => if (EQ.equals(s.getOp)) true else false
    case le: LogicalExpression => {
      if (AND.equals(le.getOp)) {
        val lf = le.getLhs.asInstanceOf[SimpleExpression]
        val rf = le.getRhs.asInstanceOf[SimpleExpression]
        val others = le.getOthers.map(_.asInstanceOf[SimpleExpression]).toList
        val exFlatten: List[SimpleExpression] = ArrayUtils.flatten(List(lf, rf, others)).asInstanceOf[List[SimpleExpression]]
        exFlatten.exists(e => EQ.equals(e.getOp))
      } else {
        false
      }
    }
  }

  /**
   * 转换expression表达式为map
   */
  def convertExpressions2Map(expressions: List[Criterion]): Map[String, Any] = {
    val entityMap = expressions.map {
      case s: SimpleExpression => Map(s.getField -> s.getValue)
      case le: LogicalExpression => {
        val lf = le.getLhs.asInstanceOf[SimpleExpression]
        val rf = le.getRhs.asInstanceOf[SimpleExpression]
        val others = le.getOthers.map(_.asInstanceOf[SimpleExpression])
        val om = others map {
          case s: SimpleExpression => s.getField -> s.getValue
        }
        Map(lf.getField -> lf.getValue, rf.getField -> rf.getValue) ++ om.toMap
      }
    }
    entityMap.flatten.toMap
  }

  /**
   * 根据应用名称、资源名称及查询表生产对应的sql实体
   */
  private def produce(appName: String, resource: String, expressions: List[Criterion], search: (Dialect, String) => (String, Array[Object])): List[SqlEntity] = {
    // 应用对应的缓存配置服务
    val proxySettingCacheService = new ProxySettingCacheService(appName)
    // 获取缓存资源对象
    val cacheResourceObj = proxySettingCacheService.getResourceSettingCache(resource)
    if (cacheResourceObj == null) {
      throw ModuleServiceException(InfoExchangeType.C_107, "查询资源不存在!")
    }
    // 获取表对象及其对应的方言集合
    val getResourceTableAndDialects: (List[Table]) => List[(Table, Dialect)] = (tables) => {
      tables.map(table => {
        val database = proxySettingCacheService.getDatabaseSettingCache(table.belong_db)
        val dialect = ServiceConstant.DB_DIALECT(database.dbtype.toLowerCase())
        (table, dialect)
      })
    }
    // 分库业务
    val tableAndDialects: List[(Table, Dialect)] = cacheResourceObj.sharding match {
      case Some(sharding) => {
        // 过滤and eq的表达式
        val andEqExpressions = expressions.filter(checkIsAndEqExpression(_))
        val entityMap = convertExpressions2Map(andEqExpressions)
        // 获取查询字段中分库的key及value
        entityMap.get(sharding.column) match {
          case Some(value) => {
            // 根据资源分片获取查询表
            val rules = DatabaseShardingHelper.getMathcShardingRules(value, sharding)
            rules.map(rule => {
              val database = proxySettingCacheService.getDatabaseSettingCache(rule.write_db)
              val dialect = ServiceConstant.DB_DIALECT(database.dbtype.toLowerCase())
              val table = proxySettingCacheService.getTableSettingCache(rule.write_db, rule.write_table)
              (table, dialect)
            })
          }
          case None => getResourceTableAndDialects(cacheResourceObj.tables)
        }
      }
      case None => getResourceTableAndDialects(cacheResourceObj.tables)
    }
    // 对每张表生成对应的查询语句
    tableAndDialects.map(td => {
      val (sql, params): (String, Array[Object]) = search(td._2, td._1.name)
      // 组装sql实体对象
      SqlEntity(appName, td._1.belong_db, td._1.name, sql, params)
    }).toList
  }

  /**
   * 生产查询sql
   */
  def produceSearchSqlAndParams(dialect: Dialect, tableName: String, selectFields: Array[String], expressions: List[Criterion], sort: Map[String, String], paging: Paging, terms: Terms, funs: Map[String, Fun]): (String, Array[Object]) = {
    val criteria: CoreCriteria = new CriteriaImpl(dialect, tableName, selectFields)
    // 设置条件
    if (expressions != null) {
      expressions.foreach {
        case s: SimpleExpression => {
          criteria.add(getCoreSimpleExpression(s))
        }
        case le: LogicalExpression => {
          criteria.add(getCoreLogicalExpression(le))
        }
      }
    }
    // 设置分页
    if (paging != null) {
      criteria.setFirstResult(paging.offset)
      criteria.setMaxResults(paging.limit)
    }
    // 设置排序
    if (sort != null && !sort.isEmpty) {
      sort map {
        case (k, v) => {
          if (v == "asc") criteria.addOrder(Order.asc(k)) else criteria.addOrder(Order.desc(k))
        }
      }
    }
    // 投影集合
    val pl = new ProjectionList()
    // 设置使用聚集函数
    if (funs != null && !funs.isEmpty) {
      funs.foreach {
        case (name, fun) => {
          fun.getName match {
            case AggFunsEnum.COUNT => pl.add(Projections.count(fun.getField), name)
            case AggFunsEnum.AVG => pl.add(Projections.avg(fun.getField), name)
            case AggFunsEnum.MAX => pl.add(Projections.max(fun.getField), name)
            case AggFunsEnum.MIN => pl.add(Projections.min(fun.getField), name)
            case AggFunsEnum.SUM => pl.add(Projections.sum(fun.getField), name)
          }
        }
      }
    }
    // 获取分组字段和排序Map
    val (groupFields, sortMap): (List[String], Map[String, String]) = if (terms == null) (null, null) else (terms.fields.getOrElse(null), terms.sort.getOrElse(null))
    // 设置分组
    if (groupFields != null) {
      groupFields.foreach(field => pl.add(Projections.groupProperty(field), field))
    }
    // 解析排序Map,转换为排序sql语句片段
    val parseSortMap2Sql: Map[String, String] => String = sortMap => {
      if (sortMap == null) ""
      else {
        s" order by ${
          sortMap.map {
            case (field, order) => s"$field $order"
          } mkString ","
        }"
      }
    }
    // append group sort sql clause
    val appendGroupSortSql = parseSortMap2Sql(sortMap)

    // 设置投影
    if (pl.getLength() != 0) {
      criteria.setProjection(pl)
    }

    // 组装sql语句及其语句中占位符对应的参数值
    (criteria.getSQLString() + appendGroupSortSql, criteria.getParameterValues())
  }

  /**
   * 获取内部简单表达式语句
   */
  def getCoreSimpleExpression(s: SimpleExpression): CoreCriterion = s.getOp match {
    case EQ => Restrictions.eq(s.getField, s.getValue)
    case NEQ => Restrictions.ne(s.getField, s.getValue)
    case LT => Restrictions.lt(s.getField, s.getValue)
    case LE => Restrictions.le(s.getField, s.getValue)
    case GT => Restrictions.gt(s.getField, s.getValue)
    case GE => Restrictions.ge(s.getField, s.getValue)
    case IN => {
      val listValue = s.getValue.asInstanceOf[List[String]]
      val objectValue = listValue.toArray.asInstanceOf[Array[Object]]
      Restrictions.in(s.getField, objectValue)
    }
    case NIN => {
      val listValue = s.getValue.asInstanceOf[List[String]]
      val objectValue = listValue.toArray.asInstanceOf[Array[Object]]
      Restrictions.not(Restrictions.in(s.getField, objectValue))
    }
    case BETWEEN => {
      val values = s.getValue.asInstanceOf[List[String]]
      //低区间值
      val lo = values(0)
      //高区间值
      val hi = values(1)
      Restrictions.between(s.getField, lo, hi)
    }
    case NULL => if (s.getValue.asInstanceOf[Boolean]) Restrictions.isNull(s.getField) else Restrictions.isNotNull(s.getField)
    case LIKE => Restrictions.like(s.getField, s.getValue)
  }

  /**
   * 获取内部逻辑表达式语句
   */
  def getCoreLogicalExpression(le: LogicalExpression): CoreCriterion = {
    val lhs = getCoreSimpleExpression(le.getLhs.asInstanceOf[SimpleExpression])
    val rhs = getCoreSimpleExpression(le.getRhs.asInstanceOf[SimpleExpression])
    val others = le.getOthers.map(e => {
      getCoreSimpleExpression(e.asInstanceOf[SimpleExpression])
    })
    le.getOp match {
      case OR => {
        Restrictions.or(lhs, rhs, others: _*)
      }
      case AND => {
        Restrictions.and(lhs, rhs, others: _*)
      }
    }
  }

}