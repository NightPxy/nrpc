package nrpc.rpc.hub

import io.netty.channel.socket.SocketChannel
import io.netty.channel.{ChannelHandlerContext, ChannelInboundHandlerAdapter}
import nrpc.rpc.RpcHubEnv
import nrpc.rpc.hub.handler.{RpcHubChannelHandler, RpcHubHandlerHanlder}


trait RpcHubHandler extends RpcHubHandlerHanlder with RpcHubChannelHandler{

  def doInitChannel(channel: SocketChannel): Unit = {

  }

  final def userEventTriggered(ctx: ChannelHandlerContext, evt: scala.Any,
                               hubContext: RpcHubEnv, handler: ChannelInboundHandlerAdapter): Boolean = {
    this.doUserEventTriggered(ctx, evt, hubContext, handler)
  }

  protected def doUserEventTriggered(ctx: ChannelHandlerContext, evt: scala.Any,
                                     hubContext: RpcHubEnv, handler: ChannelInboundHandlerAdapter): Boolean = {
    return false
  }

   final def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable,
                             hubContext: RpcHubEnv, handler: ChannelInboundHandlerAdapter): Unit = {
     hubContext.log.error{
       s"[${hubContext.name}] exception ${cause.getMessage}"
     }
     cause.printStackTrace()
     doExceptionCaught(ctx,cause,hubContext,handler)
  }

  protected def doExceptionCaught(ctx: ChannelHandlerContext, cause: Throwable,
                                  hubContext: RpcHubEnv, handler: ChannelInboundHandlerAdapter): Unit = {

  }

}
