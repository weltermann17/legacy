package eu.man.phevos

package dx

package ezis

import org.junit.Test
import org.junit.Before
import org.junit.After
import org.junit.Assert._
import com.ibm.haploid.core.file._
import java.nio.file._
import eu.man.phevos.dx.ezis.EzisServices._
import com.ibm.haploid.core.service._
import java.io.IOException
import cc.spray.io.IoClient.IoClientException
//import cc.spray.io.IoClient.IoClientException

@Test private class EzisUtilTest {

  @Test def testUnstampedTiff {

    val tempDir = temporaryDirectory
    val workDir = tempDir.getAbsolutePath.replace("""\""", """/""") + "/"

    val result = eu.man.phevos.dx.ezis.EzisServices.UnstampedTiff(TiffInput("33.77115-8558", "_B_", 1, Paths.get(workDir)))

    result match {
      case Success(x) => {
        assertTrue(Files.exists(x.path))
        Files.delete(x.path)
      }
      case Failure(x) =>
        x match {
          case ex: java.lang.RuntimeException => {
            ex match {
              case ioe: cc.spray.io.IoClient.IoClientException => {
                println("Ignore failure.\n" + ex)
              }
              case _ => {
                fail("Exceptionclass:\n" + ex)
              }
            }
          }
          case _ => {
            fail("" + x.printStackTrace)
          }
        }
    }
  }

  //  @Test def testStampedTiff {
  //
  //    val tempDir = temporaryDirectory
  //    val workDir = tempDir.getAbsolutePath.replace("""\""", """/""") + "/"
  //
  //    val result = eu.man.phevos.dx.ezis.EzisServices.StampedTiff(TiffInput("36.74406-8054", "___", 1, Paths.get(workDir)))
  //
  //    result match {
  //      case Success(x) => {
  //        assertTrue(Files.exists(x.path))
  //        Files.delete(x.path)
  //      }
  //      case Failure(x) => fail("Either was Left. Exception:\n" + x.printStackTrace)
  //    }
  //  }

}