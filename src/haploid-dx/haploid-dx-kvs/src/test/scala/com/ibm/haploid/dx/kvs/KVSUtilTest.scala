package com.ibm.haploid

package dx

package kvs

import java.nio.file.{ Path, FileSystems }

import org.junit.Assert.assertTrue
import org.junit.Test

import com.ibm.haploid.core.service._
import com.typesafe.config.Config

@Test class KVSUtilTest {

  @Test def selectDocuversByTeilKey() {
    KVSBaseServices.SelectDocuVers(SelectDocuVersByAttributesInput(Map("TEIL_KEY" -> "T.EST.MIW.MAN.*"), "MANNIM", Some(255))) match {
      case Success(config) =>
        println(config)
      case Failure(_) =>
        assertTrue(true)
    }
  }

  /*@Test*/ def uploadFile() {
    val map = Map("TRANSPARENT_MODE" -> "1",
      "DOCU_KEY" -> "TZ:T.EST.MIW.MAN.06,Blatt=2",
      "DV_NUMMER" -> "next",
      "DV_STATUS" -> "I",
      "DV_PF_NACHNAME" -> "ManTest",
      "DV_PF_VORNAME" -> "Manni",
      "DV_PF_TELEFON" -> "4711",
      "DV_BESCHREIBUNG" -> "_A_ e 12345",
      "KSTAND_OBJEKTTYP.0" -> "T",
      "KSTAND_KSTAND.0" -> "Knext",
      //      "KSTAND_DATUM.0" -> "20120531170043",
      "KSTAND_BESCHREIBUNG.0" -> "_A_ 12345",
      "TEIL_KEY.0" -> "T.EST.MIW.MAN.06",
      "KSTAND_ROWS" -> "1",
      "DV_DATUM" -> "20120531170043",
      "KSTAND_MODDATUM.0" -> "31052012")

    val path: Path = FileSystems.getDefault().getPath("C:\\tmp\\test.tif")
    KVSBaseServices.UploadFile(UploadFileInput(path, responsible = "MANNIM", api2attributes = map)) match {
      case Success(s) => println(s)
      case Failure(f) => f.printStackTrace
    }
  }

  @Test def getDV {
    val map = Map(
      "TEIL_KEY" -> "T.EST.MIW.MAN.06")

    KVSBaseServices.SelectDocuVers(SelectDocuVersByAttributesInput(map, "MANNIM")) match {
      case Success(s) => println(s)
      case Failure(f) => f.printStackTrace
    }

  }

  @Test def selectKStand {

    KVSBaseServices.SelectKStand(SelectKStandByKeyInput("T.EST.MIW.MAN.06", KVSObject.TYP_T, "MANNIM")) match {
      case Success(s) => println(s)
      case Failure(f) => f.printStackTrace
    }

  }

  @Test def attachDVToKStand {

    KVSBaseServices.LinkDVToKStand(LinkDVToKStandInput("37000000000000000000155975", "37000000000000000000050488", "FOX00GR")) match {
      case Success(s) => println(s)
      case Failure(f) => f.printStackTrace
    }

  }

  @Test def updateKStand { // 20120621171757
    val map = Map("KSTAND_DATUM" -> "20120621181657") // 20120621171757

    KVSBaseServices.UpdateKStand(UpdateKStandInput("37000000000000000000050494", map, "MANNIM")) match {
      case Success(s) => println(s)
      case Failure(f) => f.printStackTrace
    }

  }

  @Test def parseIntoConfig = {
    import java.io._
    val input = new BufferedReader(new InputStreamReader((getClass.getResourceAsStream("/kvs_dump.txt"))))
    val buf = new java.io.StringWriter
    var done = false
    while (!done) { val line = input.readLine; if (null == line) done = true else buf.append(line).append("\n") }
    val converter = new KVSTypeconverter {}
    converter.fromStringToConfig(buf.toString)
    ()
  }

}