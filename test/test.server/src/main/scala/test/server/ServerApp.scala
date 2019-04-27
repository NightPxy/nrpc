package test.server

import nrpc.service.RpcServerConf
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
object ServerApp {
  def main(args: Array[String]): Unit = {
        val ioc =  RpcServerConf(
          host = "127.0.0.1",
          port = 10030,
          services = "test.server.controllers.TestController,test.server.controllers.SparkController",
          notAcceptAlive = false,
          heartTimeoutMilSec = 15000,
          bossCount = 1,
          workerCount = 5
         )

        val server = ioc.build()

        server.start()

  }
}
