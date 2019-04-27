package nrpc.rpc.hub

import io.netty.channel.socket.SocketChannel
import io.netty.channel.{ChannelHandlerContext, ChannelInboundHandlerAdapter, ChannelInitializer}
import nrpc.rpc.RpcHubEnv

abstract class RpcHub(val env: RpcHubEnv)  extends RpcHubHandler {

  private val me = this

  protected def HandlerInitializer = new ChannelInitializer[SocketChannel] {
    override def initChannel(channel: SocketChannel): Unit = me.doInitChannel(channel)
  }

  protected def Handler = new ChannelInboundHandlerAdapter {
    override def userEventTriggered(ctx: ChannelHandlerContext, evt: scala.Any): Unit = {
      val isUsed =  me.userEventTriggered(ctx, evt, me.env, this)
      if(!isUsed) return super.userEventTriggered(ctx,evt)
    }

    override def channelRead(ctx: ChannelHandlerContext, msg: scala.Any): Unit =
      me.channelRead(ctx, msg, me.env, this)

    override def channelActive(ctx: ChannelHandlerContext): Unit =
      me.channelActive(ctx, me.env, this)

    override def channelInactive(ctx: ChannelHandlerContext): Unit =
      me.channelInactive(ctx, me.env, this)

    override def channelRegistered(ctx: ChannelHandlerContext): Unit =
      me.channelRegistered(ctx, me.env, this)

    override def channelUnregistered(ctx: ChannelHandlerContext): Unit =
      me.channelUnregistered(ctx, me.env, this)

    override def handlerAdded(ctx: ChannelHandlerContext): Unit =
      me.handlerAdded(ctx, me.env, this)

    override def handlerRemoved(ctx: ChannelHandlerContext): Unit =
      me.handlerRemoved(ctx, me.env, this)

    override def channelReadComplete(ctx: ChannelHandlerContext): Unit =
      me.channelReadComplete(ctx, me.env, this)

    override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit =
      me.exceptionCaught(ctx,cause, me.env, this)
  }
}
