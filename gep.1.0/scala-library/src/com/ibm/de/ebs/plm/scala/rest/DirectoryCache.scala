package com.ibm.de.ebs.plm.scala.rest

import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.ByteBuffer
import java.util.concurrent.ScheduledExecutorService
import java.util.Calendar
import java.util.LinkedList

import org.restlet.data.CacheDirective
import org.restlet.data.CharacterSet
import org.restlet.data.MediaType
import org.restlet.data._
import org.restlet.representation.FileRepresentation
import org.restlet.representation.Representation
import org.restlet.routing.Filter
import org.restlet.util.WrapperRepresentation
import org.restlet.Context
import org.restlet.Request
import org.restlet.Response
import org.restlet.Restlet

import com.ibm.de.ebs.plm.scala.caching._
import com.ibm.de.ebs.plm.scala.caching.Caching
import com.ibm.de.ebs.plm.scala.caching.Cacheable
import com.ibm.de.ebs.plm.scala.concurrent.ops.schedule
import com.ibm.de.ebs.plm.scala.json.Json
import com.ibm.de.ebs.plm.scala.json.JsonConversions
import com.ibm.de.ebs.plm.scala.resource.b
import com.ibm.de.ebs.plm.scala.resource.disposable
import com.ibm.de.ebs.plm.scala.resource.forceContextType
import com.ibm.de.ebs.plm.scala.resource.using
import com.ibm.de.ebs.plm.scala.security.MessageDigest.MD5
import com.ibm.de.ebs.plm.scala.rest._
import com.ibm.de.ebs.plm.scala.text.Uuid
import com.ibm.de.ebs.plm.scala.util.Io.buffersize
import com.ibm.de.ebs.plm.scala.util.Timers.Int2Unit
import com.ibm.de.ebs.plm.scala.util.Timers.Long2MilliSeconds
import com.ibm.de.ebs.plm.scala.util.Timers.now

case class CacheableRepresentation(representation: Representation, maxage: Long)
  extends WrapperRepresentation(representation) with Cacheable {
  override def length: Long = representation.getSize
  override def freshness = expires - now
  lazy val expires = now + maxage

  override def write(out: java.io.OutputStream) = {
    if (representation.isInstanceOf[FileRepresentation]) {
      val channel = getChannel
      org.restlet.engine.io.NioUtils.copy(channel.asInstanceOf[FileChannel], Channels.newChannel(out))
      channel.close
    } else {
      representation.write(Channels.newChannel(out))
    }
  }
}

case class CachingRepresentations(
  cache: RemoveableCache[String, CacheableRepresentation],
  next: Restlet,
  maxage: Long,
  context: Context)
  extends Filter(context, next) with Caching[String, CacheableRepresentation] {
  filter =>

  override protected def beforeHandle(request: Request, response: Response): Int = {
    cache match {
      case null => Filter.CONTINUE
      case _ =>
        if (request.getCacheDirectives.contains(CacheDirective.noCache)) {
          cache.remove(makeKey(request))
          Filter.CONTINUE
        } else {
          request.getMethod match {
            case Method.GET =>
              cache.get(makeKey(request)) match {
                case Some(representation) =>
                  val cachedirectives = new LinkedList[CacheDirective]
                  cachedirectives.add(CacheDirective.maxAge(maxage.inSeconds.toInt))
                  response.setCacheDirectives(cachedirectives)
                  response.setEntity(representation)
                  Filter.STOP
                case None => Filter.CONTINUE
              }
            case _ =>
              cache.remove(makeKey(request))
              Filter.CONTINUE
          }
        }
    }
  }

  override protected def afterHandle(request: Request, response: Response) = {
    cache match {
      case null =>
      case _ =>
        if (response.getStatus.isSuccess) {
          request.getMethod match {
            case Method.GET =>
              if (!request.getCacheDirectives.contains(CacheDirective.noCache)) {
                response.setEntity(cache.add(request.getAttributes.get("md5").toString, CacheableRepresentation(response.getEntity, maxage)))
              }
            case _ =>
          }
        }
    }
  }

  private def makeKey(request: Request): String = {
    val ref = request.getResourceRef
    val identifier = if (null != request.getChallengeResponse) request.getChallengeResponse.getIdentifier.toUpperCase else ""
    val accepted = request.getClientInfo.getAcceptedMediaTypes
    val mediatype = if (0 < accepted.size) accepted.get(0).getMetadata.toString else "application/json"
    val readablekey = ref.getPath + "?" + ref.getQuery + ":" + (if ("*/*" == mediatype) "application/json" else mediatype) + ":" + identifier
    val key = MD5(readablekey)
    request.getAttributes.put("md5", key)
    key
  }
}

case class RepresentationDirectoryCache(directoryuri: String, maxsize: Long, shrinkby: Double, stalecleaning: Long, context: Context)(implicit scheduler: ScheduledExecutorService)
  extends RemoveableCache[String, CacheableRepresentation] {

  staleCleaning

  override def contents: List[String] = Nil

  override def get(k: String): Option[CacheableRepresentation] = {
    try {
      val filepath = directorypath + hashDirectory(k) + k
      val infofile = new File(filepath + cacheinfoext)
      if (infofile.exists) {
        val reader = new InputStreamReader(new FileInputStream(infofile), "UTF-8")
        val cacheinfo = Json.parse(reader).asObject
        reader.close
        val extension = cacheinfo("extension").asString
        val datafile = new File(filepath + extension)
        if (datafile.exists) {
          if (cacheinfo.contains("filename")) {
            val disposition = new Disposition
            disposition.setFilename(cacheinfo("filename").asString)
            disposition.setSize(datafile.length)
          }
          val expires = cacheinfo("expires").asLong
          if (expires > now) {
            val maxage = expires - now
            infofile.setLastModified(now)
            datafile.setLastModified(now)
            val mediatype = Services.Metadata.getMediaType(extension.replace(cacheext, ""))
            val representation = InRepresentation(datafile, mediatype, maxage)
            return Some(CacheableRepresentation(representation, maxage)) // return point
          } else {
            infofile.delete
            datafile.delete
          }
        } else {
          infofile.delete
        }
      }
    } catch {
      case _ =>
    }
    None
  }

  override def add(k: String, v: CacheableRepresentation) = {
    CacheableRepresentation(OutRepresentation(k, v), v.maxage)
  }

  override def remove(k: String): Option[CacheableRepresentation] = {
    try {
      val filepath = directorypath + hashDirectory(k) + k
      val infofile = new File(filepath + cacheinfoext)
      if (infofile.exists) {
        val reader = new InputStreamReader(new FileInputStream(infofile), "UTF-8")
        val cacheinfo = Json.parse(reader)
        reader.close
        val extension = cacheinfo.asObject("extension").asString
        val datafile = new File(filepath + extension)
        if (datafile.exists) {
          infofile.delete
          datafile.delete
        } else {
          infofile.delete
        }
      }
    } catch {
      case _ =>
    }
    None
  }

  def computeSize: Long = {
    def recurse(f: File): Array[File] = {
      try {
        val files = f.listFiles.filter(_.isFile)
        val directories = f.listFiles.filter(_.isDirectory)
        files ++ directories.flatMap(recurse)
      } catch {
        case e => Array[File]()
      }
    }
    def toList[a](array: Array[a]): List[a] = {
      if (array == null || array.length == 0) Nil
      else if (array.length == 1) List(array(0))
      else array(0) :: toList(array.slice(1, array.length))
    }
    new File(directorypath) match {
      case null => 0
      case root =>
        toList(recurse(new File(directorypath))).foldLeft(0L)(_ + _.length)
    }
  }

  def removeAllStale: Long = {
    def isStale(infofile: File) = {
      try {
        implicit val _ = forceContextType[Boolean]
        using {
          val reader = disposable(new InputStreamReader(new FileInputStream(infofile), "UTF-8"))
          val cacheinfo = Json.parse(reader)
          val expires = cacheinfo.asObject("expires").asLong
          val extension = cacheinfo.asObject("extension").asString
          val datafile = new File(infofile.getAbsolutePath.replace(cacheinfoext, extension))
          (expires + (1 minute) < now) || !datafile.exists
        }
      } catch {
        case e => true
      }
    }
    def recurse(f: File): Array[File] = {
      try {
        try {
          val danglingfiles = f.listFiles.filter(_.isFile).filter(!_.getName.endsWith(cacheinfoext)).filter { f =>
            val path = f.getAbsolutePath
            val extension = path.substring(path.replace(cacheext, "").lastIndexOf("."))
            val infofile = new File(path.replace(extension, cacheinfoext))
            !infofile.exists
          }
          danglingfiles.foreach(_.delete)
        } catch {
          case e =>
        }
        val files = f.listFiles.filter(_.isFile).filter(_.getName.endsWith(cacheinfoext)).filter(isStale)
        val directories = f.listFiles.filter(_.isDirectory)
        if (0 == f.listFiles.length && directorypath != f.getAbsolutePath) {
          f.delete
          Array[File]()
        } else {
          files ++ directories.flatMap(recurse)
        }
      } catch {
        case e => Array[File]()
      }
    }
    new File(directorypath) match {
      case null => 0
      case root =>
        val stalefiles = recurse(new File(directorypath))
        val size = stalefiles.size
        stalefiles.foreach { infofile =>
          try {
            using {
              val reader = disposable(new InputStreamReader(new FileInputStream(infofile), "UTF-8"))
              val cacheinfo = Json.parse(reader)
              val extension = cacheinfo.asObject("extension").asString
              val datafile = new File(infofile.getAbsolutePath.replace(cacheinfoext, extension))
              if (datafile.exists) datafile.delete
              infofile.delete
              ()
            }
          } catch {
            case e =>
          }
        }
        size
    }
  }

  private def shrinkTo(targetsize: Long) = {

  }

  private def staleCleaning = {
    schedule(1 minute, stalecleaning) {
      val before = computeSize
      removeAllStale match {
        case 0 =>
        case n =>
          val after = computeSize
          context.getLogger.info("DirectoryCache : Removed " + n + " entries and reduced cache size from " + before + " to " + after + " bytes.")
      }
      val currentsize = computeSize
      if (currentsize > maxsize) {
        shrinkTo((maxsize * (1 - shrinkby)).toLong)
      }
    }
    context.getLogger.info("DirectoryCache : Scheduled stale cleaner every " + stalecleaning + " msec.")
  }

  protected def hashDirectory(filename: String): String =
    filename.substring(0, 3).foldLeft("") { (xs, x) => xs + x + File.separator }

  private case class OutRepresentation(filename: String, representation: CacheableRepresentation)
    extends WritableByteChannelRepresentation(representation.getMediaType) {

    setCharacterSet(representation.getCharacterSet)
    setDisposition(representation.getDisposition)

    private var zipped = true
    private val detectStream = {
      val in = new BufferedInputStream(representation.getStream, buffersize)
      try {
        in.mark(2)
        new java.util.zip.GZIPInputStream(in, buffersize)
      } catch {
        case _ => zipped = false; in.reset; in
      }
    }
    private val detectChannel = {
      if (!representation.isTransient) {
        representation.getChannel
      } else {
        Channels.newChannel(detectStream)
      }
    }

    override def isTransient = if (zipped) true else representation.isTransient
    override def getSize = if (zipped) -1 else representation.getSize
    override def getAvailableSize = if (zipped) -1 else representation.getAvailableSize

    override def write(out: java.nio.channels.WritableByteChannel) = {
      var data: FileChannel = null
      try { data = new FileOutputStream(datafile).getChannel } catch { case e => context.getLogger.warning(e.toString) }
      try {
        using {
          val in = disposable(detectChannel)
          val o = disposable(out)
          val buffer = ByteBuffer.allocateDirect(buffersize)
          while (0 <= in.read(buffer)) {
            buffer.flip
            o.write(buffer)
            if (null != data) {
              buffer.rewind
              data.write(buffer)
            }
            buffer.clear
          }
          if (null != data) data.close
          cacheInfo
        }
      } catch {
        case e => e.printStackTrace
      }
    }

    private def cacheInfo: Unit = {
      if (infofile.exists) infofile.delete
      if (datafile.exists) {
        val cacheinfo = Map(
          "extension" -> extension,
          "created" -> now,
          "expires" -> representation.expires,
          "maxage" -> representation.freshness)
        val dispositioninfo = if (null != representation.getDisposition)
          Map("filename" -> representation.getDisposition.getFilename) else Map()
        using {
          val info = disposable(new OutputStreamWriter(new FileOutputStream(infofile), "UTF-8"))
          info.write(Json.build(cacheinfo ++ dispositioninfo))
        }
        if (!infofile.exists && datafile.exists) {
          datafile.delete
        }
        if (infofile.exists) {
          var success = false
          val infofinal = new File(filepath + cacheinfoext)
          if (!infofinal.exists && infofile.renameTo(infofinal)) {
            val datafinal = new File(filepath + extension)
            if (!datafinal.exists && datafile.renameTo(datafinal)) {
              success = true
            }
          }
          if (!success) {
            infofile.delete
            datafile.delete
          }
        }
      }
    }
    private val hashdirectorypath = directorypath + hashDirectory(filename)
    private val hashdirectory = new File(hashdirectorypath)
    if (!hashdirectory.exists) try { hashdirectory.mkdirs } catch { case e => e.printStackTrace }
    private lazy val extension = getExtension(representation.getMediaType)
    private lazy val temptag = "." + Uuid.newUuid
    private lazy val filepath = hashdirectorypath + filename
    private lazy val infofile = new File(filepath + cacheinfoext + temptag)
    private lazy val datafile = new File(filepath + extension + temptag)

  }

  private case class InRepresentation(file: File, mediatype: MediaType, maxage: Long)
    extends FileRepresentation(file, mediatype, 42) {
    val calendar = Calendar.getInstance
    calendar.add(Calendar.SECOND, maxage.inSeconds.toInt)
    setExpirationDate(calendar.getTime)
    setCharacterSet(CharacterSet.UTF_8)

    override def getStream = {
      stream = super.getStream
      stream
    }
    override def release = if (null != stream) {
      if (null != stream) stream.close
      stream = null
    }
    private var stream: FileInputStream = null
  }

  private def getExtension(mediatype: MediaType) = "." + Services.Metadata.getExtension(mediatype) + cacheext
  private lazy val directorypath = (new File(new java.net.URI(directoryuri))).getPath + File.separator
  private val cacheext = ".cache"
  private val cacheinfoext = ".cacheinfo"
  private implicit val _ = forceContextType[Unit]

}
