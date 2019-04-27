package nrpc.metric

import nrpc.metric.MetricEventType.MetricEventType
import nrpc.utils.mq.disruptor.DisruptorMemoryMQMessage


case class MetricEvent(var eventType: MetricEventType,var time: Long) extends DisruptorMemoryMQMessage {
  override def cloneTo(to: DisruptorMemoryMQMessage): Unit = {
    val t = to.asInstanceOf[MetricEvent]
    t.eventType = eventType
    t.time = time
  }
}
