/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: ArrayUtils.scala
 * created at: 2014年10月20日
 */
package com.hikvision.dbproxy.services

/**
 * 数组工具类
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年10月20日 下午3:39:25
 * @version: $Rev$
 */
object ArrayUtils {

  /***
   * 集合扁平化
   */
  def flatten(ls: List[Any]): List[Any] = ls flatMap {
    case i: List[_] => flatten(i)
    case e => List(e)
  }
  
}