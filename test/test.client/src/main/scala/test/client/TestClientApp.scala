package test.client

import java.util
import java.util.concurrent.atomic.LongAdder

import io.netty.util.CharsetUtil
import nrpc.client.callback.Callback
import nrpc.client.{RpcClient, RpcClientConf}
import nrpc.constants.value.{KeepAlive, ProtocolVersion, RpcProtocolType}
import nrpc.rpc.{RpcRequest, TransmissionMessage}

import scala.collection.mutable.ListBuffer

object TestClientApp {
  import nrpc.utils.All._
  def checkResult(prefix: String, count: Int, result: ListBuffer[String]) = {
    // 确定回调数量正确
    if (result.length != count) throw new Exception(s"$prefix 数量不对 $count:${result.length}")
    val sortResult = result.sortBy(x => {
      Integer.parseInt(x.split("-")(0))
    })
    //确定回调没有重复和遗漏
    for (i <- 0 until sortResult.length) {
      if (sortResult(i) != s"$i-$prefix-开始处理") {
        throw new Exception(s"$prefix 异常")
      }
    }
  }

  def ntcpSgl(count: Int, client: RpcClient) = {
    /*
      同步请求
     */
    val syncResult = ListBuffer[String]()
    for (i <- 0 until count) {
      val response = client.requestSync(new RpcRequest() {
        this.uri("/test/single")
        this.content(i.toString + "-sync")
      })
      syncResult.append(response.content())
    }

    checkResult("sync", count, syncResult)


    /*
      异步请求
      */
    val asyncResult = ListBuffer[String]()
    for (i <- 0 until count) {
      client.requestAsync(new RpcRequest() {
        this.uri("/test/single")
        this.content(i.toString + "-async")
      }, { response =>
        TestClientApp.synchronized{
          asyncResult.append(response.content())
        }
      })
    }
    for (i <- 0 until count) {
      client.requestOneWay(new RpcRequest() {
        this.uri("/test/single")
        this.content(i.toString + "-oneway")
      })
    }
    println("接受完毕开始检查")
    System.in.read()
    checkResult("async", count, asyncResult)
  }

  def ntcpMut(count: Int, client: RpcClient) = {

    /*
      异步请求
      */
    val asyncResult = ListBuffer[String]()
    client.requestAsync(new RpcRequest() {
      this.uri("/test/mul")
      this.content(count.toString)
    }, { response =>
      asyncResult.append(response.content())
      println(response.content())
    })

    println("接受完毕开始检查")
    System.in.read()
    asyncResult.foreach(println)
    //    checkResult("async",count,asyncResult)
  }

  def main(args: Array[String]): Unit = {

    val confLikeIoc = RpcClientConf(
      protocol = "NTCP",
      protocolVersion = "1.1",
      host = "127.0.0.1",
      port = 10030,
      works = 5, //客户端工作线程15
      log = "nrpc.log.ConsoleLog",
      channelProvider = "nrpc.client.channel.provider.DefaultChannelProvider", //使用连接池
      maxConnection = 5, //连接池最大5
      maxPending = Integer.MAX_VALUE, //
      heartTimeoutMilSec = 3000
    )
    val client: RpcClient = confLikeIoc.build();

    promiseProcess{
      ntcpSgl(5000, client) // 单步测试
      //ntcpMut(10, client) // 多步测试
    }{
      println(client.env.metric.RequestMetric.toString)
      client.close()
    }

  }
}
