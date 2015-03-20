/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: DatabaseSharding.scala
 * created at: 2014年8月28日
 */
package com.hikvision.dbproxy.services.sharding

import com.twitter.util.Eval

/**
 * 数据库切分规则验证
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年8月28日 上午10:36:30
 * @version: $Rev: 3248 $
 */
class DatabaseShardingRuleValidator(columnName: String, t: String, expression: String) {

  /**脚本类名称*/
  private val SCRIPT_CLASS_NAME = "Sharding"

  def script: String = {
    // 获取列对应的类型
    val columnValueType = t.toLowerCase() match {
      case "string" => "String"
      case "int" => "Int"
      case "double" => "Double"
    }
    // 数据库分库脚本
    s"""|trait $SCRIPT_CLASS_NAME extends ($columnValueType => Boolean) {
        |  import com.hikvision.dbproxy.services.StringUtils
	
	    |  implicit def toStr[T](x: T)(implicit ev: (Int with Float with Double with Long) <:< T) = x.toString
    
		|  def apply($columnName: $columnValueType): Boolean = {
		|    $expression
		|  }
				    
		|  def hash(s: String): Int = {
		|    StringUtils.BKDRHash(s)
		|  }
	  |}""".stripMargin
  }

  private val derived = (new Eval).apply[Any => String](s"$script \n new $SCRIPT_CLASS_NAME {}")

  //水平分库函数
  val fun: Any => Boolean = columnValue => {
    derived(columnValue).asInstanceOf[Boolean]
  }

  /**表达式是否为真*/
  def isTrue(columnValue: Any): Boolean = {
    fun(columnValue)
  }

}