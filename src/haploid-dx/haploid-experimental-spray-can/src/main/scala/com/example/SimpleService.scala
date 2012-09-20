package com.example
import com.ibm.haploid.rest.HaploidService
import java.io.File

class SimpleService extends HaploidService {

  lazy val service = {
    path("test") {
      completeWith("Hallo")
    }
  }

}