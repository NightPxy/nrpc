package nrpc.client

import nrpc.client.channel.provider.ChannelProvider
import nrpc.constants.value.KeepAlive
import nrpc.log.ILog
import nrpc.protocol.ProtocolFactory

/**
  * 客户端配置信息
  * 配置信息视为来自Ioc
  */
case class RpcClientConf(val protocol: String,
                         val protocolVersion: String,
                         val host: String,
                         val port: Int,
                         val works: Int,
                         val channelProvider: String = "nrpc.client.channel.provider.DefaultChannelProvider",
                         val log: String = "nrpc.log.ConsoleLog",
                         val maxConnection: Int,
                         val maxPending: Int,
                         val isKeepHeart:Boolean = false,
                         val heartTimeoutMilSec: Long
                        ) {

  val clientName = s"client:${this.host}:${this.port}"

  def build(): RpcClient = {
    val env = new RpcClientEnv(
      name = s"${this.host}:${this.port}",
      currentProtocol = ProtocolFactory.assertGetProtocol(protocol, protocolVersion),
      log = Class.forName(this.log).newInstance().asInstanceOf[ILog],
      alive = if (heartTimeoutMilSec <= 0) KeepAlive.NONE else KeepAlive.KEEP_ALIVE,
      channelProvider = Class.forName(this.channelProvider).newInstance().asInstanceOf[ChannelProvider],
      conf = this
    )
    return new RpcClient(env)
  }
}
