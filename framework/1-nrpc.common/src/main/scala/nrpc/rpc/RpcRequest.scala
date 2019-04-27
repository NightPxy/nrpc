package nrpc.rpc

import java.util.UUID

import nrpc.constants.HeaderKey
import nrpc.constants.value.KeepAlive.KeepAlive
import nrpc.constants.value.RequestMethod.RequestMethod
import nrpc.constants.value.RpcMessageType.RpcMessageType
import nrpc.constants.value.{KeepAlive, RpcMessageType, _}

class RpcRequest extends TransmissionMessage {

  this.direct(MessageDirect.REQUEST)
  this.uniqueRequestId(UUID.randomUUID().toString)

  def messageType() = RpcMessageType.withName {
    this.headerGetOrElse(HeaderKey.MESSAGE_TYPE, RpcMessageType.NORMAL.toString)
  }

  def messageType(messageType: RpcMessageType): this.type = this.headerSet(HeaderKey.MESSAGE_TYPE, messageType)


  def uri() = this.headerGetAsst(HeaderKey.URI)

  def uri(uri: String) = this.headerSet(HeaderKey.URI, uri)

  def method() = RequestMethod.withName {
    this.headerGetOrElse(HeaderKey.METHOD, RequestMethod.POST.toString)
  }

  def method(method: RequestMethod): this.type = this.headerSet(HeaderKey.METHOD, method)

  def keepAlive() = KeepAlive.withName {
    this.headerGetOrElse(HeaderKey.KeepAlive, KeepAlive.NONE.toString)
  }

  def keepAlive(keepAlive: KeepAlive): this.type = this.headerSet(HeaderKey.KeepAlive, keepAlive)

  def createResponse(): RpcResponse = RpcResponse()
    .protocol(this.protocol)
    .protocolVersion(this.protocolVersion)
    .uniqueRequestId(this.uniqueRequestId)
}

object RpcRequest {
  def apply(): RpcRequest = new RpcRequest()
}
