package test.client

import nrpc.client.{RpcClient, RpcClientConf}
import nrpc.rpc.RpcRequest
import nrpc.utils.All.promiseProcess
import test.client.TestClientApp.ntcpSgl


object SparkClientApp {
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
      isKeepHeart = true,
      heartTimeoutMilSec = 3000
    )
    val client: RpcClient = confLikeIoc.build();

    val code =
      """
        val rdd = spark.sparkContext.parallelize(Array("a,b,c,d", "a,b,c,d"))
 |      rdd.flatMap(x => x.split(",")).map(x => (x, 1)).reduceByKey(_ + _).collect()
      """.stripMargin

    promiseProcess{
      client.requestAsync(new RpcRequest() {
        this.uri("/spark/execute")
        this.content(code)
      }, { response =>
         println(response.content())
      })
      System.in.read()
    }{
      println(client.env.metric.RequestMetric)
      client.close()
    }
  }
}
