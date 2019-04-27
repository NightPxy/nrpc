package nrpc.client.handler.send

import io.netty.channel.{Channel, ChannelFuture, ChannelFutureListener}
import io.netty.util.concurrent.{Future, FutureListener}
import nrpc.client.RpcClientEnv
import nrpc.client.callback.CallbackManager
import nrpc.client.channel.provider.ChannelProvider
import nrpc.exception.validate.InvalidateArgException
import nrpc.rpc.{RpcRequest, RpcResponse}

class SendHandler(request: RpcRequest,
                  responseAsyncHandler: RpcResponse => Unit,
                  env: RpcClientEnv,
                  callbackManager: CallbackManager,
                  channelProvider: ChannelProvider) {

  class SendOperationComplete extends ChannelFutureListener {
    override def operationComplete(future: ChannelFuture): Unit = {
      if (!future.isSuccess) {
        throw future.cause()
      }
    }
  }

  class ProvidedChannelOperationCompleted extends FutureListener[Channel] {
    override def operationComplete(future: Future[Channel]): Unit = {
      val channel = future.getNow
      if (future.isSuccess && channel != null && channel.isActive) {
        val f = channel.writeAndFlush(request)
        channelProvider.release(channel)
        f.addListener(new SendOperationComplete())
      }
    }
  }

  protected def preSend():this.type = {
    if (request == null) throw InvalidateArgException(s"[${env.name}] request can not be null")
    // 绑定请求协议
    request.protocol(env.currentProtocol.protocol)
    request.protocolVersion(env.currentProtocol.protocolVersion)
    // 请求最终验证
    env.currentProtocol.createTool.validateForRequest(request)
    this
  }

  protected def registerCallback(timeout:Long):this.type = {
    callbackManager.register(request,timeout, responseAsyncHandler)
    this
  }

  protected def sendAsync():this.type = {
    channelProvider.acquire().addListener(new ProvidedChannelOperationCompleted())
    this
  }

  protected def waitForCallback(timeoutMils: Long):RpcResponse = {
    callbackManager.waitForCallback(request,timeoutMils)
  }

  protected def requestMetric[T](send: => T): T = {
    val start = System.currentTimeMillis()
    val result = send
    env.metric.requestTime(System.currentTimeMillis() - start)
    return result
  }
}
