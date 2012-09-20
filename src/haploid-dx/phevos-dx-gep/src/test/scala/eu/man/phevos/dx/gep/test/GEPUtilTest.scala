package eu.man.phevos.dx.gep.test

import java.io.{ File, FileOutputStream }
import java.nio.file.{ FileSystems, Paths }
import org.junit.Assert._
import org.junit.Test
import org.restlet.data.MediaType
import com.ibm.haploid.core.service._
import com.ibm.haploid.core.file.temporaryDirectory
import eu.man.phevos.dx.gep._
import eu.man.phevos.dx.util.interfaces.PartInfo
import java.nio.file.Path

@Test private class GEPUtilTest {
  
//  @Test def testDownload = {
//    val result = GEPServices.DownloadFile(DownloadCATPartInput("81.43704-0077", "_E_", FileSystems.getDefault.getPath("C:\\tmp\\testcatpart.CATPart")))
//    println(result)
//  }
  
//  @Test def testGetNativeFormats = {
//    // "36.77825-8004", "___"
//    // "88.76001-0073", "_H_"
//      //val natives = GEPServices.GetNativeCatFormats(PartMetadataInput("36.73201-4549", "_A_")) match {
//    val natives = GEPServices.GetNativeCatFormats(PartMetadataInput("65.41102-0001", "_A_")) match {
//      case Success(s) ⇒
//        println("isAssembly= " + s.isAssembly)
//        println("prcname= " + s.prcname)
//        println("natives= " + s.nativenames)
//      case Failure(e) ⇒ {
//        println("Failure : " + e.getMessage)
//      }
//    }
//  }
  //  @Test def testGepJpgDownload {
  //    val tempDir = temporaryDirectory
  //    val workDir = tempDir.getAbsolutePath.replace("""\""", """/""")
  //    val testJpgFile = workDir + "/" + testfilenamejpg
  //
  //    val queryurl = "/plm/divisions/truck/subsystems/enovia5/versions/?like%26partname%2681.93030-2081%26sort%26partname&from=0&to=50"
  //
  //    val formatssummary = {
  //      GEPServices.Lookup(LookupLinkInput(queryurl, "___", "formatssummary")) match {
  //        case Success(s) =>
  //          s
  //        case Failure(e) =>
  //          throw e
  //      }
  //    }
  //
  //    val downloadurl = GEPServices.Lookup(LookupFileUrlInput(formatssummary, "jpg", "muc")) match {
  //      case Success(s) => s
  //    }
  //
  //    val path = FileSystems.getDefault().getPath(testJpgFile)
  //
  //    GEPServices.DownloadFile(DownloadFileInput(downloadurl, MediaType.IMAGE_JPEG, path)) match {
  //      case Success(s) =>
  //        s
  //        println("Downloaded " + s)
  //      case Failure(e) => throw e
  //    }
  //  }
  //
  //  @Test def testLookupId {
  //    val url = "/plm/divisions/truck/subsystems/enovia5/versions/?like%26partname%2681.93030-2081%26sort%26partname&from=0&to=50&token=123"
  //
  //    GEPServices.Lookup(LookupFieldInput(url, "___", "id")) match {
  //      case Failure(e) => throw e
  //      case Success(s) =>
  //        assertEquals(s, "5EB7D5820098007C4A2FB7170009A957")
  //    }
  //  }
  //
  //  @Test def testLookupJpgLink {
  //    val url = "/plm/divisions/truck/subsystems/enovia5/versions/?like%26partname%2681.93030-2081%26sort%26partname&from=0&to=50"
  //
  //    GEPServices.Lookup(LookupLinkInput(url, "___", "formatssummary")) match {
  //      case Success(s) =>
  //        assertEquals(s, "/plm/divisions/truck/subsystems/enovia5/versions/5EB7D5820098007C4A2FB7170009A957/formats/summary/")
  //      case Failure(e) => throw e
  //    }
  //  }
  //
  //  @Test def testLookupJpgURL = {
  //    val url = "/plm/divisions/truck/subsystems/enovia5/versions/5EB7D5820098007C4A2FB7170009A957/formats/summary/"
  //
  //    GEPServices.Lookup(LookupFileUrlInput(url, "jpg", "muc")) match {
  //      case Success(s) =>
  //        assertEquals(s, "/plm/divisions/truck/subsystems/enovia5/vaults/muc/nativeformats/jpg/5EB7D5820098007C4A2FB717000A1CF5/2F73656375726564302F36363931444430323030383830303434344544374135394430303041463738462E5661756C74/38315F39333033305F323038315F4733445F303030315F5F5F5F2E6A7067/")
  //      case Failure(e) =>
  //        throw e
  //    }
  //  }
  //
  //  @Test def testGepTiffDownload {
  //    val tempDir = temporaryDirectory
  //    val workDir = tempDir.getAbsolutePath.replace("""\""", """/""")
  //    val testTiffFile = workDir + "/" + testfilenametiff
  //
  //    val queryurl = "/plm/divisions/truck/subsystems/enovia5/versions/?like%26partname%2683.28205-8530%26sort%26partname&from=0&to=50"
  //
  //    val formatssummary = GEPServices.Lookup(LookupLinkInput(queryurl, "_H_", "formatssummary")) match {
  //      case Success(s) =>
  //        s
  //      case Failure(e) =>
  //        throw e
  //    }
  //
  //    val downloadurl = GEPServices.Lookup(LookupFileUrlInput(formatssummary, "tiff")) match {
  //      case Success(s) =>
  //        s
  //      case Failure(e) =>
  //        throw e
  //    }
  //
  //    val path = FileSystems.getDefault().getPath(testTiffFile)
  //    GEPServices.DownloadFile(DownloadFileInput(downloadurl, MediaType.IMAGE_TIFF, path)) match {
  //      case Success(s) =>
  //        s
  //        println("Downloaded " + s)
  //      case Failure(e) => throw e
  //    }
  //    val fileOutputStream = new FileOutputStream(testTiffFile)
  //  }
  //
  //  @Test def testLookupTiffLinks = {
  //    val url = "/plm/divisions/truck/subsystems/enovia5/versions/?like%26partname%2681.51200-8623%26sort%26partname&from=0&to=50"
  //
  //    // 81.51200-8623 _R_
  //    val tifflist = GEPServices.GetList(LookupTiffUrlsInput("33.77115-8558", "_B_")) match {
  //      case Success(s) =>
  //        s
  //      case Failure(e) => {
  //        throw e
  //      }
  //    }
  //  }

  //  /*@Test*/ def testGepCATPartDownload {
  //    val tempDir = temporaryDirectory
  //    val workDir = tempDir.getAbsolutePath.replace("""\""", """/""")
  //    val testTiffFile = workDir + "/testCATPart.CATPart"
  //
  //    val downloadurl = GEPServices.DownloadFile(DownloadCATPartInput("XX.SAC00-0562", "___", Paths.get(testTiffFile))) match {
  //      case Success(s) =>
  //        s
  //      case Failure(e) =>
  //        throw e
  //    }
  //
  //    assertTrue(new File(downloadurl.toAbsolutePath.toString).exists)
  //  }

  //  @Test def testGepPartMetadata {
  //    val tempDir = temporaryDirectory
  //    val workDir = tempDir.getAbsolutePath.replace("""\""", """/""")
  //    val testTiffFile = workDir + "/testCATPart.CATPart"
  //
  //    val partinfo = new PartInfo("XX.SAC00-0562", "___", "XX.SAC00-0562", "T.EST.MTB.000.AA", None, None, None, "T.EST.MTB.000.AA", false, false, false, "03")
  //    val downloadurl = GEPServices.GetMetadata(partinfo) match {
  //      case Success(s) =>
  //        s
  //      case Failure(e) =>
  //        throw e
  //    }
  //  }

  private val testfilenamejpg = "testGEPOutput.jpg"
  private val testfilenametiff = "testGEPOutput.tiff"
}