package nrpc.utils.mq.poll

import java.util.concurrent.TimeUnit

import nrpc.rpc.RpcHubEnv
import nrpc.utils.mq.MemoryMQ

/**
  * 轮询型消息队列(不只根据队列消息触发,没有消息时也会根据Sleep触发)
  *    注意:该队列实现中 consume(message: T) 可能传入null消息
  * 这种消息队列主要应用在过期扫描中
  *    如果完全依靠消息订阅,那么如果在某个时间段内没有新消息产生,会影响到历史过期扫描
  *    所以使用这种队列,强制没有消息但间隔一定时间后触发
  */
abstract class PollMemoryMQ[T](name: String, context: RpcHubEnv) extends MemoryMQ[T](name, context) {
  private val me = this
  private val queue = new java.util.concurrent.LinkedBlockingQueue[T]()
  private val thread = new Thread(new Runnable {
    override def run(): Unit = {
      while (true) {
        try {
          val message = me.queue.poll(3000,TimeUnit.MILLISECONDS)
          me.consume(message)
        }
        catch {
          case e:Throwable => context.log.error(e,s"PollMemoryMQ $name error:${e.getMessage}")
        }
      }
    }
  })
  thread.setDaemon(true)
  thread.setName(s"thread-$name")

  override def produce(message: T): Unit = queue.offer(message)

  override def start(): PollMemoryMQ.this.type = {
    thread.start()
    this
  }
}
