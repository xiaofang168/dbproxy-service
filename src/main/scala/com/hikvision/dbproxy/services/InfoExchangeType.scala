/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: InfoExchangeType.scala
 * created at: 2014年8月7日
 */
package com.hikvision.dbproxy.services

import com.hikvision.dbproxy.entities.ExceptionCode

/**
 * 数据库代理服务信息交换类型码定义
 * <p>
 * 譬如code:000表示信息返回成功
 * </p>
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年8月7日 下午5:09:59
 * @version: $Rev: 3664 $
 */
object InfoExchangeType extends ExceptionCode {

  /**应用未注册*/
  val C_402 = "402"
  /**应用服务未开启*/
  val C_400 = "400"
  /**拒绝访问*/
  val C_401 = "401"
  /**Redis数据库连接异常*/
  val C_405 = "405"
  /**未知的系统错误*/
  val C_999 = "999"

  /*-------------------数据库操作异常定义-------------------*/
  /**插入数据异常*/
  val C_301 = "301"
  /**更新数据异常*/
  val C_302 = "302"
  /**删除数据异常*/
  val C_303 = "303"

  /*-------------------查询返回码定义-------------------*/
  /**不存在的字段*/
  val C_101 = "101"
  /**查询命令不存在*/
  val C_102 = "102"
  /**查询id格式不正确*/
  val C_104 = "104"
  /**排序命令错误,正确的格式为asc:field;desc:field-*/
  val C_105 = "105"
  /**解析查询dsl错误*/
  val C_103 = "103"
  /**查询sql语句错误*/
  val C_106 = "106"
  /**资源名称错误*/
  val C_107 = "107"
  /**应用名称错误*/  
  val C_108 = "108"
  /**被代理数据库连接异常*/
  val C_109 = "109"

  /*-------------------分库异常码定义-------------------*/
  /**未发现主键id列*/
  val C_201 = "201"
  /**未找到匹配分库的表达式*/
  val C_204 = "204"
  /**分库表达式非互斥*/
  val C_205 = "205"

}