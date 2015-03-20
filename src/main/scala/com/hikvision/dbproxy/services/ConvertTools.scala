/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: ConvertTools.scala
 * created at: 2014年8月6日
 */
package com.hikvision.dbproxy.services

import scala.collection.JavaConverters._
/**
 * 转换工具类
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年8月6日 下午2:30:06
 * @version: $Rev: 3248 $
 */
object ConvertTools {

  /**
   * java Map 转换为 scala Map
   */
  def javaMap2ScalaMap[K, V](jmap: java.util.Map[K, V]): Map[K, V] = {
    jmap.asScala.toMap
  }

  /**
   * java List　转换　Scala List
   */
  def javaList2ScalaList[T](jlist: java.util.List[T]): List[T] = {
    jlist.asScala.toList
  }

}