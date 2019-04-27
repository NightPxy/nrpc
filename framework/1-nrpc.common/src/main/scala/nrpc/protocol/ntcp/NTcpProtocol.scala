package nrpc.protocol.ntcp

import java.net.ProtocolException
import java.util

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import nrpc.constants.HeaderKey
import nrpc.constants.value.ProtocolVersion.ProtocolVersion
import nrpc.constants.value.{KeepAlive, MessageDirect, RpcProtocolType}
import nrpc.rpc.{RpcRequest, RpcResponse, TransmissionMessage}
import nrpc.protocol.{Protocol, ProtocolCodec, ProtocolTool}
import nrpc.utils.All._

import scala.collection.mutable

class NTcpProtocol(protocolVersion:ProtocolVersion) extends Protocol(RpcProtocolType.NTCP,protocolVersion) with ProtocolCodec with ProtocolTool {
  private val MESSAGE_HEADER_SIZE = 2

  override def encode(ctx: ChannelHandlerContext, message: TransmissionMessage, byteBuf: ByteBuf): Unit = {
    val header = message.getHeader().toBytes()
    val headerSize = header.length;
    val body = message.content().toBytes()
    val bodySize = body.length

    //段零 协议头
    //byteBuf.writeBytes(protocolConent)

    //段一 header 消息全长但不含长度位本身 定长4
    val messageSize = MESSAGE_HEADER_SIZE + headerSize + bodySize
    byteBuf.writeInt(messageSize)

    //段二 消息头长度(定长4,但不含长度本身) + 消息头内容
    byteBuf.writeShort(headerSize).writeBytes(header)

    //段三 消息体
    byteBuf.writeBytes(body)
  }

  override def decode(ctx: ChannelHandlerContext, in: ByteBuf):TransmissionMessage  = {

    //保证能读出消息总长
    if (in.readableBytes() < 4) return null

    //消息总长
    val messageSize = in.readInt()
    if (in.readableBytes() < messageSize) {
      return null
    }

    //消息头 长度
    val headerSize = in.readShort()

    val headerBytes = new Array[Byte](headerSize)
    val header = in.readBytes(headerBytes)

    //消息体
    val bodySize = messageSize - MESSAGE_HEADER_SIZE - headerSize
    val bodyBytes = new Array[Byte](bodySize)
    val body = in.readBytes(bodyBytes)

    val hashMap = headerBytes.bytesTo[mutable.HashMap[String, String]]().getOrElse(null)

    if (hashMap == null) throw new ProtocolException(s"unresolved header")

    val direct = hashMap.get(HeaderKey.DIRECT.toString).getOrElse(null)
    if (direct == null)  throw new ProtocolException(s"unresolved header-DIRECT")

    val transmissionMessage = if (direct == MessageDirect.REQUEST.toString) {
      RpcRequest()
    }
    else if (direct == MessageDirect.RESPONSE.toString) {
      RpcResponse()
    }
    else {
       throw new ProtocolException(s"unresolved header-DIRECT: $direct")
    }

    hashMap.foreach(x => {
      transmissionMessage.headerSet(x._1, x._2)
    })
    transmissionMessage.content(bodyBytes.bytesTo[String]().getOrElse(""))

    return transmissionMessage
  }

  override def validateForRequest(message: RpcRequest): Boolean = {
    assertTrue(message != null, "RpcRequest: can not be null")
    assertTrue(message.getHeader() != null, "RpcRequest:Header can not be null")
    assertTrue(message.content() != null, "RpcRequest:body can not be null")
    assertTrue(message.protocol() != null, "RpcRequest:protocol can not be empty")
    assertTrue(message.protocolVersion() != null, "RpcRequest:protocolVersion can not be empty")
    assertStringNotEmpty(message.uniqueRequestId,"RpcRequest:UniqueRequestId can not be empty")
    assertProtocolAcceptMessage(message)
    return true
  }

  override def validateForResponse(message: RpcResponse): Boolean = {
    assertTrue(message != null, "RpcResponse: can not be null")
    assertTrue(message.getHeader() != null, "RpcResponse:Header can not be null")
    assertTrue(message.content() != null, "RpcResponse:body can not be null")
    assertTrue(message.protocol() != null, "RpcResponse:protocol can not be empty")
    assertTrue(message.protocolVersion() != null, "RpcResponse:protocolVersion can not be empty")
    assertTrue(message.status != null,"RpcResponse:Status can not be empty")
    assertStringNotEmpty(message.uniqueRequestId,"RpcResponse:UniqueRequestId can not be empty")
    assertProtocolAcceptMessage(message)
    return true
  }

  override def isKeepAlive(message: TransmissionMessage): Boolean = {
    assertTrue(message != null, "TransmissionMessage: can not be null")
    assertProtocolAcceptMessage(message)

    if (message.headerGet(HeaderKey.KeepAlive).getOrElse("") == KeepAlive.KEEP_ALIVE.toString) {
      return true
    }
    return false
  }
}
