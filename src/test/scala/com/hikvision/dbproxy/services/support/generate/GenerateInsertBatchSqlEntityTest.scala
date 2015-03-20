/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: GenerateInsertBatchSqlEntityTest.java
 * created at: 2014年9月22日
 */
package com.hikvision.dbproxy.services.support.generate;

import org.junit.Assert
import org.junit.Test
import com.hikvision.dbproxy.entities.Add
import com.hikvision.dbproxy.entities.Dml
import com.hikvision.dbproxy.entities.Query
import com.hikvision.dbproxy.services.init.InitDataService
import org.junit.Before

/**
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年9月22日 下午6:11:38
 * @version: $Rev: 3945 $
 */
class GenerateInsertBatchSqlEntityTest {



  @Before
  def init() {
    new InitDataService{
      init
    }
  }

  /**
   * Test method for {@link com.hikvision.dbproxy.services.support.generate.GenerateInsertBatchSqlEntity#generate(java.lang.String, java.lang.String, com.hikvision.dbproxy.entities.Query)}.
   */
  @Test
  def testGenerate() {
    val query: Add = Add(Map("id" -> "20140924172515", "username" -> "zhangsan", "realname" -> "张三"))
    val expectSql = "insert into user2(id,username,realname) values (?,?,?)"
    val gibse = new GenerateInsertBatchSqlEntity(query)
    val batchSqlEntity = gibse.generate("app1", "user")
    val dml = Dml.INSERT
    val sqlEntityList = batchSqlEntity.list
    val sqlEntity = sqlEntityList(0)
    val params = sqlEntity.params
    Assert.assertEquals(expectSql, sqlEntity.sql)
    Assert.assertEquals("20140924172515", params(0))
    Assert.assertEquals("zhangsan", params(1))
    Assert.assertEquals("张三", params(2))
    Assert.assertEquals(Dml.INSERT, batchSqlEntity.dml)
  }

  @Test
  def testGenerate2() {
    val query: Add = Add(Map("username" -> "zhangsan", "realname" -> "张三", "sex" -> "1", "age" -> "20"))
    val expectSql = "insert into user2(username,realname,sex,age) values (?,?,?,?)"
    val gibse = new GenerateInsertBatchSqlEntity(query)
    val batchSqlEntity = gibse.generate("app1", "user")
    val sqlEntityList = batchSqlEntity.list
    val sqlEntity = sqlEntityList(0)
    println(sqlEntity)
    val params = sqlEntity.params
    Assert.assertEquals(Dml.INSERT, batchSqlEntity.dml)
    Assert.assertEquals(5, params.length)
  }

}
