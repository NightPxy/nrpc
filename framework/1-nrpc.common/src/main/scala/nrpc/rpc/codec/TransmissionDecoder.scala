package nrpc.rpc.codec

import java.util
import java.util.concurrent.atomic.LongAdder

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import io.netty.handler.codec.http.{HttpObjectDecoder, HttpRequestDecoder}
import io.netty.util.internal.AppendableCharSequence
import nrpc.constants.HeaderKey
import nrpc.constants.value.MessageDirect
import nrpc.rpc.{RpcHubEnv, RpcRequest, RpcResponse, TransmissionMessage}
import nrpc.protocol.{Protocol, ProtocolFactory}
import nrpc.utils.All._

import scala.collection.mutable

class TransmissionDecoder(context: RpcHubEnv) extends ByteToMessageDecoder {
  private val MESSAGE_HEADER_SIZE = 2
  val protocolLineParser = new ProtocolLineParser(new AppendableCharSequence(64), 256)

  override def decode(ctx: ChannelHandlerContext, in: ByteBuf, out: util.List[AnyRef]): Unit = {
    if (null == in) return

    in.markReaderIndex()

    protocolLineParser.reset()
    val line = protocolLineParser.parse(in)
    if (line == null) {
      return
    }
    val lineString = line.toString
    val protocolLine = assertGet(lineString.split("/"))(x => x.length == 2)

    val protocol: Protocol = ProtocolFactory.assertGetProtocol(protocolLine(0), protocolLine(1))

    val transmissionMessage = protocol.createCodec.decode(ctx, in)
    if (transmissionMessage == null) {
      in.resetReaderIndex()
    }
    else {
      out.add(transmissionMessage)
    }


    //    if (null == in) return
    //    in.markReaderIndex()
    //
    //    //保证能读出消息总长
    //    if(in.readableBytes() < 4) return
    //
    //    //消息总长
    //    val messageSize = in.readInt()
    //    if(in.readableBytes() < messageSize)  {
    //      in.resetReaderIndex()
    //      return
    //    }
    //
    //    //消息头 长度
    //    val headerSize = in.readShort()
    //
    //    val headerBytes = new Array[Byte](headerSize)
    //    val header = in.readBytes(headerBytes)
    //
    //    //消息体
    //    val bodySize = messageSize - MESSAGE_HEADER_SIZE - headerSize
    //    val bodyBytes = new Array[Byte](bodySize)
    //    val body = in.readBytes(bodyBytes)
    //
    //    val hashMap = headerBytes.bytesTo[mutable.HashMap[String, String]]().getOrElse(null)
    //
    //    if(hashMap == null) return
    //
    //    val direct = hashMap.get(HeaderKey.DIRECT.toString).getOrElse(null)
    //    if(direct == null) return
    //
    //    val transmissionMessage = if (direct == MessageDirect.REQUEST.toString) {
    //      RpcRequest()
    //    }
    //    else if (direct == MessageDirect.RESPONSE.toString) {
    //      RpcResponse()
    //    }
    //    else {
    //      context.log.error{
    //        s"[${context.name}] received unknown message direct:[$direct]"
    //      }
    //      return
    //    }
    //
    //    hashMap.foreach(x => {
    //      transmissionMessage.headerSet(x._1, x._2)
    //    })
    //    transmissionMessage.content(bodyBytes.bytesTo[String]().getOrElse(""))
    //
    //    context.log.debug{
    //      s"[${context.name}] decode message [$transmissionMessage]"
    //    }
    //
    //    out.add(transmissionMessage)

  }
}

//class TransmissionDecoder(context: RpcHubContext)
//  extends TransmissionMessageDecoder {
//
//
//  override protected def isDecodingRequest: Boolean = true
//
//  override protected def createMessage(initialLine: Array[String]): TransmissionMessage = {
//    println("TransmissionDecoder")
//    if (initialLine(0).split("/").length == 2) {
//      val protocolFull = assertGet(initialLine(0).split("/"))(x => x.length == 2)
//      val status = initialLine(1)
//
//      return RpcResponse()
//        .protocol(RpcProtocolType.withName(protocolFull(0)))
//        .protocolVersion(ProtocolVersion.withName(protocolFull(1)))
//    }
//    else {
//      val protocolFull = initialLine(2).split("/")
//      return RpcRequest()
//        .protocol(RpcProtocolType.withName(protocolFull(0)))
//        .protocolVersion(ProtocolVersion.withName(protocolFull(1)))
//        .uri(initialLine(1))
//    }
//  }
//
//  override protected def createInvalidMessage(): TransmissionMessage = {
//    new TransmissionMessage()
//      .headerSet(HeaderKey.PROTOCOL, RpcProtocolType.HTTP)
//      .headerSet(HeaderKey.PROTOCOL_VERSION, ProtocolVersion.V_1_0)
//      .headerSet(HeaderKey.URI, "/bad-request")
//  }
//}

object TransmissionDecoder {

  def apply(context: RpcHubEnv): TransmissionDecoder = new TransmissionDecoder(context)
}
