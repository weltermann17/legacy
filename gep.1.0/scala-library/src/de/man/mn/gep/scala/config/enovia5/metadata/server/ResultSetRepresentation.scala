package de.man.mn.gep.scala.config.enovia5.metadata.server

import java.io.OutputStream
import java.io.PrintWriter

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.restlet.data.Disposition
import org.restlet.data.MediaType

import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.NString
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.RichPreparedStatement
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.RichResultSet
import com.ibm.de.ebs.plm.scala.database.PropertiesMapper

abstract class ResultSetRepresentation extends DatabaseRepresentation {

  override def doWrite(writer: PrintWriter) = {
    try {
      writer.print("{\"response\":{\"data\":[")
      var rows = from
      for (
        row <- prepare <<! (result => row(result))
      ) {
        if (from < rows) writer.print(",")
        writer.print(row)
        rows += 1
      }
      writer.print("],\"startRow\":")
      writer.print(this.from)
      writer.print(",\"endRow\":")
      writer.print(rows)
      writer.print(",\"totalRows\":")
      writer.print(computeTotal(rows))
      writer.print(",\"status\":0}}")
    } catch {
      case e =>
        writer.print("],\"status\":-1}}")
        throw e
    }
  }

  override def doWrite(out: OutputStream) = {
    try {
      val workbook = getMediaType match {
        case MediaType.APPLICATION_MSOFFICE_XLSX => new SXSSFWorkbook(pagesize * 10)
      }
      val cellDateStyle = workbook.createCellStyle
      cellDateStyle.setDataFormat(workbook.getCreationHelper.createDataFormat.getFormat("yyyy-mm-dd"))
      val excelSheet = workbook.createSheet
      val excelHeader = excelSheet.createRow(0)
      var rows = from
      var headers: List[String] = null
      for (
        row <- prepare <<! (result => row(result))
      ) {
        if (rows == from) {
          // header
          headers = row.getPropertiesNames
          var i = 0
          headers foreach { h =>
            excelHeader.createCell(i, Cell.CELL_TYPE_STRING).setCellValue(h.toUpperCase)
            i += 1
          }
        }
        var excelrow = excelSheet.createRow(rows + 1);
        var i = 0
        headers.foreach { h =>
          row.toMap.get(h) match {
            case Some(value) =>
              value match {
                case s: String => excelrow.createCell(i, Cell.CELL_TYPE_STRING).setCellValue(s)
                case s: NString => excelrow.createCell(i, Cell.CELL_TYPE_STRING).setCellValue(s)
                case b: Boolean => excelrow.createCell(i, Cell.CELL_TYPE_BOOLEAN).setCellValue(b)
                case int: Int => excelrow.createCell(i, Cell.CELL_TYPE_NUMERIC).setCellValue(int)
                case d: Double => excelrow.createCell(i, Cell.CELL_TYPE_NUMERIC).setCellValue(d)
                case d: java.sql.Date =>
                  val datecell = excelrow.createCell(i)
                  datecell.setCellValue(d)
                  datecell.setCellStyle(cellDateStyle)
                case t: java.sql.Timestamp =>
                  val datecell = excelrow.createCell(i)
                  datecell.setCellValue(t)
                  datecell.setCellStyle(cellDateStyle)
                case _ =>
              }
            case None =>
          }
          i += 1
        }
        rows += 1
      }
      for (i <- 0 to headers.size) excelSheet.autoSizeColumn(i)
      workbook.write(out)
    } catch {
      case e =>
        throw e
    }
  }

  override def addDisposition = {
    getMediaType match {
      case MediaType.APPLICATION_MSOFFICE_XLSX =>
        val disposition = new Disposition(Disposition.TYPE_ATTACHMENT)
        disposition.setFilename("Export.xlsx")
        disposition.setSize(-1)
        setDisposition(disposition)
      case _ =>
    }
  }

  protected def row(result: RichResultSet): PropertiesMapper

  protected def prepare: RichPreparedStatement

}