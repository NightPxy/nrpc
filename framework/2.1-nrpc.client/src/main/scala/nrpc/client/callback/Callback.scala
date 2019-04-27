package nrpc.client.callback

import nrpc.callback.ConcurrentCallback
import nrpc.rpc.{RpcRequest, RpcResponse}

case class Callback(request:RpcRequest,timeout:Long, var responseProcessor : RpcResponse => Unit = null)
  extends ConcurrentCallback(request,timeout){

  def isAsync = this.responseProcessor != null

  val startTime = System.currentTimeMillis()

}
