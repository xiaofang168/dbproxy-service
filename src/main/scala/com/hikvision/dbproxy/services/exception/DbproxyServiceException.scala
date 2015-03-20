/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: DbproxyServiceException.scala
 * created at: 2014年9月28日
 */
package com.hikvision.dbproxy.services.exception

/**
 * 数据库代理服务异常信息封装
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年9月28日 下午5:50:33
 * @version: $Rev: 3248 $
 */
class DbproxyServiceException private (ex: RuntimeException) extends RuntimeException(ex) {
  def this(message: String) = this(new RuntimeException(message))
  def this(message: String, throwable: Throwable) = this(new RuntimeException(message, throwable))
}

object DbproxyServiceException {
  def apply(message: String) = new DbproxyServiceException(message)
  def apply(message: String, throwable: Throwable) = new DbproxyServiceException(message, throwable)
}