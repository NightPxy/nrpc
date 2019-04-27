package nrpc.client.handler.send

import io.netty.channel.Channel
import io.netty.util.concurrent.Future
import nrpc.client.RpcClientEnv
import nrpc.client.callback.CallbackManager
import nrpc.client.channel.provider.ChannelProvider
import nrpc.rpc.{RpcRequest, RpcResponse}

/**
  *  同步发送包装
  *    1.请求验证
  *    2.注册回调
  *    3.异步发送
  *    4.阻塞等待回调
  */
class SyncSendHandler(request: RpcRequest,
                      env: RpcClientEnv,
                      callbackManager: CallbackManager,
                      channelProvider: ChannelProvider)
  extends SendHandler(request, null, env, callbackManager, channelProvider) {

  def send(timeoutMils: Long): RpcResponse = {
    requestMetric {
      this
        .preSend()
        .registerCallback(timeoutMils)
        .sendAsync()
        .waitForCallback(timeoutMils)
    }
  }
}

object SyncSendHandler 
{
  def apply(request: RpcRequest,
            context: RpcClientEnv,
            callbackManager: CallbackManager,
            channelProvider: ChannelProvider): SyncSendHandler = new SyncSendHandler(request, context, callbackManager, channelProvider)
}
