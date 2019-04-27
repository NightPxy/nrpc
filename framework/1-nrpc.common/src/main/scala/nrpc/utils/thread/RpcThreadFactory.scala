package nrpc.utils.thread

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

import nrpc.rpc.RpcHubEnv

class RpcThreadFactory(context:RpcHubEnv, tag:String="*") extends ThreadFactory{
  private val threadNo = new AtomicInteger(0)
  override def newThread(r: Runnable): Thread = {
    val thread = new Thread(r)
    thread.setName(s"${context.name}-$tag-thread-${threadNo.decrementAndGet()}")
    thread.setDaemon(context.isThreadDaemon)
    return thread
  }
}
