/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: GenerateUpdateBatchSqlEntityTest.java
 * created at: 2014年9月29日
 */
package com.hikvision.dbproxy.services.support.generate;

import org.junit.Assert._
import org.junit.Test
import com.hikvision.dbproxy.services.init.InitDataService
import org.junit.Before
import com.hikvision.dbproxy.entities.Update
import com.hikvision.dbproxy.services.StringUtils
import org.junit.Assert
import com.hikvision.dbproxy.entities.Dml

/**
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年9月29日 下午2:24:52
 * @version: $Rev: 3945 $
 */
class GenerateUpdateBatchSqlEntityTest {

  @Before
  def init() {
    new InitDataService{
      init
    }
  }

  /**
   * Test method for {@link com.hikvision.dbproxy.services.support.generate.GenerateUpdateBatchSqlEntity#generate(java.lang.String, java.lang.String, com.hikvision.dbproxy.entities.Query)}.
   */
  @Test
  def testGenerate() {
    val id = StringUtils.BKDRHash("app1testuser").toString
    val map = Map[String, String]("username" -> "zhangsan", "realname" -> "张三", "sex" -> "1")
    val query = Update(s"${id}_1", map)
    val gibse = new GenerateUpdateBatchSqlEntity(query)
    val batchSqlEntity = gibse.generate("app1", "user")
    val dml = batchSqlEntity.dml
    Assert.assertEquals(Dml.UPDATE, dml)
    val sqlEntityList = batchSqlEntity.list
    val sqlEntity = sqlEntityList(0)
    Assert.assertEquals("update user set username = ?, realname = ?, sex = ? where id = ?", sqlEntity.sql)
  }

  @Test
  def testGenerateSharding() {
    val id = StringUtils.BKDRHash("app1testuser").toString
    val map = Map[String, String]("username" -> "李四", "realname" -> "zhansan", "sex" -> "1")
    val query = Update(s"${id}_1", map)
    val gibse = new GenerateUpdateBatchSqlEntity(query)
    val batchSqlEntity = gibse.generate("app1", "user")
    val dml = batchSqlEntity.dml
    val sqlEntityList = batchSqlEntity.list
    Assert.assertEquals(Dml.UPDATE, dml)
    Assert.assertEquals("delete from user where id = ?", sqlEntityList(0).sql)
    Assert.assertEquals("1", sqlEntityList(0).params(0))
    Assert.assertEquals("insert into user(name,username,_id,realname,age,sex,id,password) values (?,?,?,?,?,?,?,?)", sqlEntityList(1).sql)
  }

  @Test
  def testGenerateSharding2() {
    val id = StringUtils.BKDRHash("app1testuser").toString
    val map = Map[String, String]("username" -> "张三3", "realname" -> "zhansan", "sex" -> "1")
    val query = Update(s"${id}_1", map)
    val gibse = new GenerateUpdateBatchSqlEntity(query)
    val batchSqlEntity = gibse.generate("app1", "user")
    val dml = batchSqlEntity.dml
    val sqlEntityList = batchSqlEntity.list
    Assert.assertEquals(Dml.UPDATE, dml)
    Assert.assertEquals("insert into user3(name,username,_id,realname,age,sex,id,password) values (?,?,?,?,?,?,?,?)", sqlEntityList(1).sql)
  }

}
