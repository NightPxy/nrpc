package nrpc.client.handler.send

import nrpc.client.RpcClientEnv
import nrpc.client.callback.CallbackManager
import nrpc.client.channel.provider.ChannelProvider
import nrpc.rpc.{RpcRequest, RpcResponse}

/**
  * 异步发送包装
  *    1.请求验证
  *    2.注册回调
  *    3.异步发送
  *
  * 去除:
  *    1.阻塞等待回调
  */
class AsyncSendHandler(request: RpcRequest,
                       responseProcessor: RpcResponse => Unit,
                       env: RpcClientEnv,
                       callbackManager: CallbackManager,
                       channelProvider: ChannelProvider)
  extends SendHandler(request, responseProcessor, env, callbackManager, channelProvider) {


  def send(timeoutMils: Long): Unit = {
    requestMetric {
      this
        .preSend()
        .registerCallback(timeoutMils)
        .sendAsync()
    }
  }
}

object AsyncSendHandler {
  def apply(request: RpcRequest,
            responseProcessor: RpcResponse => Unit,
            context: RpcClientEnv,
            callbackManager: CallbackManager,
            channelProvider: ChannelProvider): AsyncSendHandler = new AsyncSendHandler(request, responseProcessor, context, callbackManager, channelProvider)
}
