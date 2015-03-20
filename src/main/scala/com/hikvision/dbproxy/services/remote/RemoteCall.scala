/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: RemoteCall.scala
 * created at: 2014年9月26日
 */
package com.hikvision.dbproxy.services.remote

/**
 * 远程调用接口
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年9月26日 下午3:01:55
 * @version: $Rev: 3248 $
 */
trait RemoteCall {

  /**
   * 返回调用的结果
   * 远程调用
   * @address 地址
   * @params 参数(Map),提供调用所需要的所有参数(键值对形式)
   */
  def call(address: String, params: Map[String, Any]): Any

}