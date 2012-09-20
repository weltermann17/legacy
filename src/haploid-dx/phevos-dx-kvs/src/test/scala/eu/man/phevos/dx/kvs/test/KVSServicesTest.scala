package eu.man.phevos.dx.kvs.test

import org.junit.Test
import com.ibm.haploid.core.service.Success
import com.ibm.haploid.dx.kvs.KVSObject.TYP_T
import com.ibm.haploid.dx.kvs.{ SelectKStandByKeyInput, KVSTypeconverter, KVSBaseServices }
import eu.man.phevos.dx.kvs.services.{ PartNameLanguage, GetPartNameInput, GetHighestKStandInput, CheckPartNumberExistsInput, CheckDVForEZISFileExistsInput }
import eu.man.phevos.dx.kvs.KVSServices
import eu.man.phevos.dx.util.interfaces.{ PartInfo, MTBPartMetadata }
import eu.man.phevos.dx.util.interfaces.MTBPartFile

@Test class KVSServicesTest extends KVSTypeconverter {

  val partinfo = new PartInfo("33.77115-8558", "_H_", "33.77115-8558", "T.EST.MTB.000.AA", None, None, None, "T.EST.MTB.000.AA", false, false, false, "03", "33.K4359.9")
  //new PartInfo("81.25902-8114", "BA_", "81.25902-8114", "DUM.002.005.A", None,None,Some(1330729200000L),"DUM.002.005.A",true,false,false,"03")

  val metadata = MTBPartMetadata("33771158558_01_H_CX_pre.tif")

  @Test def checkPartNumberExists {
    KVSServices.CheckPartNumberExists(CheckPartNumberExistsInput(partinfo)) match {
      case Success(b) ⇒
        println("CheckPartNumberExists: " + b)
    }
  }

  @Test def checkDVTIFFExists {
    KVSServices.CheckDVForEZISFileExists(CheckDVForEZISFileExistsInput(partinfo, metadata)) match {
      case Success(b) ⇒
        println("Check DV For TIFF Exists: " + b)
    }
  }

  @Test def getPartname {
    KVSServices.GetPartName(GetPartNameInput(partinfo, PartNameLanguage.English)) match {
      case Success(s) ⇒
        println("GetPartName (English): " + s)
    }
  }

  @Test def getHighestKStand {
    // Test with T.EST.MIW.MAN.01
    KVSServices.GetHighestKStand(GetHighestKStandInput(partinfo)) match {
      case Success(i) ⇒
        println("GetHighestKStand: " + i)
    }
  }

  @Test def selectKStand {
    val map = Map("TEIL_KEY" -> "T.EST.MIW.MAN.01")
    KVSBaseServices.SelectKStand(SelectKStandByKeyInput("T.EST.MIW.MAN.01:K9", TYP_T, "MANNIM")) match {
      case Success(s) ⇒
        println(s)
    }
  }

  @Test def testMetadataZeug {
    println(MTBPartMetadata("33771158558_01_H_CX_pre.tif"))
    println(MTBPartMetadata("EST_MAN_001_A__DRW_TZ__005_____BLATT_19__20121312_36_26430_2000__A__kn.tif"))
    println(MTBPartMetadata("23B_767_401_A__DMU_TM__005_____BRACKET_LEFT_______36_26430_2000__A____.tar"))
    println(MTBPartMetadata("23B_767_401_A__RAW_CPL_005_____PARAMETRIC_FILES___36_26430_2000__A____.zip"))
  }

  @Test def testEZISFile {
    MTBPartMetadata("T_EST_MTB_000_AA_DRW_TZ__001_____BLATT_3___20120627_81_96210_0450__H__KN.tif")
  }

}