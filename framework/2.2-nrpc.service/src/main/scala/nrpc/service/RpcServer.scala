package nrpc.service

import java.util.concurrent.TimeUnit

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel._
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.timeout.{IdleState, IdleStateEvent, IdleStateHandler}
import nrpc.constants.value._
import nrpc.rpc.codec.{TransmissionDecoder, TransmissionEncoder}
import nrpc.rpc.hub.RpcHub
import nrpc.rpc.{RpcHubEnv, RpcRequest, RpcResponse}
import nrpc.utils.All.notCareException
import nrpc.utils.thread.RpcThreadFactory

/**
  * RPC 服务端
  * 自适应长短连接
  *       1.如果配置要求不接受长连接,则保证每次响应完成后立即断开
  *       2.长连接请求响应后不断开,短连接请求将会响应完成后立即断开
  *       2.心跳超时后也会立即断开
  */
class RpcServer(env: RpcServerEnv) extends RpcHub(env) {

  private def closeAtOnce(ctx: ChannelHandlerContext, response: AnyRef) = {
    ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE)
  }

  override def doInitChannel(channel: SocketChannel): Unit = {
    channel.pipeline().addLast(new IdleStateHandler(env.conf.heartTimeoutMilSec, 0, 0, TimeUnit.MILLISECONDS))
    channel.pipeline().addLast("decoder", new TransmissionDecoder(env))
    channel.pipeline().addLast("encoder", new TransmissionEncoder(env))
    channel.pipeline().addLast("handler", Handler)
  }

  override protected def doUserEventTriggered(ctx: ChannelHandlerContext, evt: Any,
                                              hubContext: RpcHubEnv, handler: ChannelInboundHandlerAdapter): Boolean = {
    if (evt.isInstanceOf[IdleStateEvent]) {
      val event = evt.asInstanceOf[IdleStateEvent];
      event.state() match {
        // 触发空闲监测的连接关闭
        case IdleState.READER_IDLE => println("强制关闭"); ctx.channel().close();
        case _ =>
      }
      return true
    }
    return false
  }

  private def processRequest(ctx: ChannelHandlerContext, request: RpcRequest,
                             hubContext: RpcHubEnv, handler: ChannelInboundHandlerAdapter): Unit = {
    //心跳信息不处理
    if (request.messageType == RpcMessageType.HEART_BEAT) {
      return
    }

    //超时信息直接断开
    if (request.messageType == RpcMessageType.TIME_OUT) {
      ctx.channel().closeFuture().addListener(ChannelFutureListener.CLOSE)
      return
    }

    val rpcResponse = request.createResponse()

    //调用
    val mapping = env.mapping.getMapping(request.uri)
    if (mapping == null) return closeAtOnce(ctx, rpcResponse.status(RpcResponseStatus.NOT_FOUND).content("404"))
    val result = mapping.invoke(request, RpcContext(request, ctx))


    // 响应
    val future = ctx.writeAndFlush {
      val response = if (result == null) {
        rpcResponse.status(RpcResponseStatus.OK)
      }
      else if (result.isInstanceOf[RpcResponse]) {
        result.asInstanceOf[RpcResponse]
      }
      else {
        rpcResponse.content(result.toString).status(RpcResponseStatus.OK)
      }
      response
    }

    //收尾
    if (env.conf.notAcceptAlive) {
      env.log.debug {
        s"[${env.name} not accept alive , auto closing ]"
      }
      future.addListener(ChannelFutureListener.CLOSE)
    }
  }

  private def processResponse(ctx: ChannelHandlerContext, request: RpcResponse,
                              hubContext: RpcHubEnv, handler: ChannelInboundHandlerAdapter): Unit = {
    //双工底子先打到这里 如何利用再仔细想想
  }

  override protected def doChannelRead(ctx: ChannelHandlerContext, msg: Any,
                                       hubContext: RpcHubEnv, handler: ChannelInboundHandlerAdapter): Unit = {
    if (msg == null) return

    env.log.debug {
      s"[${env.name}] received ${msg.toString}"
    }

    if (msg.isInstanceOf[RpcRequest]) {
      //正向: 客户端->请求->服务端
      this.processRequest(ctx, msg.asInstanceOf[RpcRequest], hubContext, handler)
    }
    else if (msg.isInstanceOf[RpcResponse]) {
      //反向: 客户端->响应->服务端
      this.processResponse(ctx, msg.asInstanceOf[RpcResponse], hubContext, handler)
    }
  }

  private val boss = new NioEventLoopGroup(env.conf.bossCount, new RpcThreadFactory(env, "boss"))
  private val worker = new NioEventLoopGroup(env.conf.workerCount, new RpcThreadFactory(env, "worker"))
  private val server = new ServerBootstrap()
    .group(boss, worker)
    .channel(classOf[NioServerSocketChannel])
    .childHandler(HandlerInitializer)
    .option(ChannelOption.SO_REUSEADDR, Boolean.box(true)) // 服务端端口复用
    .option(ChannelOption.SO_BACKLOG, new Integer(128)) //临时存放请求队列上限
    .childOption(ChannelOption.TCP_NODELAY, Boolean.box(true)) //RPC偏向小包发送
    .childOption(ChannelOption.SO_KEEPALIVE, Boolean.box(false)) // 禁用TCP自带心跳,在应用层解决心跳问题

  private var channel: ChannelFuture = null

  def close(): this.type = {
    notCareException {
      if (channel != null) {
        channel.channel().closeFuture().sync()
      }
    }
    notCareException {
      boss.shutdownGracefully()
      env.log.info {
        s"${env.conf.host}:${env.conf.port} boss closed"
      }
    }
    notCareException {
      worker.shutdownGracefully()
      env.log.info {
        s"${env.conf.host}:${env.conf.port} worker closed"
      }
    }
    this
  }

  def start(): this.type = {
    channel = server.bind(env.conf.host, env.conf.port).sync()
    env.log.info {
      s"${env.conf.host}:${env.conf.port} started"
    }
    this
  }
}
