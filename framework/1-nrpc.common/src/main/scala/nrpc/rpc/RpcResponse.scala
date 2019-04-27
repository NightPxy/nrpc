package nrpc.rpc

import nrpc.constants.HeaderKey
import nrpc.constants.value.ProtocolVersion.ProtocolVersion
import nrpc.constants.value.RpcProtocolType.RpcProtocolType
import nrpc.constants.value._
import nrpc.constants.value.RpcResponseStatus.ResponseStatus

class RpcResponse extends TransmissionMessage {

  this.direct(MessageDirect.RESPONSE)

  def messageType() = RpcMessageType.withName{
    this.headerGetOrElse(HeaderKey.MESSAGE_TYPE, RpcMessageType.NORMAL.toString)
  }

  def messageType(messageType: String): this.type = this.headerSet(HeaderKey.MESSAGE_TYPE, messageType)

  def status() = RpcResponseStatus.withName {
    this.headerGetAsst(HeaderKey.STATUS)
  }

  def status(status: ResponseStatus) = this.headerSet(HeaderKey.STATUS, status)
}

object RpcResponse {
  def apply(): RpcResponse = new RpcResponse()
}
