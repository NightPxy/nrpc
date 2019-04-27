package nrpc.client.handler.send

import nrpc.client.RpcClientEnv
import nrpc.client.callback.CallbackManager
import nrpc.client.channel.provider.ChannelProvider
import nrpc.rpc.{RpcRequest, RpcResponse}

/**
  *  单向发送包装
  *    1.请求验证
  *    2.异步发送
  *
  *  去除:
  *      1.不注册回调
  *      2.不阻塞等待回调
  */
class OneWaySendHandler(request: RpcRequest,
                        env: RpcClientEnv,
                        callbackManager: CallbackManager,
                        channelProvider: ChannelProvider)
  extends SendHandler(request, null, env, callbackManager, channelProvider){

  def send(): Unit = {
    requestMetric {
      this
        .preSend()
        .sendAsync()
    }
  }
}

object OneWaySendHandler 
{
  def apply(request: RpcRequest,
            context: RpcClientEnv,
            callbackManager: CallbackManager,
            channelProvider: ChannelProvider): OneWaySendHandler = new OneWaySendHandler(request, context, callbackManager, channelProvider)
}
