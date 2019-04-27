package nrpc.service

import nrpc.log.{ConsoleLog, ILog}
import nrpc.service.mapping.{MappingResolver, ServerMapping}

case class RpcServerConf(
                     val host: String,
                     val port: Int,
                     val log: String = "nrpc.log.ConsoleLog",
                     val services: String,
                     val notAcceptAlive: Boolean,
                     val heartTimeoutMilSec: Long,
                     val bossCount:Int = 1,
                     val workerCount:Int = 5) {

  def build() = {
    val mapping = new ServerMapping()
    services.split(",").foreach(service => {
      val serviceClazz = Class.forName(service)
      val serviceInstance = serviceClazz.newInstance()
      MappingResolver.resolve(mapping, serviceInstance, serviceClazz)
    })

    val env = new RpcServerEnv(
      name = s"$host:$port",
      log =  Class.forName(this.log).newInstance().asInstanceOf[ILog],
      mapping = mapping,
      conf = this
    )
    new RpcServer(env)
  }
}
