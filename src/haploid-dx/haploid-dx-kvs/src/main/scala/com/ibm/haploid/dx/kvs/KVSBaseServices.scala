package com.ibm.haploid

package dx

package kvs

import java.nio.file.Path

import org.restlet.data.{ Protocol, Method, MediaType, ChallengeScheme, ChallengeResponse }
import org.restlet.engine.Engine
import org.restlet.ext.html.{ FormDataSet, FormData }
import org.restlet.representation.{ Representation, FileRepresentation }
import org.restlet.{ Response, Request, Client }

import com.ibm.haploid.core.service.{ Success, Result }
import com.ibm.haploid.core.service.Service
import com.ibm.haploid.dx.kvs.KVSObject.{ ZUORDNUNG, TEIL, KSTAND, FILE, DOCUVERS, DOCUMENT, DIAGNOSE }
import com.typesafe.config.Config

/**
 *
 */
case class DownloadFileInput(fileId: String, responsible: String, errorType: String = "text/plain", header: Option[String] = None, mimeType: Option[String] = None)

case class DiagnoseInput(responsible: String)

case class CreateKStandForDVInput(dvId: String, partKey: String, map: Map[String, String], responsible: String)

case class LinkDVToKStandInput(dvId: String, kStandId: String, responsible: String)

case class UpdateKStandInput(id: String, map: Map[String, String], responsible: String)

sealed abstract class SelectDocuInput
case class SelectDocuByCodedIdInput(id: String, responsible: String, api2: Option[Int] = None) extends SelectDocuInput
case class SelectDocuByIdInput(id: String, responsible: String, api2: Option[Int] = None) extends SelectDocuInput
case class SelectDocuByKeyInput(key: String, responsible: String, api2: Option[Int] = None) extends SelectDocuInput
case class SelectDocuByAttributesInput(map: Map[String, String], responsible: String, api2: Option[Int] = None) extends SelectDocuInput

sealed abstract class SelectDocuVersInput
case class SelectDocuVersByCodedIdInput(id: String, responsible: String, api2: Option[Int] = None) extends SelectDocuVersInput
case class SelectDocuVersByIdInput(id: String, responsible: String, api2: Option[Int] = None) extends SelectDocuVersInput
case class SelectDocuVersByKeyInput(key: String, responsible: String, api2: Option[Int] = None) extends SelectDocuVersInput
case class SelectDocuVersByAttributesInput(map: Map[String, String], responsible: String, api2: Option[Int] = None) extends SelectDocuVersInput

case class SelectFileInput(versId: String, filesystem: String, map: Map[String, String] = Map(), responsible: String, api2: Option[Int] = None)

sealed abstract class SelectTeilInput
case class SelectTeilByCodedIdInput(id: String, responsible: String, api2: Option[Int] = None) extends SelectTeilInput
case class SelectTeilByIdInput(id: String, responsible: String, api2: Option[Int] = None) extends SelectTeilInput
case class SelectTeilByKeyInput(key: String, responsible: String, api2: Option[Int] = None) extends SelectTeilInput
case class SelectTeilByAttributesInput(map: Map[String, String], responsible: String, api2: Option[Int] = None) extends SelectTeilInput

sealed abstract class SelectKStandInput
case class SelectKStandByCodedIdInput(id: String, responsible: String, api2: Option[Int] = None) extends SelectKStandInput
case class SelectKStandByIdInput(id: String, responsible: String, api2: Option[Int] = None) extends SelectKStandInput
case class SelectKStandByKeyInput(key: String, typ: KVSObjectType, responsible: String, api2: Option[Int] = None) extends SelectKStandInput
case class SelectKStandByAttributesInput(map: Map[String, String], responsible: String, api2: Option[Int] = None) extends SelectKStandInput

sealed abstract class SelectZuordnungInput
case class SelectZuordnungByCodedIdInput(id: String, responsible: String, api2: Option[Int] = None) extends SelectZuordnungInput
case class SelectZuordnungByIdInput(id: String, responsible: String, api2: Option[Int] = None) extends SelectZuordnungInput
case class SelectZuordnungByKeyInput(key: String, responsible: String, api2: Option[Int] = None) extends SelectZuordnungInput
case class SelectZuordnungByAttributesInput(map: Map[String, String], responsible: String, api2: Option[Int] = None) extends SelectZuordnungInput

case class UploadFileInput(path: Path, modelname: Option[String] = None, responsible: String, api2attributes: Map[String, String] = Map(), mediaType: MediaType = MediaType.APPLICATION_OCTET_STREAM)

/**
 *
 */
object KVSBaseServices

  extends KVSTypeconverter {

  private def getQueryURI(function: API_FUNC, obj: Option[KVSObject], params: Map[String, String], api2: Option[Int]): String = {
    val uriParams = (params.foldLeft(new String)((r, e) => r + "&" + e._1 + "=" + e._2)).concat(
      { if (api2 != None) "&API2_INFO_TYPE=" + api2.get else "" })

    "DE-script/webagent/" + function.value + (if (obj != None) "/" + obj.get.value) + "?sys_name=" + sysName + "&sys_id=" + sysId + uriParams
  }

  private def query(
    function: API_FUNC,
    obj: Option[KVSObject],
    responsible: String,
    params: Map[String, String] = Map(),
    api2: Option[Int] = None,
    content: Option[Representation] = None): Response = {

    val clients = Engine.getInstance.getRegisteredClients
    clients.clear
    clients.add(new com.ibm.haploid.dx.kvs.KVSHttpClientHelper(null))
    clients.add(new org.restlet.engine.connector.HttpClientHelper(null))

    System.setProperty("javax.net.ssl.trustStore", truststore);
    System.setProperty("javax.net.ssl.trustStorePassword", truststorePassword);

    //    System.setProperty("javax.net.debug", "ssl");

    if (com.ibm.haploid.dx.proxyHost != null && com.ibm.haploid.dx.proxyHost.length() > 0) {
      System.setProperty("http.proxyHost", com.ibm.haploid.dx.proxyHost);
      System.setProperty("http.proxyPort", com.ibm.haploid.dx.proxyPort);
      System.setProperty("https.proxyHost", com.ibm.haploid.dx.proxyHost);
      System.setProperty("https.proxyPort", com.ibm.haploid.dx.proxyPort);
    }

    val url = "https://" + host + ":" + port + "/" + getQueryURI(function, obj, params, api2)
    val request = new Request(Method.GET, url)
    val client = new Client(Protocol.HTTPS)

    val user = username + "&" + responsible

    request.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_BASIC, user, password));

    if (content != None) {
      request.setMethod(Method.POST)
      request.setEntity(content.get)
    }
    
    logger.debug(request.getMethod.toString + " " + url)
    val response = client.handle(request)
    logger.debug("Response status :  " + response.getStatus())
    response
  }

  private def selectByCodedId(id: String, obj: KVSObject, responsible: String, api2: Option[Int]): String = {
    query(API_INFO, obj, responsible, obj.idCoded -> id, api2)
  }

  private def selectById(id: String, obj: KVSObject, responsible: String, api2: Option[Int]): String = {
    query(API_INFO, obj, responsible, obj.id -> id, api2)
  }

  private def selectByKey(key: String, obj: KVSObject, responsible: String, map: Map[String, String] = Map(), api2: Option[Int]): String = {
    query(API_SELECT, obj, responsible, (obj.key -> key) ++ map, api2)
  }

  private def select(map: Map[String, String], obj: KVSObject, responsible: String, api2: Option[Int]): String = {
    query(API_SELECT, obj, responsible, map, api2)
  }

  object Diagnose extends Service[DiagnoseInput, Config] {

    def doService(in: DiagnoseInput): Result[Config] = {
      Success(responseToString(query(API_DIAGNOSE, DIAGNOSE, in.responsible, api2 = None)))
    }

  }

  object DownloadFile extends Service[DownloadFileInput, Config] {

    def doService(in: DownloadFileInput): Result[Config] = {

      val map: scala.collection.mutable.Map[String, String] = scala.collection.mutable.Map("file_id" -> in.fileId, "error_type" -> in.errorType)

      if (in.header != None) map.put("header", in.header.get)
      if (in.mimeType != None) map.put("mimetype", in.mimeType.get)

      Success(responseToString(query(API_DOWNLOAD, None, in.responsible, map.toMap, None)))

    }

  }

  object SelectDocu extends Service[SelectDocuInput, Config] {

    def doService(in: SelectDocuInput): Result[Config] = {

      Success(in match {
        case SelectDocuByCodedIdInput(id, responsible, api2) =>
          selectByCodedId(id, DOCUMENT, responsible, api2)
        case SelectDocuByIdInput(id, responsible, api2) =>
          selectById(id, DOCUMENT, responsible, api2)
        case SelectDocuByKeyInput(key, responsible, api2) =>
          selectByKey(key, DOCUMENT, responsible, api2 = api2)
        case SelectDocuByAttributesInput(map, responsible, api2) =>
          select(map, DOCUMENT, responsible, api2)
      })

    }

  }

  object SelectDocuVers extends Service[SelectDocuVersInput, Config] {

    def doService(in: SelectDocuVersInput): Result[Config] = {

      Success(in match {
        case SelectDocuVersByCodedIdInput(id, responsible, api2) =>
          selectByCodedId(id, DOCUVERS, responsible, api2)
        case SelectDocuVersByIdInput(id, responsible, api2) =>
          selectById(id, DOCUVERS, responsible, api2)
        case SelectDocuVersByKeyInput(key, responsible, api2) =>
          selectByKey(key, DOCUVERS, responsible, api2 = api2)
        case SelectDocuVersByAttributesInput(map, responsible, api2) =>
          select(map, DOCUVERS, responsible, api2)
      })

    }

  }

  object SelectFile extends Service[SelectFileInput, Config] {

    def doService(in: SelectFileInput): Result[Config] = {
      Success(responseToString(query(API_SELECT, FILE, in.responsible, Map("FILE_VERS_ID" -> in.versId, "DV_DATEISYSTEM" -> in.filesystem) ++ in.map, in.api2)))
    }

  }

  object SelectTeil extends Service[SelectTeilInput, Config] {

    def doService(in: SelectTeilInput): Result[Config] = {

      Success(in match {
        case SelectTeilByCodedIdInput(id, responsible, api2) =>
          selectByCodedId(id, TEIL, responsible, api2)
        case SelectTeilByIdInput(id, responsible, api2) =>
          selectById(id, TEIL, responsible, api2)
        case SelectTeilByKeyInput(key, responsible, api2) =>
          selectByKey(key, TEIL, responsible, api2 = api2)
        case SelectTeilByAttributesInput(map, responsible, api2) =>
          select(map, TEIL, responsible, api2)
      })

    }

  }

  object SelectKStand extends Service[SelectKStandInput, Config] {

    def doService(in: SelectKStandInput): Result[Config] = {

      Success(in match {
        case SelectKStandByCodedIdInput(id, responsible, api2) =>
          selectByCodedId(id, KSTAND, responsible, api2)
        case SelectKStandByIdInput(id, responsible, api2) =>
          selectById(id, KSTAND, responsible, api2)
        case SelectKStandByKeyInput(key, typ, responsible, api2) =>
          selectByKey(key, KSTAND, responsible, "KSTAND_OBJEKTTYP" -> typ.value, api2 = api2)
        case SelectKStandByAttributesInput(map, responsible, api2) =>
          select(map, KSTAND, responsible, api2)
      })

    }

  }

  object SelectZuordnung extends Service[SelectZuordnungInput, Config] {

    def doService(in: SelectZuordnungInput): Result[Config] = {
      Success(in match {
        case SelectZuordnungByCodedIdInput(id, responsible, api2) =>
          selectByCodedId(id, ZUORDNUNG, responsible, api2)
        case SelectZuordnungByIdInput(id, responsible, api2) =>
          selectById(id, ZUORDNUNG, responsible, api2)
        case SelectZuordnungByKeyInput(key, responsible, api2) =>
          selectByKey(key, ZUORDNUNG, responsible, api2 = api2)
        case SelectZuordnungByAttributesInput(map, responsible, api2) =>
          select(map, ZUORDNUNG, responsible, api2)
      })
    }

  }

  object UploadFile extends Service[UploadFileInput, Config] {

    def doService(in: UploadFileInput): Result[Config] = {
      val fr = new FileRepresentation(in.path.toFile(), in.mediaType)

      val form = new FormDataSet()
      form.setMultipart(true);
      form.getEntries().add(new FormData("UPLOAD_FILENAME", fr))
      form.getEntries().add(new FormData("MODE", "UPLOAD"))
      form.getEntries().add(new FormData("STORE_BUTTON", "Submit"))

      if (in.modelname != None) form.getEntries().add(new FormData("UPLOAD_MODELNAME", in.modelname.get))

      in.api2attributes.foreach(e => form.getEntries().add(new FormData(e._1, e._2)))

      Success(responseToString(query(API_UPLOAD, None, in.responsible, content = Some(form), api2 = None)))
    }

  }

  object UpdateKStand extends Service[UpdateKStandInput, Config] {

    def doService(in: UpdateKStandInput): Result[Config] = {
      Success(
        responseToString(
          query(API_UPDATE, KSTAND, in.responsible, ("id" -> in.id) ++ in.map, None)))
    }

  }

  object CreateKStandForDV extends Service[CreateKStandForDVInput, Config] {

    def doService(in: CreateKStandForDVInput): Result[Config] = {
      val form = new FormDataSet()

      form.getEntries().add(new FormData("MZ_DV_ID", in.dvId))
      form.getEntries().add(new FormData("KSTAND_ID.0", ":new:T=new"))
      form.getEntries().add(new FormData("KSTAND_KSTAND.0", "Knext"))
      form.getEntries().add(new FormData("TEIL_KEY.0", in.partKey))
      form.getEntries().add(new FormData("PARTS_TO_LINK", "1"))
      form.getEntries().add(new FormData("MZW_BUTTON", "Submit"))

      in.map.foreach(e => form.getEntries().add(new FormData("KSTAND_" + e._1 + ".0", e._2)))

      Success(responseToString(query(API_UPLOAD, None, in.responsible, content = Some(form), api2 = None)))
    }

  }

  object LinkDVToKStand extends Service[LinkDVToKStandInput, Config] {

    def doService(in: LinkDVToKStandInput): Result[Config] = {
      val form = new FormDataSet()

      form.getEntries().add(new FormData("MZ_DV_ID", in.dvId))
      form.getEntries().add(new FormData("KSTAND_ID.0", in.kStandId))
      form.getEntries().add(new FormData("PARTS_TO_LINK", "1"))
      form.getEntries().add(new FormData("MZW_BUTTON", "Submit"))

      Success(responseToString(query(API_UPLOAD, None, in.responsible, content = Some(form), api2 = None)))
    }

  }

}