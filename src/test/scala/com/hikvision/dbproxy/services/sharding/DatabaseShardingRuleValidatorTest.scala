/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: DatabaseShardingRuleValidatorTest.scala
 * created at: 2014年8月28日
 */
package com.hikvision.dbproxy.services.sharding

import org.junit.Assert
import org.junit.Test

/**
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年8月28日 上午10:53:49
 * @version: $Rev: 3478 $
 */
class DatabaseShardingRuleValidatorTest {

  val idValidator = new DatabaseShardingRuleValidator("id", "int", "id>300 && id<400")
  val idValidator2 = new DatabaseShardingRuleValidator("id", "Int", "hash(id)%3==0")
  val idValidator3 = new DatabaseShardingRuleValidator("id", "String", "hash(id)%3==0")
  val idValidator4 = new DatabaseShardingRuleValidator("id", "String", "hash(id)%3==1")
  val idValidator5 = new DatabaseShardingRuleValidator("id", "double", "id>300 && id<400")
  val nameValidator = new DatabaseShardingRuleValidator("name", "String", "name.matches(\"[A-Za-z0-9]*\")")
  val nameValidator2 = new DatabaseShardingRuleValidator("name", "String", "name.matches(\"张.*\")")
  val nameValidator3 = new DatabaseShardingRuleValidator("name", "String", "name.matches(\"[张,李].*\")")
  val nameValidator31 = new DatabaseShardingRuleValidator("name", "String", "name.matches(\"[^张,^李].*\")")

  @Test
  def testRangeIsTrue() {
    val f = idValidator.fun
    Assert.assertTrue(f(310))
  }

  @Test
  def testRangeIsTrue2() {
    val f = idValidator5.fun
    Assert.assertTrue(f(306d))
  }

  @Test
  def testRegexIsTrue() {
    val f = nameValidator.fun
    Assert.assertTrue(f("abcd"))
  }

  @Test
  def testHashIsTrue() {
    val f = idValidator2.fun
    Assert.assertFalse(f(12343))
  }

  @Test
  def testHashIsTrue2() {
    val f = idValidator3.fun
    Assert.assertFalse(f("adfad"))
  }

  @Test
  def testHashIsTrue3() {
    val f = idValidator4.fun
    Assert.assertTrue(f("adfad"))
  }

  @Test
  def testRegexName() {
    val f = nameValidator2.fun
    Assert.assertTrue(f("张三"))
    Assert.assertTrue(f("张"))
    Assert.assertTrue(f("张三一"))
  }

  @Test
  def testRegex3() {
    val f = nameValidator3.fun
    Assert.assertTrue(f("张三"))
    Assert.assertTrue(f("李四"))
    Assert.assertTrue(f("张三1"))
    Assert.assertTrue(f("张"))
    Assert.assertFalse(f("王五"))
  }

  @Test
  def testRegex31() {
    val f = nameValidator31.fun
    Assert.assertFalse(f("张三"))
    Assert.assertTrue(f("王五"))
  }
}