package com.ibm.de.ebs.plm.scala.rest

import org.restlet.data._
import org.restlet.service.MetadataService
import org.restlet.Request

object Services {

  object Metadata extends MetadataService {

    val APPLICATION_CGR = MediaType.valueOf("cgr")
    val APPLICATION_3DXML = MediaType.valueOf("3dxml")
    val APPLICATION_JT = MediaType.valueOf("jt")
    val APPLICATION_PLMXML = MediaType.valueOf("plmxml")
    val APPLICATION_VFZ = MediaType.valueOf("vfz")
    val APPLICATION_RASTER = MediaType.valueOf("application/raster")
    val APPLICATION_EXCEL = MediaType.valueOf("application/x-msexcel")
    val APPLICATION_WORD = MediaType.valueOf("application/x-msword")

    addCommonExtensions
    addExtension("jpg", MediaType.valueOf("image/pjpeg"))
    addExtension("png", MediaType.valueOf("image/x-png"))
    addExtension("7z", MediaType.APPLICATION_ZIP)
    addExtension("tif", APPLICATION_RASTER)
    addExtension("xls", APPLICATION_EXCEL)
    addExtension("doc", APPLICATION_WORD)
    addExtension("CATDrawing", MediaType.valueOf("application/catiav5-local2D"))
    addExtension("CATPart", MediaType.valueOf("application/catiaV5-Part"))
    addExtension("CATProduct", MediaType.valueOf("application/catiaV5-Product"))
    addExtension("CATMaterial", MediaType.valueOf("application/catiaV5-Material"))
    addExtension("CATCatalog", MediaType.valueOf("application/catiav5Catalog"))
    addExtension("catdrawing", MediaType.valueOf("application/catiav5-local2D"))
    addExtension("catpart", MediaType.valueOf("application/catiaV5-Part"))
    addExtension("catproduct", MediaType.valueOf("application/catiaV5-Product"))
    addExtension("catmaterial", MediaType.valueOf("application/catiaV5-Material"))
    addExtension("catcatalog", MediaType.valueOf("application/catiav5Catalog"))
    addExtension("3dxml", APPLICATION_3DXML)
    addExtension("3dxml", APPLICATION_CGR)
    addExtension("cgr", APPLICATION_CGR)
    addExtension("cgr", MediaType.valueOf("application/catia-3D"))
    addExtension("jt", APPLICATION_JT)
    addExtension("plmxml", APPLICATION_PLMXML)
    addExtension("vfz", APPLICATION_VFZ)

    def getExtension(mediatype: MediaType): String = mediatype.toString match {
      case "image/jpeg" => "jpg"
      case "image/x-png" => "png"
      case _ => super.getExtension(mediatype) match {
        case null => "unknown"
        case e => e
      }
    }
  }

  def getPreferredMediaType(request: Request) = {
    val acceptedmediatypes = request.getClientInfo.getAcceptedMediaTypes
    val mediatype = {
      if (0 < acceptedmediatypes.size) acceptedmediatypes.get(0).getMetadata match {
        case MediaType.ALL => MediaType.APPLICATION_JSON
        case m => m
      }
      else MediaType.APPLICATION_JSON
    }
    mediatype
  }

  def setPreferredMediaType(request: Request, mediatype: MediaType) = {
    val preferredmediatypes = new java.util.LinkedList[Preference[MediaType]]
    preferredmediatypes.add(new Preference[MediaType](mediatype, 1.f))
    request.getClientInfo.setAcceptedMediaTypes(preferredmediatypes)
  }

}

