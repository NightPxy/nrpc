package nrpc.client.aop

import io.netty.channel.ChannelHandlerContext


class ClientAop {
  def channelActive(ctx: ChannelHandlerContext): Unit = {
  }

  def channelReadComplete(ctx: ChannelHandlerContext): Unit = {

  }

  def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {

  }
}
