/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: DatabaseShardingException.scala
 * created at: 2014年9月28日
 */
package com.hikvision.dbproxy.services.exception

/**
 * 模块服务异常
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年9月28日 下午6:08:10
 * @version: $Rev: 3248 $
 */
class ModuleServiceException private(val code: String, val message: String, throwable: Throwable) extends DbproxyServiceException(message, throwable)

object ModuleServiceException {
  def apply(code: String, message: String) = new ModuleServiceException(code, message, null)
  def apply(code: String, message: String, throwable: Throwable) = new ModuleServiceException(code, message, throwable)
}

