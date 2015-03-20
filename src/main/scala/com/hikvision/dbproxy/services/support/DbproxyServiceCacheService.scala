/*
 * Copyright 2014 The Hikvision CO.Ltd
 * site: http://www.hikvision.com
 * Prject: dbproxy-services
 * Description: DbproxyServiceCacheService.scala
 * created at: 2014年8月7日
 */
package com.hikvision.dbproxy.services.support

import spray.json._
import DefaultJsonProtocol._
import com.hikvision.dbproxy.services.ResourceDataCacheService
import com.hikvision.dbproxy.services.ServiceConstant
import org.slf4j.LoggerFactory
import com.hikvision.dbproxy.services.ProxySettingCacheService
import scala.annotation.tailrec
import com.hikvision.dbproxy.services.exception.ModuleServiceException
import com.hikvision.dbproxy.services.InfoExchangeType
import com.hikvision.dbproxy.services.StringUtils

/**
 * 数据库代理服务缓存服务
 * @author: <a href="mailto:hbxffj@163.com">方杰</a>
 * @Date: 2014年8月7日 下午12:49:34
 * @version: $Rev: 4007 $
 */
trait DbproxyServiceCacheService {

  val logger = LoggerFactory.getLogger(classOf[DbproxyServiceCacheService])

  //数字类型
  private val NUMBER: String = "number"
  //字符串类型
  private val VARCHAR: String = "varchar"
  // 排序操作升序
  private val SORT_OP_ASC = "+"
  // 排序操作降序
  private val SORT_OP_DESC = "-"

  def select(app: String, resource: String, conditions: Map[String, String]): List[Map[String, String]] = {
    // 查看是否设置缓存
    val proxySettingCacheService = new ProxySettingCacheService(app)
    val resourceSetting = proxySettingCacheService.getResourceSettingCache(resource)
    if (resourceSetting == null) {
      throw ModuleServiceException(InfoExchangeType.C_107, "查询资源不存在!")
    }
    if (ServiceConstant.IS_TRUE_1 == resourceSetting.is_cache.getOrElse("")) {
      // 获取过滤的条件,排除排序等其他字段
      val getFilterConditions: Map[String, String] => Map[String, String] = _.filterKeys(!_.contains("$"))
      // 条件中的过滤项
      val filterConditions = getFilterConditions(conditions)
      // 条件中的排序项
      val sortConditions = conditions.get("$sort") getOrElse null

      logger.debug(s"selectCache conditions ${filterConditions.keys mkString (" ")} ")

      val cacheService = new ResourceDataCacheService(app, resource)

      // 查询总数
      val count = cacheService.count
      if (count == 0) {
        List()
      } else {
        /*-------------检验字段项是否存在-------------*/
        // 取缓存的一条数据验证
        val tableFirstRow = cacheService.lindex(0).parseJson.convertTo[Map[String, String]]
        val columnSet = tableFirstRow.keySet
        if (!StringUtils.contains(columnSet, filterConditions.keySet)) {
          throw ModuleServiceException(InfoExchangeType.C_101, "查询条件字段不存在!")
        }
        // 排序(数据量必须小于批处理数)
        if (sortConditions != null && count <= ServiceConstant.batchSize) {
          //检验排序字段项是否存在
          val sortConditionsSet: Set[String] = sortConditions.split(",").toSet
          if (!StringUtils.contains(columnSet, sortConditionsSet)) {
            throw ModuleServiceException(InfoExchangeType.C_101, "排序字段不存在!")
          }
          // 缓存所有结果集
          val list = cacheService.list
          // 反序列化
          val listMap = list map {
            case Some(m) => m.parseJson.convertTo[Map[String, String]]
            case None => null
          }
          // 普通结果集
          val result = listMap.toSeq.toList
          // 排序
          val sortItem: String => String = in => in.replaceAll("[+-]$", "")
          val sortItems = sortConditions.split(",") map sortItem
          val sortAndOpList = sortConditions.split(",") map (in => sortItem(in) + (if (in.endsWith("-")) " -" else " +"))
          logger.debug(s"sortAndOpList: ${sortAndOpList mkString ("")} ")
          // 转换为排序字段的map
          val sortMap = sortAndOpList map { e =>
            val sortArray = e.split(" ")
            sortArray(0) -> sortArray(1)
          }
          // TODO 获取排序字段数据类型
          val typeMap = sortItems map {
            case "id" => "id" -> NUMBER
            case "age" => "age" -> NUMBER
            case field @ _ => field -> VARCHAR
          }
          resultSort(result, sortItems, sortMap.toMap, typeMap.toMap).slice(0, ServiceConstant.selectSize)
        } else {
          // josn转换为map对象
          val converterMap: Option[String] => Map[String, String] = (in) => in match {
            case Some(m) => m.parseJson.convertTo[Map[String, String]]
            case None => null
          }
          if (filterConditions != null && !filterConditions.isEmpty) {
            val list = search(cacheService, filterConditions, count.toInt, List[Option[String]](), 0, ServiceConstant.batchSize - 1)
            list map converterMap
          } else {
            val list = cacheService.lrange(0, ServiceConstant.selectSize - 1)
            list map converterMap
          }
        }
      }
    } else {
      List()
    }
  }

  /**条件检查source: Map[String, String]*/
  def checkCondition(source: Map[String, String], conditions: Map[String, String]): Boolean = {
    import spray.json._
    import DefaultJsonProtocol._
    checkConditionSourceJsonString(source.toJson.toString, conditions)
  }

  /**条件检查 source: String,and it's json String and can convert Map*/
  def checkConditionSourceJsonString(source: String, conditions: Map[String, String]): Boolean = {
    val s = source.parseJson.convertTo[Map[String, String]]
    val booleanList = for ((k, v) <- conditions) yield (s(k) == v)
    booleanList.forall(s => s == true)
  }

  /**结果集排序(只支持两个字段项排序)*/
  def resultSort(result: List[Map[String, String]], sortItems: Array[String], sortMap: Map[String, String], typeMap: Map[String, String]): List[Map[String, String]] = {
    sortItems match {
      case Array(e1) => {
        val sortResult = typeMap(e1) match {
          case NUMBER => result.sortBy(m => m(e1).toDouble)
          case _ => result.sortBy(m => m(e1))
        }
        if (sortMap(e1) == SORT_OP_DESC) sortResult.reverse else sortResult
      }
      case Array(e1, e2) => {
        (typeMap(e1), typeMap(e2)) match {
          // 都为数字的情况
          case (NUMBER, NUMBER) => {
            (sortMap(e1), sortMap(e2)) match {
              // 都为降序的情况
              case (SORT_OP_DESC, SORT_OP_DESC) => result.sortBy(m => (m(e1).toDouble, m(e2).toDouble))(Ordering.Tuple2(Ordering.Double.reverse, Ordering.Double.reverse))
              // 都为升序的情况
              case (SORT_OP_ASC, SORT_OP_ASC) => result.sortBy(m => (m(e1).toDouble, m(e2).toDouble))(Ordering.Tuple2(Ordering.Double, Ordering.Double))
              // 前者升序,后者降序
              case (SORT_OP_ASC, SORT_OP_DESC) => result.sortBy(m => (m(e1).toDouble, m(e2).toDouble))(Ordering.Tuple2(Ordering.Double, Ordering.Double.reverse))
              // 前者降序,后者升序
              case (SORT_OP_DESC, SORT_OP_ASC) => result.sortBy(m => (m(e1).toDouble, m(e2).toDouble))(Ordering.Tuple2(Ordering.Double.reverse, Ordering.Double))
            }
          }
          // 都为字符串的情况
          case (VARCHAR, VARCHAR) => {
            (sortMap(e1), sortMap(e2)) match {
              // 都为降序的情况
              case (SORT_OP_DESC, SORT_OP_DESC) => result.sortBy(m => (m(e1), m(e2)))(Ordering.Tuple2(Ordering.String.reverse, Ordering.String.reverse))
              // 都为升序的情况
              case (SORT_OP_ASC, SORT_OP_ASC) => result.sortBy(m => (m(e1), m(e2)))(Ordering.Tuple2(Ordering.String, Ordering.String))
              // 前者升序,后者降序
              case (SORT_OP_ASC, SORT_OP_DESC) => result.sortBy(m => (m(e1), m(e2)))(Ordering.Tuple2(Ordering.String, Ordering.String.reverse))
              // 前者降序,后者升序
              case (SORT_OP_DESC, SORT_OP_ASC) => result.sortBy(m => (m(e1), m(e2)))(Ordering.Tuple2(Ordering.String.reverse, Ordering.String))
            }
          }
          // 前者为数字,后者为字符串的情况
          case (NUMBER, VARCHAR) => {
            (sortMap(e1), sortMap(e2)) match {
              // 都为降序的情况
              case (SORT_OP_DESC, SORT_OP_DESC) => result.sortBy(m => (m(e1).toDouble, m(e2)))(Ordering.Tuple2(Ordering.Double.reverse, Ordering.String.reverse))
              // 都为升序的情况
              case (SORT_OP_ASC, SORT_OP_ASC) => result.sortBy(m => (m(e1).toDouble, m(e2)))(Ordering.Tuple2(Ordering.Double, Ordering.String))
              // 前者升序,后者降序
              case (SORT_OP_ASC, SORT_OP_DESC) => result.sortBy(m => (m(e1).toDouble, m(e2)))(Ordering.Tuple2(Ordering.Double, Ordering.String.reverse))
              // 前者降序,后者升序
              case (SORT_OP_DESC, SORT_OP_ASC) => result.sortBy(m => (m(e1).toDouble, m(e2)))(Ordering.Tuple2(Ordering.Double.reverse, Ordering.String))
            }
          }
          // 前者为字符串,后者为数字的情况
          case (VARCHAR, NUMBER) => {
            (sortMap(e1), sortMap(e2)) match {
              // 都为降序的情况
              case (SORT_OP_DESC, SORT_OP_DESC) => result.sortBy(m => (m(e1), m(e2).toDouble))(Ordering.Tuple2(Ordering.String.reverse, Ordering.Double.reverse))
              // 都为升序的情况
              case (SORT_OP_ASC, SORT_OP_ASC) => result.sortBy(m => (m(e1), m(e2).toDouble))(Ordering.Tuple2(Ordering.String, Ordering.Double))
              // 前者升序,后者降序
              case (SORT_OP_ASC, SORT_OP_DESC) => result.sortBy(m => (m(e1), m(e2).toDouble))(Ordering.Tuple2(Ordering.String, Ordering.Double.reverse))
              // 前者降序,后者升序
              case (SORT_OP_DESC, SORT_OP_ASC) => result.sortBy(m => (m(e1), m(e2).toDouble))(Ordering.Tuple2(Ordering.String.reverse, Ordering.Double))
            }
          }
          case _ => result.sortBy(m => m(e1))
        }
      }
    }
  }

  /**
   * 查询
   */
  @tailrec
  private def search(cacheService: ResourceDataCacheService, filterConditions: Map[String, String], count: Int, result: List[Option[String]], startIndex: Int, endIndex: Int): List[Option[String]] = {
    val list = cacheService.lrange(startIndex, endIndex)
    val selectResult = (list.par.filter {
      case Some(m) => checkConditionSourceJsonString(m, filterConditions)
      case None => false
    }).seq.toList
    if (selectResult.size >= ServiceConstant.selectSize || endIndex >= count - 1) selectResult.slice(0, ServiceConstant.selectSize)
    else {
      search(cacheService, filterConditions, count, result ++ selectResult, endIndex + 1, endIndex + 1 + (endIndex - startIndex))
    }
  }

}