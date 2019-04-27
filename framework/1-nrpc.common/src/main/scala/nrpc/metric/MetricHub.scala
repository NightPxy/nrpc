package nrpc.metric

import com.lmax.disruptor.WaitStrategy
import com.lmax.disruptor.dsl.ProducerType
import nrpc.rpc.RpcHubEnv
import nrpc.utils.mq.disruptor.{DisruptorMemoryMQ, DisruptorMemoryMQCfg}

class MetricHub(context: RpcHubEnv) {

  object RequestMetric {
    var count = 0L
    var totalTime = 0L
    var avgTime = 0L

    def add(time: Long) = {
      this.count += 1
      this.totalTime += time
      this.avgTime = this.totalTime / this.count
    }

    override def toString: String = {
      return s"count:$count  totalTime:$totalTime  avgTime:$avgTime"
    }
  }

  private val queue = new DisruptorMemoryMQ[MetricEvent]("Metric", context, new DisruptorMemoryMQCfg(
    eventCreator = () => MetricEvent(MetricEventType.UNDEFINED, 0),
    bufferCount = 2 << 16,
    productType = ProducerType.MULTI,
    waitStrategy = new com.lmax.disruptor.YieldingWaitStrategy()
  )) {
    override def consume(message: MetricEvent): Unit = {
      message.eventType match {
        case MetricEventType.REQUEST => RequestMetric.add(message.time)
        case _ =>
      }
    }
  }

  def start():this.type  =  {
    this.queue.start()
    this
  }

  def requestTime(time: Long) = {
    this.queue.produce(MetricEvent(MetricEventType.REQUEST,time))
  }
}
