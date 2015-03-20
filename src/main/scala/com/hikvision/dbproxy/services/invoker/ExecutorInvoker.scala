/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: Invoker.scala
 * created at: 2014年9月23日
 */
package com.hikvision.dbproxy.services.invoker

import com.hikvision.dbproxy.entities.ProxySetting
import com.hikvision.dbproxy.services.executor.DbproxyExecutor
import com.hikvision.dbproxy.services.executor.DbproxyExecutor
import com.hikvision.dbproxy.entities.ProxySetting

/**
 * 所有执行者的中间者
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年9月23日 下午1:54:30
 * @version: $Rev: 3248 $
 */
class ExecutorInvoker(setting: ProxySetting) {

  def init(executor: DbproxyExecutor) {
    executor.init(setting)
  }

}