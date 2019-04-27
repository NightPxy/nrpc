package nrpc.rpc.codec

import io.netty.buffer.{ByteBuf, ByteBufUtil}
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import io.netty.handler.codec.http.HttpRequestEncoder
import io.netty.util.AsciiString
import nrpc.rpc.{RpcHubEnv, TransmissionMessage}
import nrpc.protocol.ProtocolFactory
import nrpc.utils.All._
/**
  * 传输编码器(参考Netty-Http实现)
  * 双工适应: [RpcRequest,RpcResponse] => TransmissionMessage
  */
class TransmissionEncoder(context: RpcHubEnv) extends MessageToByteEncoder[TransmissionMessage] {
  private val MESSAGE_HEADER_SIZE = 2

  override def encode(ctx: ChannelHandlerContext, message: TransmissionMessage, byteBuf: ByteBuf): Unit = {
    val protocol = ProtocolFactory.assertGetProtocol(message)
    val protocolConent = AsciiString.cached(s"${message.protocol}/${message.protocolVersion()}\r\n")
    ByteBufUtil.copy(protocolConent, byteBuf)
    protocol.createCodec.encode(ctx, message, byteBuf)



//    val header = message.getHeader().toBytes()
//    val headerSize = header.length;
//    val body = message.content().toBytes()
//    val bodySize = body.length
//
//    //段零 协议头
//    //byteBuf.writeBytes(protocolConent)
//
//    //段一 header 消息全长但不含长度位本身 定长4
//    val messageSize = MESSAGE_HEADER_SIZE + headerSize + bodySize
//    byteBuf.writeInt(messageSize)
//
//    //段二 消息头长度(定长4,但不含长度本身) + 消息头内容
//    byteBuf.writeShort(headerSize).writeBytes(header)
//
//    //段三 消息体
//    byteBuf.writeBytes(body)
  }
}
//class TransmissionEncoder(context: RpcHubContext) extends TransmissionMessageEncoder[TransmissionMessage] {
//
//  private val SLASH:Char = '/'
//  private val QUESTION_MARK:Char= '?'
//  private val SLASH_AND_SPACE_SHORT = (SLASH << 8) | SP
//  private val SPACE_SLASH_AND_SPACE_MEDIUM = (SP << 16) | SLASH_AND_SPACE_SHORT
//
//
//  override protected def encodeInitialLine(buf: ByteBuf, message: TransmissionMessage): Unit = {
//    println("encodeInitialLine")
//    if (message == null) return
//    if (message.isInstanceOf[RpcRequest]) {
//      println("1")
//      return doEncodeInitialLineByRequest(buf, message.asInstanceOf[RpcRequest])
//    }
//    else if (message.isInstanceOf[RpcResponse]) {
//      println("2")
//      return doEncodeInitialLineByResponse(buf, message.asInstanceOf[RpcResponse])
//    }
//  }
//
//  private def doEncodeInitialLineByRequest(buf: ByteBuf, message: RpcRequest): Unit = {
//
//    /*
//      METHOD URI PROTOCOL/PROTOCOL_VERSION
//     */
//
//    val method = AsciiString.cached(message.method.toString)
//    ByteBufUtil.copy(method, buf)
//
//    val uri = message.headerGet(HeaderKey.URI)
//
//    if (uri.isEmpty) {
//      ByteBufUtil.writeMediumBE(buf, SPACE_SLASH_AND_SPACE_MEDIUM)
//    }
//    else {
//      var uriCharSequence = uri
//      var needSlash = false
//      var start = uri.indexOf("://")
//      if (start != -1 && uri.charAt(0) != SLASH) {
//        start += 3
//        // Correctly handle query params.
//        // See https://github.com/netty/netty/issues/2732
//        val index = uri.indexOf(QUESTION_MARK, start)
//        if (index == -1) if (uri.lastIndexOf(SLASH) < start) needSlash = true
//        else if (uri.lastIndexOf(SLASH, index) < start) uriCharSequence = new StringBuilder().append(uri).insert(index, SLASH).toString()
//      }
//      buf.writeByte(SP).writeCharSequence(uriCharSequence, CharsetUtil.UTF_8)
//      if (needSlash) { // write "/ " after uri
//        ByteBufUtil.writeShortBE(buf, SLASH_AND_SPACE_SHORT)
//      }
//      else buf.writeByte(SP)
//    }
//
//    val protocolVersion = message.headerGetAsst(HeaderKey.PROTOCOL_VERSION)
//
//    buf.writeCharSequence(protocolVersion, CharsetUtil.US_ASCII);
//
//    ByteBufUtil.writeShortBE(buf, TransmissionMessageEncoder.CRLF_SHORT)
//  }
//
//  private def doEncodeInitialLineByResponse(buf: ByteBuf, message: RpcResponse): Unit = {
//
//    val protocolFull = AsciiString.cached(s"${message.protocol}/${message.protocolVersion()}" )
//    ByteBufUtil.copy(protocolFull, buf)
//    buf.writeByte(SP)
//
//    val status = message.headerGetAsstByAsciiString(HeaderKey.STATUS)
//    ByteBufUtil.copy(status, buf)
//    ByteBufUtil.writeShortBE(buf, TransmissionMessageEncoder.CRLF_SHORT)
//  }
//}

object TransmissionEncoder {
  def apply(context: RpcHubEnv): TransmissionEncoder = new TransmissionEncoder(context)
}
