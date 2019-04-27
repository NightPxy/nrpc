package nrpc.rpc.hub.handler

import io.netty.channel.{ChannelHandlerContext, ChannelInboundHandlerAdapter}
import nrpc.rpc.RpcHubEnv


trait RpcHubChannelHandler {
  final def channelRegistered(ctx: ChannelHandlerContext,
                              hubContext: RpcHubEnv, handler:ChannelInboundHandlerAdapter): Unit = {
    this.doChannelRegistered(ctx,hubContext,handler)
  }

  protected def doChannelRegistered(ctx: ChannelHandlerContext,
                                    hubContext: RpcHubEnv, handler:ChannelInboundHandlerAdapter): Unit = {

  }

  final def channelUnregistered(ctx: ChannelHandlerContext,
                                hubContext: RpcHubEnv, handler:ChannelInboundHandlerAdapter): Unit = {
    this.doChannelUnregistered(ctx,hubContext,handler)
  }

  protected def doChannelUnregistered(ctx: ChannelHandlerContext,
                                      hubContext: RpcHubEnv, handler:ChannelInboundHandlerAdapter): Unit = {

  }

  final def handlerAdded(ctx: ChannelHandlerContext,
                         hubContext: RpcHubEnv, handler:ChannelInboundHandlerAdapter): Unit = {
    this.doHandlerAdded(ctx,hubContext,handler)
  }

  protected def doHandlerAdded(ctx: ChannelHandlerContext,
                               hubContext: RpcHubEnv, handler:ChannelInboundHandlerAdapter): Unit = {

  }

  final def handlerRemoved(ctx: ChannelHandlerContext,
                           hubContext: RpcHubEnv, handler:ChannelInboundHandlerAdapter): Unit = {
    this.handlerRemoved(ctx,hubContext,handler)
  }

  protected def doHandlerRemoved(ctx: ChannelHandlerContext,
                                 hubContext: RpcHubEnv, handler:ChannelInboundHandlerAdapter): Unit = {

  }
}
