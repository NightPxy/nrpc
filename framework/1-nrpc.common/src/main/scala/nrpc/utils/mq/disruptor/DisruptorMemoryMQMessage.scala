package nrpc.utils.mq.disruptor

trait DisruptorMemoryMQMessage {
  def cloneTo(to: DisruptorMemoryMQMessage): Unit
}
