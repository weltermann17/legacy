package com.ibm.de.ebs.plm.scala.rest

import java.util.Calendar
import java.util.Date
import java.util.LinkedList

import org.restlet.data.CacheDirective
import org.restlet.data.CharacterSet
import org.restlet.data.Language
import org.restlet.data.MediaType
import org.restlet.data.Method
import org.restlet.representation.Representation
import org.restlet.representation.StringRepresentation
import org.restlet.routing.Filter.CONTINUE
import org.restlet.routing.Filter
import org.restlet.Context
import org.restlet.Request
import org.restlet.Response
import org.restlet.Restlet

import com.ibm.de.ebs.plm.scala.caching.Cache
import com.ibm.de.ebs.plm.scala.caching.Caching
import com.ibm.de.ebs.plm.scala.caching.MemoryCache
import com.ibm.de.ebs.plm.scala.caching.Cacheable
import com.ibm.de.ebs.plm.scala.json.Json
import com.ibm.de.ebs.plm.scala.json.JsonSerializable
import com.ibm.de.ebs.plm.scala.rest.HeaderUtils.removeHeader
import com.ibm.de.ebs.plm.scala.util.Timers.Long2MilliSeconds
import com.ibm.de.ebs.plm.scala.util.Timers.now

case class StringMemoryCache(hint: Int, maxsize: Long, shrinkby: Double)
  extends MemoryCache[String, CachedString]

case class CachingStrings(
  cache: Cache[String, CachedString],
  next: Restlet,
  maxage: Long,
  context: Context,
  convert: (String, Date) => Representation = (s, d) => {
    val representation = new StringRepresentation(s, MediaType.APPLICATION_JSON, Language.ALL, CharacterSet.UTF_8)
    representation.setExpirationDate(d)
    representation
  })
  extends Filter(context, next) with Caching[String, CachedString] {

  override protected def beforeHandle(request: Request, response: Response): Int = {
    cache match {
      case null => Filter.CONTINUE
      case _ =>
        request.getMethod match {
          case Method.GET =>
            makeKey(request)
            cache.get(key) match {
              case Some(v) =>
                val calendar = Calendar.getInstance
                calendar.add(Calendar.SECOND, maxage.inSeconds.toInt)
                response.setEntity(convert(v.toString, calendar.getTime))
                Filter.STOP
              case None => Filter.CONTINUE
            }
          case _ => Filter.CONTINUE
        }
    }
  }

  override protected def afterHandle(request: Request, response: Response) = cache match {
    case null =>
    case _ =>
      if (response.getStatus.isSuccess) request.getMethod match {
        case Method.GET =>
          asText(response.getEntity) match {
            case Some(v) =>
              cache.add(key, CachedString(v, maxage))
              val calendar = Calendar.getInstance
              calendar.add(Calendar.SECOND, maxage.inSeconds.toInt)
              response.setEntity(convert(v.toString, calendar.getTime))
            case None =>
          }
        case _ =>
      }
  }

  private def asText(r: Representation) = {
    val media = MediaType.valueOf(r.getMediaType.getMainType)
    if (textmedia.contains(media)) Some(r.getText) else None
  }

  private var key: String = null

  private def makeKey(request: Request) = {
    val path = request.getResourceRef.getPath
    val query = request.getResourceRef.getQuery
    key = if (null == query) path else path + "?" + query
    key += (if (0 < request.getClientInfo.getAcceptedMediaTypes.size) request.getClientInfo.getAcceptedMediaTypes.get(0).getMetadata else "*/*")
  }

  private val textmedia = {
    val s = new scala.collection.mutable.HashSet[MediaType]
    s ++= MediaType.TEXT_ALL :: Nil
    s
  }
}

case class CachedString(value: String, maxage: Long) extends Cacheable with JsonSerializable {
  val bestbefore = now + maxage
  def freshness = bestbefore - now
  def length = value.getBytes.length
  override def toString = value
  override def toJson = Json(Map("value" -> value, "bestbefore" -> bestbefore, "freshness" -> freshness))
}

case class Expires(next: Restlet, expiresinmsec: Long, context: Context) extends Filter(context, next) {

  override protected def afterHandle(request: Request, response: Response) = {
    request.getMethod match {
      case Method.GET =>
        if (response.getStatus.isSuccess && response.isEntityAvailable) {
          val isrootloader = request.getResourceRef.getPath.contains("gepclient.nocache.js")
          if (!isrootloader) {
            val calendar = Calendar.getInstance
            calendar.add(Calendar.SECOND, expiresinmsec.inSeconds.toInt)
            response.getEntity.setExpirationDate(calendar.getTime)
            val cachedirectives = new LinkedList[CacheDirective]
            cachedirectives.add(CacheDirective.maxAge(expiresinmsec.inSeconds.toInt))
            response.setCacheDirectives(cachedirectives)
          }
        }
      case _ =>
    }
  }
}

