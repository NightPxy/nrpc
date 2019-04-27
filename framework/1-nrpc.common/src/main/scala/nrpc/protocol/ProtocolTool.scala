package nrpc.protocol

import nrpc.rpc.{RpcRequest, RpcResponse, TransmissionMessage}

/**
  * 协议的工具类方法
  *    比如该协议要求的 请求/响应 的必传参数的验证等
  */
trait ProtocolTool {
  def validateForRequest(message:RpcRequest):Boolean
  def validateForResponse(message:RpcResponse):Boolean
  def isKeepAlive(message:TransmissionMessage):Boolean
}
