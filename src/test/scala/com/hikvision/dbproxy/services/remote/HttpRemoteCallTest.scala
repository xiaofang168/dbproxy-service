/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: HttpRemoteCallTest.java
 * created at: 2014年9月26日
 */
package com.hikvision.dbproxy.services.remote;

import org.junit.Test
import org.junit.Assert
import com.hikvision.dbproxy.entities.Response

/**
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年9月26日 下午4:32:45
 * @version: $Rev: 3340 $
 */
class HttpRemoteCallTest {

  @Test
  def test() {
    import spray.json.JsonParser
    import spray.json._
    import DefaultJsonProtocol._
    val hrc = new HttpRemoteCall()
    val address = "http://localhost:9090/server/idproxy/proxy/10.192.32.82,10.192.32.83/user"
    val response = hrc.call(address, null)
    val map = response.toString.parseJson.convertTo[Map[String, String]]
    Assert.assertEquals("000", map("code"))
  }

  @Test
  def test2() {
    import spray.json.JsonParser
    import spray.json._
    import DefaultJsonProtocol._
    val hrc = new HttpRemoteCall()
    val address = "http://localhost:9090/server/idproxy/proxy/10.192.32.82,10.192.32.83/user"
    // 参数 map
    val params = Map("method" -> "get")
    val response = hrc.call(address, params)
    val map = response.toString.parseJson.convertTo[Map[String, String]]
    Assert.assertEquals("000", map("code"))
  }

  @Test
  def test3() {
    import spray.json.JsonParser
    import spray.json._
    import DefaultJsonProtocol._
    val hrc = new HttpRemoteCall()
    val address = "http://localhost:9090/server/idproxy/proxy/10.192.32.82,10.192.32.83/user"
    val response = hrc.call(address, Map("username" -> "zhangsan"))
    val map = response.toString.parseJson.convertTo[Map[String, String]]
    Assert.assertEquals("000", map("code"))
  }

  @Test
  def test4() {
    import spray.json.JsonParser
    import spray.json._
    import DefaultJsonProtocol._
    val hrc = new HttpRemoteCall()
    val address = "http://localhost:9090/server/idproxy/proxy/10.192.32.82,10.192.32.83/user"
    try {
      val response = hrc.call(address, Map("method" -> "add"))
      val map = response.toString.parseJson.convertTo[Map[String, String]]
    } catch {
      case e: IllegalArgumentException => Assert.assertTrue(1 == 1)
    }
  }

  @Test
  def testSelect() {
    val hrc = new HttpRemoteCall()
    val address = "http://localhost:8088/dbproxy/app1/user?realname=管理员"
    val response = hrc.call(address, null)
    println(response)
  }

}
