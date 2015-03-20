/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: StringUtils.scala
 * created at: 2014年8月18日
 */
package com.hikvision.dbproxy.services

import scala.annotation.tailrec

/**
 * String 字符串工具类
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年8月18日 下午5:49:35
 * @version: $Rev: 3248 $
 */
object StringUtils {

  /**字符串hash值(BKDR Hash Function)*/
  def BKDRHash(str: String): Int = {
    bkdrHashChars(0, str.toList)
  }

  @scala.annotation.tailrec
  private def bkdrHashChars(h: Int, chars: List[Char]): Int = {
    if (!chars.isEmpty) {
      val seed = 131
      if (chars.length > 1) {
        val ha = h * seed + chars.head
        bkdrHashChars(ha, chars.tail)
      } else (h * seed + chars.head) & 0x7FFFFFFF
    } else 0
  }

  /**
   * 一个set集合元素是否包含另一个set集合元素,包含则返回true否则返回false
   * <per>
   * example:  allElements:Set("name","sex","age"); subElements:Set("sort","sex")
   * </per>
   */
  @tailrec
  def contains(allElements: Set[String], subElements: Set[String]): Boolean = {
    if (subElements == null || subElements.isEmpty) true
    else if (!allElements.contains(subElements.head)) false
    else contains(allElements, subElements.tail)
  }

}