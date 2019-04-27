package nrpc.rpc.hub.handler

import io.netty.channel.{ChannelHandlerContext, ChannelInboundHandlerAdapter}
import nrpc.rpc.RpcHubEnv


trait RpcHubHandlerHanlder {

  final def channelActive(ctx: ChannelHandlerContext,
                          hubContext: RpcHubEnv, handler: ChannelInboundHandlerAdapter): Unit = {
    this.doChannelActive(ctx, hubContext, handler)
  }

  protected def doChannelActive(ctx: ChannelHandlerContext,
                                hubContext: RpcHubEnv, handler: ChannelInboundHandlerAdapter): Unit = {

  }

  final def channelInactive(ctx: ChannelHandlerContext,
                            hubContext: RpcHubEnv, handler: ChannelInboundHandlerAdapter): Unit = {
    this.doChannelInactive(ctx, hubContext, handler)
  }

  protected def doChannelInactive(ctx: ChannelHandlerContext,
                                  hubContext: RpcHubEnv, handler: ChannelInboundHandlerAdapter): Unit = {

  }

  final def channelRead(ctx: ChannelHandlerContext, msg: scala.Any,
                        hubContext: RpcHubEnv, handler: ChannelInboundHandlerAdapter): Unit = {
    this.doChannelRead(ctx, msg, hubContext, handler)
  }

  protected def doChannelRead(ctx: ChannelHandlerContext, msg: scala.Any,
                              hubContext: RpcHubEnv, handler: ChannelInboundHandlerAdapter): Unit = {

  }

  final def channelReadComplete(ctx: ChannelHandlerContext,
                                hubContext: RpcHubEnv, handler: ChannelInboundHandlerAdapter): Unit = {
    this.doChannelReadComplete(ctx, hubContext, handler)
  }

  protected def doChannelReadComplete(ctx: ChannelHandlerContext,
                                      hubContext: RpcHubEnv, handler: ChannelInboundHandlerAdapter): Unit = {

  }


}
