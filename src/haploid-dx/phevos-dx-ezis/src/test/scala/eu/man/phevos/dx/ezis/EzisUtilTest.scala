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
import com.ibm.haploid.core.util.text.stackTraceToString
import java.io.IOException
import cc.spray.io.IoClient.IoClientException

@Test private class EzisUtilTest {

  @Test def testUnstampedTiff {

    val tempDir = temporaryDirectory
    val workDir = tempDir.getAbsolutePath.replace("""\""", """/""") + "/"

    val result = eu.man.phevos.dx.ezis.EzisServices.UnstampedTiff(TiffInput("33.77115-8558", "_B_", 1, Paths.get(workDir)))

    result match {
      case Success(x) ⇒ {
        assertTrue(Files.exists(x.path))
        Files.delete(x.path)
      }
      case Failure(x) ⇒
        x match {
          case e: java.lang.RuntimeException ⇒ {
            e match {
              case ioe: cc.spray.io.IoClient.IoClientException ⇒
              case iob: java.lang.IndexOutOfBoundsException ⇒
              case _ ⇒ fail("Exceptionclass:\n" + e)
            }
          }
          case e: Throwable ⇒ {
            fail(stackTraceToString(e))
          }
        }
    }
  }

}