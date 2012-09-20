package com.ibm.haploid.disruptor

import java.util.concurrent.locks.ReentrantLock

final class BlockingWaitStrategy extends WaitStrategy {

  def signalAllWhenBlocking = {
    if (0 != waiters) {
      lock.lock
      try {
        processornotifycondition.signalAll
      } finally {
        lock.unlock
      }
    }
  }

  def waitFor(sequence: Long, cursor: Sequence, sequences: Array[Sequence], barrier: SequenceBarrier) = {
    var availablesequence = cursor.get
    if (sequence > availablesequence) {
      lock.lock
      try {
        waiters += 1
        while (sequence > { availablesequence = cursor.get; availablesequence }) {
          barrier.checkAlert
          processornotifycondition.await
        }
      } finally {
        waiters -= 1
        lock.unlock
      }
    }
    if (0 < sequences.length) {
      while (sequence > { availablesequence = getMinimumSequence(sequences); availablesequence }) {
        barrier.checkAlert
      }
    }
    availablesequence
  }

  def waitFor(sequence: Long, cursor: Sequence, sequences: Array[Sequence], barrier: SequenceBarrier, timeout: Long, unit: TUnit) = {
    var availablesequence = cursor.get
    if (sequence > availablesequence) {
      lock.lock
      try {
        waiters += 1
        while (sequence > { availablesequence = cursor.get; availablesequence }) {
          barrier.checkAlert
          processornotifycondition.await(timeout, unit)
        }
      } finally {
        waiters -= 1
        lock.unlock
      }
    }
    if (0 < sequences.length) {
      while (sequence > { availablesequence = getMinimumSequence(sequences); availablesequence }) {
        barrier.checkAlert
      }
    }
    availablesequence
  }

  private[this] val lock = new ReentrantLock

  private[this] val processornotifycondition = lock.newCondition

  @volatile private[this] var waiters = 0

}