package nrpc.client.channel.provider

import io.netty.bootstrap.Bootstrap
import io.netty.channel.{Channel, ChannelHandler}
import io.netty.util.concurrent.Future
import nrpc.client.RpcClientEnv

abstract class ChannelProvider{

  def init(env: RpcClientEnv, client: Bootstrap, handler: =>ChannelHandler):this.type ;

  def acquire(): Future[Channel]

  def release(channel: Channel)

  def close()
}
