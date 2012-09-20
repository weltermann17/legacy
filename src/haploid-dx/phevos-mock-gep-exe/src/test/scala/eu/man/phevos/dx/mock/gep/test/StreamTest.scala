package eu.man.phevos.dx.mock.gep.test

import java.io.FileInputStream
import java.util.Arrays
import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props

object StreamTest extends App {

  case class FileChunk(buffer: Array[Byte], i: Int)

  class ChunkingActor(stream: Stream[FileChunk]) extends Actor {
    def receive = {
      case current #:: remaining =>
        println("Received: " + current)
        self ! remaining
    }
  }

  def fileChunkStream(): Stream[FileChunk] = {
    val fis = new FileInputStream("C:/tmp/XX_MOM00_0337_PRC.CATProduct.jar")
    val chunkSize = 512

    def chunkStream(c: Int = 0): Stream[FileChunk] = {
      val buffer = new Array[Byte](chunkSize)
      val bytesRead = fis.read(buffer)

      if (bytesRead > 0) {
        val chunkBytes = if (bytesRead == buffer.length) buffer else Arrays.copyOfRange(buffer, 0, bytesRead)
        Stream.cons(FileChunk(chunkBytes, c), chunkStream(c + 1))
      } else Stream.Empty
    }

    chunkStream()
  }

  implicit val system = ActorSystem()
  def log = system.log

  def test = {
    val stream = fileChunkStream()
    stream
  }

  def execute(f: => Stream[FileChunk]) {
    val stream = f
    val actor = system.actorOf(Props(new ChunkingActor(stream)))
    actor ! stream
    println("ENDE")
  }
  
  execute(test)
  
//  class Marshaller[C]
//  
//  class MarshallerC extends Marshaller[C] 
//  
//  class C
//  
//  def test[B : Marshaller](clazz: B) {
//    println(marshaller.getClass().toString()) // class eu.man.phevos.dx.mock.gep.test.StreamTest$C
//  }
//  
//  implicit val marshaller = new MarshallerC()
//  
//  val x = test(new C())
}