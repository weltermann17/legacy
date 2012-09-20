package com.ibm.haploid

package dx

package engine

package channel

import java.util.concurrent.atomic.AtomicLong

import org.junit.Test
import akka.actor._
import akka.pattern._
import akka.util.Timeout

import core.concurrent.actorsystem
import core.util.Uuid.newUuid
import event._
import journal._
import channel._

class Job extends Actor {

  def receive = {
    case entry: JournalEntry => counter += 1
    case "count" => println("jobs " + counter)
  }

  private[this] var counter = 0L

}

class Task extends Actor {

  def receive = {
    case entry: JournalEntry => counter += 1
    case "count" => println("tasks " + counter)
  }

  private[this] var counter = 0L

}

@Test private class ChannelTest {

  @Test def test1 = {
//    val N = 1000
//    val job = actorsystem.actorOf(Props[Job]) 
//    val task = actorsystem.actorOf(Props[Task]) 
//    journal.channel.subscribe(job, JobEvent)
//    journal.channel.subscribe(task, TaskEvent)
//    def entry(i: Int) = i % 3 match { case 0 => JobCreate(newUuid) case 1 => TaskCreate(i) case 2 => JobCreate(i) }
//    for (i <- 0 until N) journal.append(entry(i)); journal.flush
//    println("written " + N)
//    journal.redo
//    job ! "count"
//    task ! "count"
  }

}

