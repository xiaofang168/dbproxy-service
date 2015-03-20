/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: GenerateDmlSql.scala
 * created at: 2014年9月22日
 */
package com.hikvision.dbproxy.services.support.generate

import com.hikvision.dbproxy.entities.BatchSqlEntity
import com.hikvision.dbproxy.entities.Query

/**
 * 生成dml数据库操纵语句
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年9月22日 下午3:19:39
 * @version: $Rev: 3944 $
 */
trait GenerateDmlBatchSqlEntity {

  /**
   * 生成DML批量sql实体
   * @appName 应用名称
   * @resource 资源名称
   */
  def generate(appName: String, resource: String): BatchSqlEntity

}