package nrpc.service

import io.netty.channel.{ChannelFuture, ChannelFutureListener, ChannelHandlerContext}
import nrpc.rpc.{RpcRequest, RpcResponse}

/**
  * Rpc请求的连接上下文
  *   用于多段响应中
  */
case class RpcContext(val request: RpcRequest, ctx: ChannelHandlerContext) {

  case class Callback(future: ChannelFuture) {
    def close() = {
      this.future.channel().close()
    }
  }

  def sendResponse(content: String,callback: Callback => Unit = null): Unit = {
    val fur = ctx.writeAndFlush(request.createResponse().content(content))
    if (callback != null) {
      fur.addListener(new ChannelFutureListener {
        override def operationComplete(future: ChannelFuture): Unit = {
          callback {
            Callback(future)
          }
        }
      })
    }
  }
}
