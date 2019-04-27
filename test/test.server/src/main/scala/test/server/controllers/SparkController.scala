package test.server.controllers

import java.util.concurrent.{ArrayBlockingQueue, ThreadPoolExecutor, TimeUnit}

import nrpc.annotation.RequestMapping
import nrpc.rpc.RpcRequest
import nrpc.service.RpcContext
import test.server.compile.{CusSparkILoop, ScalaCompiler}

@RequestMapping("/spark")
class SparkController {

  private val threadPool = new ThreadPoolExecutor(
    1, //corePoolSize =
    5, //maximumPoolSize =
    15, //keepAliveTime =
    TimeUnit.SECONDS, //unit =
    new ArrayBlockingQueue[Runnable](10),
    new ThreadPoolExecutor.AbortPolicy()
  )

  private class SparkThread(doCode:String, context: RpcContext) extends Runnable {
    override def run(): Unit = {
      val code = s"""
        $doCode
        spark.stop()
      """.stripMargin
      val result = CusSparkILoop.run(code)
      context.sendResponse(result)
    }
  }

  @RequestMapping("/scala")
  def scala(request: RpcRequest, context: RpcContext): String = {
    val content = request.content()

    val code = content
    println(code)
    try
    {
      ScalaCompiler.compile(code)
      return ScalaCompiler.evalAs[Any](code).toString
    }
    catch {
      case e:Throwable => return e.getMessage
    }
  }

  @RequestMapping("/execute")
  def execute(request: RpcRequest, context: RpcContext): String = {
    val content = request.content()
    try
    {
      threadPool.execute(new SparkThread(content,context))
      return "Success"
    }
    catch {
      case e:Throwable => return e.getMessage
    }
  }
}
