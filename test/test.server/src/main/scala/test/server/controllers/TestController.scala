package test.server.controllers

import java.util.concurrent.atomic.LongAdder
import java.util.concurrent.{ArrayBlockingQueue, ThreadPoolExecutor, TimeUnit}

import nrpc.annotation.RequestMapping
import nrpc.rpc.{RpcRequest, RpcResponse}
import nrpc.service.RpcContext

@RequestMapping("/test")
class TestController {

  private val threadPool = new ThreadPoolExecutor(
    1, //corePoolSize =
    5, //maximumPoolSize =
    15, //keepAliveTime =
    TimeUnit.SECONDS, //unit =
    new ArrayBlockingQueue[Runnable](10),
    new ThreadPoolExecutor.AbortPolicy()
  )



  private class DoSomethingThread(count: Int,context: RpcContext) extends Runnable {
    override def run(): Unit = {
      for(i <- 1 to count) {
        context.sendResponse(s"$i-do [$count] step $i");
        Thread.sleep(500)
      }
    }
  }

  @RequestMapping("/single")
  def single(request: RpcRequest, context: RpcContext): String = {
    val content = request.content()
    val proccess = s"$content-开始处理"
    println("proccess"+proccess)
    return proccess;
  }

  @RequestMapping("/mul")
  def mul(request: RpcRequest, context: RpcContext): String = {
    val content = Integer.parseInt(request.content())
    val proccess = s"$content-开始处理"
    threadPool.execute(new DoSomethingThread(content,context))
    return proccess;
  }
}
