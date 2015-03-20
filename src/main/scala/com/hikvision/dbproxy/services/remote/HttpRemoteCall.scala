/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: HttpRemoteCall.scala
 * created at: 2014年9月26日
 */
package com.hikvision.dbproxy.services.remote

import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpPut
import org.apache.http.client.methods.HttpDelete
import java.net.URI
import org.apache.http.params.HttpParams
import org.apache.http.params.BasicHttpParams
import org.apache.http.util.EntityUtils
import org.apache.http.params.CoreConnectionPNames
import org.apache.http.params.HttpConnectionParams
import org.apache.http.client.config.RequestConfig
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.client.HttpClient
import com.hikvision.dbproxy.services.exception.DbproxyServiceException

/**
 * http 协议远程调用
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年9月26日 下午3:04:12
 * @version: $Rev: 3248 $
 */
class HttpRemoteCall extends RemoteCall {

  def call(address: String, params: Map[String, Any]): Any = {

    // 设置连接超时(5秒)
    val requestConfig: RequestConfig = RequestConfig.custom().setConnectTimeout(5 * 1000).build()

    // http client 客户端对象
    val httpclient: HttpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build()

    val httpMethod = if (params == null || params.isEmpty) getHttpMethod(Option("get")) else getHttpMethod(params.get("method"))
    if (httpMethod == null) throw new IllegalArgumentException("method param value is error! please choose form get post put delete.")
    httpMethod.setURI(URI.create(address))

    if (params != null && !params.isEmpty) {
      // http 参数对象
      val httpParamsObj: HttpParams = new BasicHttpParams()
      // 获取http 参数(过滤掉非method参数)
      val httpParams = params.filter(_._1 != "method")
      httpParams.foreach(e => httpParamsObj.setParameter(e._1, e._2))
      httpMethod.setParams(httpParamsObj)
    }

    val response = httpclient.execute(httpMethod)
    response.getStatusLine().getStatusCode() match {
      case 200 => EntityUtils.toString(response.getEntity(), "UTF-8");
      case code @ _ => throw new DbproxyServiceException(s"http method execute fail! the get statusCode is $code")
    }
  }

  // 获取http Method 对象
  private def getHttpMethod(method: Option[Any]) = method match {
    case Some("get") => new HttpGet
    case Some("post") => new HttpPost
    case Some("put") => new HttpPut
    case Some("delete") => new HttpDelete
    case Some(_) => null
    case None => new HttpGet // 默认采用httpGet请求
  }

}
