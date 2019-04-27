package nrpc.client


import io.netty.bootstrap.Bootstrap
import io.netty.channel._
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.timeout.{IdleState, IdleStateEvent}
import nrpc.client.callback.CallbackManager
import nrpc.client.handler.send.{AsyncSendHandler, OneWaySendHandler, SendHandler, SyncSendHandler}
import nrpc.constants.value.{RpcMessageType, RpcProtocolType}
import nrpc.rpc.hub.RpcHub
import nrpc.rpc.{RpcHubEnv, RpcRequest, RpcResponse}
import nrpc.utils.All.notCareException
import nrpc.utils.thread.RpcThreadFactory

class RpcClient(env: RpcClientEnv) extends RpcHub(env) {

  private val me = this
  protected val client = new Bootstrap()
  protected val worker = new NioEventLoopGroup(env.conf.works, new RpcThreadFactory(env, "client"))
  client.group(worker)
  client.channel(classOf[NioSocketChannel])
    .option(ChannelOption.TCP_NODELAY, Boolean.box(true)) //RPC偏向小包发送
    .option(ChannelOption.SO_KEEPALIVE, Boolean.box(false)) // 禁用TCP自带心跳,在应用层解决心跳问题
  client.remoteAddress(env.conf.host, env.conf.port)

  protected val channelProvider = env.channelProvider.init(env, client, Handler)

  protected val callbackManager = new CallbackManager(env)

  override protected def doChannelInactive(ctx: ChannelHandlerContext, hubContext: RpcHubEnv, handler: ChannelInboundHandlerAdapter): Unit = {
    env.log.debug {
      s"${env.name} connection channel inactive"
    }

  }

  private val HEART_PACKAGE = new RpcRequest().messageType(RpcMessageType.HEART_BEAT)

  override protected def doUserEventTriggered(ctx: ChannelHandlerContext, evt: Any,
                                              hubContext: RpcHubEnv, handler: ChannelInboundHandlerAdapter): Boolean = {
    if (evt.isInstanceOf[IdleStateEvent]) {
      val event = evt.asInstanceOf[IdleStateEvent];
      event.state() match {
        // 触发空闲监测的心跳包发送
        case IdleState.WRITER_IDLE => println("发送心跳"); this.requestOneWay(HEART_PACKAGE)
        case _ =>
      }
      return true
    }
    return false
  }

  override protected def doChannelRead(ctx: ChannelHandlerContext, msg: Any,
                                       hubContext: RpcHubEnv, handler: ChannelInboundHandlerAdapter): Unit = {
    if (msg == null) return
    if (msg.isInstanceOf[RpcResponse]) {
      // 正向 服务端->响应->客户端
      val response = msg.asInstanceOf[RpcResponse]

      env.log.debug {
        s"${env.name} received response $msg"
      }
      val requestId = response.uniqueRequestId
      val callback = this.callbackManager.get(requestId)
      if (callback != null) {
        callbackManager.received(response)
      }
    }
    else if (msg.isInstanceOf[RpcRequest]) {
      //反向  服务端->请求->客户端
      env.log.debug {
        s"${env.name} received request $msg"
      }
    }
  }

  def requestOneWay(request: RpcRequest): Unit = {
    OneWaySendHandler(request,env,callbackManager,channelProvider).send()
  }

  def requestSync(request: RpcRequest, timeoutMils: Long = 30000): RpcResponse = {
    SyncSendHandler(request,env,callbackManager,channelProvider).send(timeoutMils)
  }

  def requestAsync(request: RpcRequest, responseProcessor: RpcResponse => Unit, timeoutMils: Long = 30000): Unit = {
    AsyncSendHandler(request,responseProcessor,env,callbackManager,channelProvider).send(timeoutMils)
  }


  def close(): this.type = {
    env.log.info {
      s"Client[${env.name}] call close"
    }
    notCareException {
      channelProvider.close()
      env.log.info {
        s"Client[${env.name}] provider close"
      }
    }
    notCareException {
      worker.shutdownGracefully()
      env.log.info {
        s"Client[${env.name}] worker closed"
      }
    }
    this
  }
}