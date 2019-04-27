package nrpc.client.callback

import java.util
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.LongAdder

import nrpc.client.RpcClientEnv
import nrpc.rpc.{RpcRequest, RpcResponse}
import nrpc.utils.All._
import nrpc.utils.mq.disruptor.DisruptorMemoryMQMessage
import nrpc.utils.mq.poll.PollMemoryMQ

class CallbackManager(env: RpcClientEnv) {
  private val callbackPool = new ConcurrentHashMap[String, Callback]()
  private val callbackTimeline = new util.TreeMap[Long, util.LinkedList[Callback]]();

  private val messageQueue = new PollMemoryMQ[CallbackMessage]("callback-timeout", env) {
    override def consume(message: CallbackMessage): Unit = {
      //加入分组
      if (message != null) {
        val callback = callbackPool.get(message.rid);
        if (callback != null) {
          val callbackTimeout = callback.getTimeout / 1000 //分组以秒为单位
          var callbackGroup = callbackTimeline.get(callbackTimeout)
          if (callbackGroup == null) {
            callbackGroup = new util.LinkedList[Callback]()
            callbackTimeline.put(callbackTimeout,callbackGroup)
          }
          callbackGroup.offer(callback)
        }
      }

      //扫描过期分组
      val timeoutLimit = System.currentTimeMillis() / 1000;
      var currentTimeoutGroup = callbackTimeline.lowerEntry(timeoutLimit)
      while (currentTimeoutGroup != null) {
        val timeoutGroup = currentTimeoutGroup.getValue
        //扫描分组内所有Callback,全部移除
        var timeoutCallback = timeoutGroup.poll()

        while (timeoutCallback != null) {
          callbackPool.remove(timeoutCallback.getRequest.uniqueRequestId)
          timeoutCallback = timeoutGroup.poll()
        }
        //移除整个过期分组
        callbackTimeline.remove(currentTimeoutGroup.getKey)
        //继续下个过期分组扫描
        currentTimeoutGroup = callbackTimeline.lowerEntry(System.currentTimeMillis)
      }
    }
  }.start()

  case class CallbackMessage(var rid: String)


  def get(requestId: String): Callback = {
    this.callbackPool.get(requestId)
  }

  def register(request: RpcRequest, timeout: Long, responseProcessor: RpcResponse => Unit) = {
    val callback = Callback(request, System.currentTimeMillis() + timeout, responseProcessor)
    if (null == this.callbackPool.putIfAbsent(request.uniqueRequestId, callback)) {
      messageQueue.produce(CallbackMessage(request.uniqueRequestId))
    }
  }

  def received(response: RpcResponse): Unit = {

    val rid = response.uniqueRequestId()
    val callback = this.callbackPool.get(rid)
    if (callback == null) {
      env.log.waring{
        s"[${env.name}] response callback not found:$response"
      }
      return;
    }

    if (callback.isAsync) {
      //异步:直接运行用户回调函数
      callback.responseProcessor(response)
    }
    else {
      //同步:响应写入回调上下文中,通知解除用户线程阻塞
      val ifPutSuccess = callback.putIfAbsent(response)
      if (ifPutSuccess) {
        callback.notifyCallback()
      }
    }
  }

  def waitForCallback(request: RpcRequest, timeoutMils: Long): RpcResponse = {
    val rid = request.uniqueRequestId
    val callback = this.callbackPool.get(rid)
    if (callback == null) {
      return null
    }
    val response = callback.awaitCallback(timeoutMils)
    return response
  }

}
