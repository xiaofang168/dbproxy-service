/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: GenerateSqlServiceTest.java
 * created at: 2014年8月8日
 */
package com.hikvision.dbproxy.services.support;

import org.junit.Test
import com.hikvision.dbproxy.entities.Paging
import com.hikvision.dbproxy.entities.SimpleExpression
import com.hikvision.dbproxy.entities.Criterion
import com.hikvision.dbproxy.entities.Fun
import com.hikvision.dbproxy.entities.Terms
import com.hikvision.dbproxy.entities.Count
import com.hikvision.dbproxy.entities.CountFun
import com.hikvision.dbproxy.entities.AggFunsEnum
import com.hikvision.dbproxy.core.dialect.MySQLDialect
import org.junit.Assert
import com.hikvision.dbproxy.entities.Operation
import com.hikvision.dbproxy.entities.LogicalExpression

/**
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年8月8日 下午3:26:23
 * @version: $Rev: 4006 $
 */
class GenerateSqlServiceTest {

  @Test
  def testProductSqlEntityList() {
    val service = new GenerateSqlServiceTest with GenerateSqlService
    val whereField: Map[String, String] => Map[String, String] = _.filterKeys(!_.contains("$"))
    val simpleExpressionList = for ((k, v) <- whereField(Map("id" -> "12"))) yield (new SimpleExpression(k, v, "$eq"))
    val sqlEntityList = service.produceSqlEntityList("app1", "log", Array("name", "realname"), simpleExpressionList.asInstanceOf[List[Criterion]], Map[String, String]("id" -> "desc"), Paging(0, 10))
    println(sqlEntityList(0).sql)
  }

  @Test
  def testProductAggSqlEntityList() {
    val service = new GenerateSqlServiceTest with GenerateSqlService
    val whereField: Map[String, String] => Map[String, String] = _.filterKeys(!_.contains("$"))
    val simpleExpressionList = for ((k, v) <- whereField(Map("id" -> "12"))) yield (new SimpleExpression(k, v, "$eq"))
    val funs = Map[String, Fun]("count_sex" -> CountFun(Count("sex")))
    val terms = Terms(Option(List[String]("sex")), Option(Map[String, String]("count_sex" -> "asc")))
    val sqlEntityList = service.produceAggSqlEntityList("app1", "log", simpleExpressionList.asInstanceOf[List[Criterion]], terms, funs)
    println(sqlEntityList(0).sql)
  }

  @Test
  def testProduceSearchSql() {
    val service = new GenerateSqlServiceTest with GenerateSqlService
    val dialect = new MySQLDialect
    val whereField: Map[String, String] => Map[String, String] = _.filterKeys(!_.contains("$"))
    val simpleExpressionList = for ((k, v) <- whereField(Map("id" -> "12", "name" -> "张"))) yield (new SimpleExpression(k, v, "$eq"))
    val funs = null
    val terms = null
    val expression = simpleExpressionList.asInstanceOf[List[Criterion]]
    val sort = Map("id" -> "desc")
    val paging = Paging(0, 10)
    val (sql, params) = service.produceSearchSqlAndParams(dialect, "user", Array("*"), expression, sort, paging, terms, funs)
    Assert.assertEquals("select this_.* from user this_ where this_.id=? and this_.name=? order by this_.id desc limit ?", sql)
  }

  @Test
  def testProduceSqlEntityList() {
    val service = new GenerateSqlServiceTest with GenerateSqlService
    val dialect = new MySQLDialect
    val paging = Paging(0, 10)
    val sqlEntityList = service.produceSearchSqlAndParams(dialect, "user", Array("*"), List(new SimpleExpression("username", "1", "$eq")), null, paging, null, null)
    println(sqlEntityList)
  }

  @Test
  def testProduceAggSearchSql() {
    val service = new GenerateSqlServiceTest with GenerateSqlService
    val dialect = new MySQLDialect
    val whereField: Map[String, String] => Map[String, String] = _.filterKeys(!_.contains("$"))
    val simpleExpressionList = for ((k, v) <- whereField(Map("id" -> "12"))) yield (new SimpleExpression(k, v, "$eq"))
    val funs = Map[String, Fun]("count_sex" -> CountFun(Count("sex")))
    val terms = Terms(Option(List[String]("sex")), Option(Map[String, String]("count_sex" -> "asc")))
    val expression = simpleExpressionList.asInstanceOf[List[Criterion]]
    val sort = null
    val paging = null
    val (sql, params) = service.produceSearchSqlAndParams(dialect, "user", Array("*"), expression, sort, paging, terms, funs)
    Assert.assertEquals("select count(this_.sex) as count_sex, this_.sex as sex from user this_ where this_.id=? group by this_.sex order by count_sex asc", sql)
  }

  @Test
  def testProduceAggSearchNotOrderSql() {
    val service = new GenerateSqlServiceTest with GenerateSqlService
    val dialect = new MySQLDialect
    val whereField: Map[String, String] => Map[String, String] = _.filterKeys(!_.contains("$"))
    val simpleExpressionList = for ((k, v) <- whereField(Map("id" -> "12"))) yield (new SimpleExpression(k, v, "$eq"))
    val funs = Map[String, Fun]("count_sex" -> CountFun(Count("sex")))
    val terms = Terms(Option(List[String]("sex")), None)
    val expression = simpleExpressionList.asInstanceOf[List[Criterion]]
    val sort = null
    val paging = null
    val (sql, params) = service.produceSearchSqlAndParams(dialect, "user", Array("*"), expression, sort, paging, terms, funs)
    Assert.assertEquals("select count(this_.sex) as count_sex, this_.sex as sex from user this_ where this_.id=? group by this_.sex", sql)
  }

  @Test
  def testCheckIsAndEqExpression() {
    val service = new GenerateSqlServiceTest with GenerateSqlService
    val result1 = service.checkIsAndEqExpression(new SimpleExpression("username", "张三", Operation.EQ))
    val result2 = service.checkIsAndEqExpression(new SimpleExpression("username", "张三", Operation.GE))
    val resultAnd = service.checkIsAndEqExpression(new LogicalExpression(Operation.AND, new SimpleExpression("username", "张三", Operation.EQ), new SimpleExpression("age", 20, Operation.GE)))
    val resultOr = service.checkIsAndEqExpression(new LogicalExpression(Operation.OR, new SimpleExpression("username", "张三", Operation.EQ), new SimpleExpression("age", 20, Operation.GE)))
    val resultManyAnd = service.checkIsAndEqExpression(new LogicalExpression(Operation.AND, new SimpleExpression("username", "张三", Operation.LIKE), new SimpleExpression("age", 20, Operation.GE), new SimpleExpression("sex", 1, Operation.EQ), new SimpleExpression("dept", "002", Operation.EQ)))
    val resultManyOr = service.checkIsAndEqExpression(new LogicalExpression(Operation.OR, new SimpleExpression("username", "张三", Operation.LIKE), new SimpleExpression("age", 20, Operation.GE), new SimpleExpression("sex", 1, Operation.EQ), new SimpleExpression("dept", "002", Operation.EQ)))
    val resultManyAnd1 = service.checkIsAndEqExpression(new LogicalExpression(Operation.AND, new SimpleExpression("username", "张三", Operation.LIKE), new SimpleExpression("age", 20, Operation.GE), new SimpleExpression("sex", 1, Operation.NEQ), new SimpleExpression("dept", "002", Operation.NEQ)))
    Assert.assertTrue(result1)
    Assert.assertFalse(result2)
    Assert.assertTrue(resultAnd)
    Assert.assertFalse(resultOr)
    Assert.assertTrue(resultManyAnd)
    Assert.assertFalse(resultManyOr)
    Assert.assertFalse(resultManyAnd1)
  }

  @Test
  def testConvertExpressions2Map() {
    val service = new GenerateSqlServiceTest with GenerateSqlService
    val expressions = List(new SimpleExpression("username", "张三", Operation.EQ))
    val expressions2 = List(new LogicalExpression(Operation.AND, new SimpleExpression("username", "张三", Operation.LIKE), new SimpleExpression("age", 20, Operation.GE), new SimpleExpression("sex", 1, Operation.EQ), new SimpleExpression("dept", "002", Operation.EQ)))
    val entityMap = service.convertExpressions2Map(expressions)
    val entityMap2 = service.convertExpressions2Map(expressions2)
    Assert.assertEquals("张三", entityMap("username"))
    Assert.assertEquals("张三", entityMap2("username"))
  }

}
