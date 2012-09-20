package eu.man

package phevos

package dx

package crt

import java.io.{ FileNotFoundException, File }
import java.sql.Timestamp

import scala.collection.mutable.ListBuffer
import scala.xml.{ XML, Node, Elem }

import com.ibm.haploid.core.{ newLogger, config }

import CrtServices.EntitledParts
import util.interfaces.PartInfo

object CrtUnload {

  val logger = newLogger(this)

  def getJobList(): EntitledParts = {

    val details: List[Map[String, String]] =
      {
        try {

          val foldername = config.getString("phevos.dx.unload.folder")
          if (foldername == "") throw new FileNotFoundException("folder for unload file not found : " + foldername)

          val filename = config.getString("phevos.dx.unload.file")

          val file = if (filename == "") {
            val files = (new File(foldername).listFiles).toList
            if (files.length > 0) files.sortWith(_.lastModified > _.lastModified)(0) else throw new java.io.FileNotFoundException("no unload file in : " + foldername)

          } else {

            new File(foldername, filename)

          }

          if (!file.exists) throw new FileNotFoundException("unload file not found : " + file.getAbsoluteFile)

          val xml: Elem = XML.loadFile(file)

          val parts = new ListBuffer[Map[String, String]]

          (xml \ "datarow").foreach((datarow: Node) ⇒ {

            var datamap: Map[String, String] = Map()

            (datarow.child.foreach((data: Node) ⇒ {

              datamap = datamap + (data.label -> data.text)

            }))
            
            datamap = datamap + (mapping.unloadFile -> ("""<?xml version="1.0" encoding="UTF-8" ?>""" + "\n<unload>\n" + datarow.toString + "\n</unload>"))

            parts += datamap.toMap[String, String]

          })

          parts.toList
        } catch {
          case (e: Exception) ⇒ {
            logger.warning("Parsing unload file failed : " + e.getMessage)
            List()
          }
        }
      }

    if (details != List()) {
      EntitledParts(

        details.map(entry ⇒

          PartInfo(
            entry.get(mapping.mtbPartNumber) match {
              case Some(s) if (s.length == 11) ⇒
                s.substring(0, 2) + "." + s.substring(2, 7) + "-" + s.substring(7, 11)
              case Some(s) ⇒
                s.trim
              case None ⇒
                ""
            },

            entry.get(mapping.mtbPartIndex) match {
              case Some(s) ⇒
                s.replace(" ", "_")
              case None ⇒
                ""
            },

            entry.get(mapping.mtbDefiningIdent) match {
              case Some(s) ⇒
                s.trim
              case None ⇒
                ""
            },

            entry.get(mapping.vwPartNumber) match {
              case Some(s) ⇒
                s.trim
              case None ⇒
                ""
            },

            entry.get(mapping.vwKStand) match {
              case Some(s) if (s.length > 0) ⇒
                Some(s.trim)
              case _ ⇒
                None
            },

            entry.get(mapping.vwChangeNumber) match {
              case Some(s) if (s.length > 0) ⇒
                Some(s.trim)
              case _ ⇒
                None
            },

            entry.get(mapping.vwDrawingDate) match {
              case Some(s) if (s.length == 10) ⇒
                val timestring = (s.substring(6, 10) + "-" + s.substring(3, 5) + "-" + s.substring(0, 2) + " 00:00:00")
                Some(Timestamp.valueOf(timestring).getTime())
              case _ ⇒
                None
            },

            entry.get(mapping.vwDefiningIdent) match {
              case Some(s) ⇒
                s.trim
              case None ⇒
                ""
            },

            entry.get(mapping.knRelease) match {
              case Some("K") | Some("k") ⇒
                true
              case _ ⇒
                false
            },

            entry.get(mapping.titleblock) match {
              case Some("y") | Some("Y") ⇒
                true
              case _ ⇒
                false
            },

            entry.get(mapping.mtbStandardPart) match {
              case Some("N") | Some("n") ⇒
                true
              case _ ⇒
                false
            },

            entry.get(mapping.dxstatus) match {
              case Some(s) ⇒
                s.trim
              case None ⇒
                ""
            },
            entry.get(mapping.mtbChangeNumber) match {
              case Some(s) ⇒
                s.trim
              case None ⇒
                ""
            },
            entry.get(mapping.unloadFile) match {
              case Some(s) =>
                s
              case None =>
                "Unload File not available."
            })))
    } else {
      EntitledParts(List())
    }

  }

}