package nrpc.protocol

import nrpc.constants.value.ProtocolVersion.ProtocolVersion
import nrpc.constants.value.{ProtocolVersion, RpcProtocolType}
import nrpc.constants.value.RpcProtocolType.RpcProtocolType
import nrpc.exception.protocol.ProtocolException
import nrpc.rpc.TransmissionMessage

/**
  * 具体协议选择器
  */
object ProtocolFactory {

  def assertGetProtocol(transmissionMessage: TransmissionMessage): Protocol =
    assertGetProtocol(transmissionMessage.protocol, transmissionMessage.protocolVersion)


  def assertGetProtocol(protocol: RpcProtocolType, protocolVersion: ProtocolVersion): Protocol =
    assertGetProtocol(protocol.toString, protocolVersion.toString)

  def assertGetProtocol(protocol: String, protocolVersion: String): Protocol = {
    val protocolValue = tryGetProtocol(protocol, protocolVersion)
    if (protocolValue == null)
      throw ProtocolException(s"unknown protocol $protocol/$protocolVersion")
    else
      return protocolValue
  }


  def tryGetProtocol(protocol: String, protocolVersion: String): Protocol = {
    if (protocol == RpcProtocolType.NTCP.toString) {
      if (protocolVersion == ProtocolVersion.V_1_1.toString) {
        return Protocol.NTcp11
      }
    }

    return null
  }
}
