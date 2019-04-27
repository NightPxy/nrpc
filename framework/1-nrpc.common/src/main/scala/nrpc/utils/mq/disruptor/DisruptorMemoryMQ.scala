package nrpc.utils.mq.disruptor

import com.lmax.disruptor.dsl.Disruptor
import com.lmax.disruptor.{EventFactory, EventHandler}
import nrpc.rpc.RpcHubEnv
import nrpc.utils.All._
import nrpc.utils.mq.MemoryMQ
import nrpc.utils.thread.RpcThreadFactory

abstract class DisruptorMemoryMQ[T <: DisruptorMemoryMQMessage](name:String, context: RpcHubEnv, cfg:DisruptorMemoryMQCfg[T]) extends MemoryMQ[T](name,context){
  private val me = this
  protected val queue: Disruptor[T] = new Disruptor[T](
    new EventFactory[T] {
      override def newInstance(): T = cfg.eventCreator()
    },
    cfg.bufferCount,
    new RpcThreadFactory(context, s"disruptor-mq-$name"),
    cfg.productType,
    cfg.waitStrategy
  )

  queue.handleEventsWith(new EventHandler[T] {
    override def onEvent(t: T, l: Long, isBatchEnd: Boolean): Unit = {
      me.consume(t)
    }
  })


  def produce(message:T) = {
    val ringBuffer = queue.getRingBuffer
    val sequence  = ringBuffer.next()

    promiseProcess{
      val queueMessage =  ringBuffer.get(sequence)
      message.cloneTo(queueMessage)
    }{
      ringBuffer.publish(sequence)
    }

  }

  def start():this.type = {

    queue.start()
    this
  }
}
