package com.ibm.haploid.disruptor

class Sequencer(
    claimstrategy: ClaimStrategy, 
    waitstrategy: WaitStrategy) {

  def setGatingSequences(sequences: Array[Sequence]) = gatingsequences = sequences

  def newBarrier(sequences: Array[Sequence]): SequenceBarrier = new ProcessingSequenceBarrier(waitstrategy, cursor, sequences)

  def newBarrier: SequenceBarrier = newBarrier(Sequencer.nullsequences)

  def newBatchDescriptor(size: Int) = new BatchDescriptor(math.min(size, claimstrategy.getBufferSize))

  def getBufferSize = claimstrategy.getBufferSize

  def getCursor = cursor.get

  def hasAvailableCapacity(capacity: Int) = claimstrategy.hasAvailableCapacity(capacity, gatingsequences)

  def next: Long = {
    claimstrategy.incrementAndGet(gatingsequences)
  }

  def next(timeout: Long, unit: TUnit): Long = {
    waitForCapacity(1, timeout, unit)
    next
  }

  def next(batchdescriptor: BatchDescriptor): BatchDescriptor = {
    val sequence = claimstrategy.incrementAndGet(batchdescriptor.getSize, gatingsequences)
    batchdescriptor.setEnd(sequence)
    batchdescriptor
  }

  def next(batchdescriptor: BatchDescriptor, timeout: Long, unit: TUnit): BatchDescriptor = {
    waitForCapacity(1, timeout, unit)
    next(batchdescriptor)
  }

  def claim(sequence: Long) = {
    claimstrategy.setSequence(sequence, gatingsequences)
  }

  def publish(sequence: Long): Unit = {
    publish(sequence, 1)
  }

  def publish(batchdescriptor: BatchDescriptor): Unit = {
    publish(batchdescriptor.getEnd, batchdescriptor.getSize)
  }

  def forcePublish(sequence: Long) = {
    cursor.set(sequence)
    waitstrategy.signalAllWhenBlocking
  }

  private def publish(sequence: Long, batchsize: Int): Unit = {
    claimstrategy.serializePublishing(sequence, cursor, batchsize)
    waitstrategy.signalAllWhenBlocking
  }

  private[this] def waitForCapacity(size: Int, timeout: Long, unit: TUnit) = ()

  private[this] val cursor = new Sequence

  private[this] var gatingsequences: Array[Sequence] = Sequencer.nullsequences

}

object Sequencer {

  final val nullsequences = new Array[Sequence](0)

}