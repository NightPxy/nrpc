package nrpc.client.channel.provider

import java.util.concurrent.TimeUnit

import io.netty.bootstrap.Bootstrap
import io.netty.channel.pool.{ChannelHealthChecker, ChannelPoolHandler, FixedChannelPool}
import io.netty.channel.{Channel, ChannelHandler}
import io.netty.handler.timeout.IdleStateHandler
import io.netty.util.concurrent.Future
import nrpc.client.RpcClientEnv
import nrpc.rpc.codec.{TransmissionDecoder, TransmissionEncoder}

class DefaultChannelProvider extends ChannelProvider {

  private var provider: FixedChannelPool = null

  override def init(env: RpcClientEnv, client: Bootstrap, handler: =>ChannelHandler): this.type = {
    this.provider = new FixedChannelPool(client, new ChannelPoolHandler {
      override def channelAcquired(ch: Channel): Unit = {

      }

      override def channelReleased(channel: Channel): Unit = {

      }

      override def channelCreated(channel: Channel): Unit = {
        if(env.conf.isKeepHeart){
          channel.pipeline().addLast(new IdleStateHandler(0, env.conf.heartTimeoutMilSec, 0, TimeUnit.MILLISECONDS))
        }
        channel.pipeline().addLast("decoder", new TransmissionDecoder(env))
        channel.pipeline().addLast("encoder", new TransmissionEncoder(env))
        channel.pipeline().addLast("handler", handler)
      }
    }, ChannelHealthChecker.ACTIVE, //使用活性检查
      null,-1,// AcquireTimeoutAction.NEW,5*1000, //15秒连接超时
      env.conf.maxConnection,  // 最大连接数
      env.conf.maxPending, // 最大挂起数
      true, // 放回检查活性
      true // 优先最近使用
    )
    this
  }

  override def acquire(): Future[Channel] = provider.acquire()

  override def release(channel: Channel): Unit = provider.release(channel)

  override def close(): Unit = provider.close()
}
