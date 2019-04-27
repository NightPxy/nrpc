package nrpc.protocol

import nrpc.constants.HeaderKey
import nrpc.constants.value.ProtocolVersion.ProtocolVersion
import nrpc.constants.value.{ProtocolVersion, RpcProtocolType}
import nrpc.constants.value.RpcProtocolType.RpcProtocolType
import nrpc.exception.validate.InvalidateArgException
import nrpc.rpc.TransmissionMessage
import nrpc.protocol.ntcp.NTcpProtocol


abstract class Protocol(val protocol:RpcProtocolType,val protocolVersion:ProtocolVersion)
  extends ProtocolAbstractFactory with ProtocolCodec with ProtocolTool {

  protected def assertProtocolAcceptMessage(message:TransmissionMessage):Boolean = {
    if(protocol !=  message.protocol)
    {
       throw new InvalidateArgException(s"Message:[$message] not accept protocol:$protocol")
    }
    return true
  }



  override def createCodec(): ProtocolCodec = this

  override def createTool(): ProtocolTool = this
}

object Protocol {
  object NTcp11 extends NTcpProtocol(ProtocolVersion.V_1_1){ }
}
