/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: DbproxyExecutor.scala
 * created at: 2014年9月23日
 */
package com.hikvision.dbproxy.services.executor

import com.hikvision.dbproxy.entities.ProxySetting

/**
 * 数据库代理执行者
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年9月23日 下午12:50:54
 * @version: $Rev: 3248 $
 */
trait DbproxyExecutor {
  /**
   * 初始化
   */
  def init(setting: ProxySetting)

}