package com.ibm.haploid

package dx

package kvs

sealed abstract class KVSObject(val value: String, val attrPrefix: String = "") {

  val id = attrPrefix + "_ID"
  val idCoded = id + "_CODED"
  val key = attrPrefix + "_KEY"

}

sealed abstract class KVSObjectType(val value: String)

object KVSObject {

  case object TEIL extends KVSObject("teil", "TEIL")
  case object KSTAND extends KVSObject("kstand", "KSTAND") {

    sealed abstract class TYPE(val value: String) {
      val ATTR = "KSTAND_OBJEKTTYP"
    }

    case object TYPE_TEIL extends TYPE("T")
    case object TYPE_BM extends TYPE("B")

  }
  case object ZUORDNUNG extends KVSObject("zuordnung", "ZREL")
  case object DIAGNOSE extends KVSObject("diag")
  case object DOCUMENT extends KVSObject("document", "DOCU")
  case object DOCUVERS extends KVSObject("docuvers", "DV")
  case object FILE extends KVSObject("file")

  case object TYP_T extends KVSObjectType("T")
  case object TYP_B extends KVSObjectType("B")
}

sealed abstract class API_FUNC(val value: String)
case object API_DIAGNOSE extends API_FUNC("co.url_diag")
case object API_DOWNLOAD extends API_FUNC("checkout.download")
case object API_INFO extends API_FUNC("co.api_info")
case object API_SELECT extends API_FUNC("co.url_select")
case object API_UPLOAD extends API_FUNC("wp.checkin(API2_ERRNO=/DE/checkin/ci_batch_ko.txt;/DE/checkin/ci_batch_ok.txt)")
case object API_UPDATE extends API_FUNC("co.url_update")
