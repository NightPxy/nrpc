package nrpc.utils.mq

import nrpc.rpc.RpcHubEnv


abstract class MemoryMQ[T](name:String,context: RpcHubEnv) {
  def consume(message:T);
  def produce(message:T)
  def start():this.type
}
