package nrpc.rpc

import io.netty.util.AsciiString
import nrpc.constants.HeaderKey
import nrpc.constants.HeaderKey.HeaderKey
import nrpc.constants.value.MessageDirect.MessageDirect
import nrpc.constants.value.ProtocolVersion.ProtocolVersion
import nrpc.constants.value.RpcProtocolType.RpcProtocolType
import nrpc.constants.value.{MessageDirect, ProtocolVersion, RpcProtocolType}
import nrpc.exception.validate.InvalidateArgException

/**
  * RPC传输模型
  *    RPCRequest 和 RPCResponse 视为传输模型的上层读写应用
  * 传输模型中必须包含
  *    1. 协议
  *    2. 协议版本号
  *    3. 传输方向
  *           正向:客户端->服务端,也就是常规意义的 Request
  *           反向:服务端->客户端,也就是常规意义的 Response
  */
case class TransmissionMessage() {
  protected var header: scala.collection.mutable.HashMap[String, String] = new scala.collection.mutable.HashMap[String, String]()
  protected var body = ""

  def getHeader() = this.header

  private def setHeader(header: scala.collection.mutable.HashMap[String, String]):this.type ={
    this.header = header
    this
  }

  def headerSet(key: HeaderKey, value: String):this.type = {
    this.header.put(key.toString, value)
    this
  }

  def headerSet(key: String, value: String):this.type = {
    this.header.put(key.toString, value)
    this
  }

  def headerSet(key: HeaderKey, value:AnyRef):this.type = {
    this.header.put(key.toString, value.toString)
    this
  }

  def headerGetOpt(key: HeaderKey):Option[String] = this.header.get(key.toString)

  def headerGetAsst(key: HeaderKey) = this.headerGetOpt(key) match {
    case Some(v) => v
    case None => throw InvalidateArgException(s" Request-Transmission-Model must set header for [${key}] ")
  }

  def headerGetAsstByAsciiString(key: HeaderKey) = AsciiString.cached{
    headerGetAsst(key)
  }

  def headerGetOrElse(key: HeaderKey, defaultValue: String) = this.headerGetOpt(key) match {
    case Some(v) => v
    case None => defaultValue
  }

  def headerGet(key: HeaderKey) = this.header.get(key.toString)

  def content(body: String): this.type = {
    this.body = body
    this
  }

  def content() = this.body


  def protocol() = RpcProtocolType.withName(this.headerGetAsst(HeaderKey.PROTOCOL))

  def protocol(value: RpcProtocolType): this.type = this.headerSet(HeaderKey.PROTOCOL, value.toString)

  def protocolVersion() = ProtocolVersion.withName(this.headerGetAsst(HeaderKey.PROTOCOL_VERSION))

  def protocolVersion(value: ProtocolVersion): this.type = this.headerSet(HeaderKey.PROTOCOL_VERSION, value)

  def direct() = MessageDirect.withName(this.headerGetAsst(HeaderKey.DIRECT))

  def direct(value: MessageDirect): this.type = this.headerSet(HeaderKey.DIRECT, value)

  def uniqueRequestId() = this.headerGetAsst(HeaderKey.UNIQUE_REQUEST_ID)

  def uniqueRequestId(requestId: String): this.type = this.headerSet(HeaderKey.UNIQUE_REQUEST_ID, requestId)

  override def toString() = {
    s"{header:${header.toString()},content:$content "
  }
}

//object TransmissionModel {
//
//  /**
//    * 段一(请求行):
//    * 消息全长(不含段一):定长4位
//    * 段二(Header):
//    * header段长度: 定长2位
//    * 段内格式 k=v;
//    * requestId,serviceType,messageType,
//    */
//  private val MESSAGE_LENGTH_OFFSET = 0
//  private val MESSAGE_LENGTH_SIZE = 4
//  private val MESSAGE_HEADER_SIZE = 2
//
//  class TransmissionMessageDecoder
//    extends ByteToMessageDecoder {
//
//
//    override def decode(channelHandlerContext: ChannelHandlerContext, in: ByteBuf, out: util.List[AnyRef]): Unit = {
//      val heapBuffer = in; // super.decode(ctx,in).asInstanceOf[ByteBuf];
//
//      if (null == heapBuffer) return
//      // val heapBuffer = ctx.alloc().heapBuffer();
//
//      //消息总长
//      val messageSize = heapBuffer.readInt()
//      //消息头
//      val headerSize = heapBuffer.readShort()
//
//      val headerBytes = new Array[Byte](headerSize)
//      val header = heapBuffer.readBytes(headerBytes)
//
//      //消息体
//      val bodySize = messageSize - TransmissionModel.MESSAGE_HEADER_SIZE - headerSize
//      val bodyBytes = new Array[Byte](bodySize)
//      val body = heapBuffer.readBytes(bodyBytes)
//
//      val m = TransmissionModel()
//      val hashMap = headerBytes.bytesTo[mutable.HashMap[String, String]]().getOrElse(null)
//      m.setHeader()
//      m.content(bodyBytes.bytesTo[String]().getOrElse(""))
//      return out.add(m)
//    }
//
//    //extends LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,TransmissionMessage.MESSAGE_LENGTH_OFFSET,TransmissionMessage.MESSAGE_LENGTH_SIZE) {
//    //    override def decode(ctx: ChannelHandlerContext, in: ByteBuf): AnyRef = {
//    //
//    //    }
//
//    override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
//      cause.printStackTrace()
//      super.exceptionCaught(ctx, cause)
//    }
//  }
//
//  //  class TransmissionMessageDecoder
//  //    extends LengthFieldPrepender(TransmissionMessage.MESSAGE_LENGTH_SIZE, false) {
//  //
//  //
//  //    override def encode(ctx: ChannelHandlerContext, msg: ByteBuf, out: util.List[AnyRef]): Unit = {
//  //      super.encode(ctx, msg, out)
//  //
//  //
//  //
//  //    }
//  //  }
//
//  class TransmissionMessageEncoder extends MessageToByteEncoder[TransmissionModel] {
//    override def encode(channelHandlerContext: ChannelHandlerContext, message: TransmissionModel, byteBuf: ByteBuf): Unit = {
//      val header = message.header.toBytes()
//      val headerSize = header.length;
//
//      val body = message.body.toBytes()
//      val bodySize = body.length
//
//      //段一 消息全长但不含长度位本身 定长4
//      val messageSize = TransmissionModel.MESSAGE_HEADER_SIZE + headerSize + bodySize
//      byteBuf.writeInt(messageSize)
//      //段二 消息头长度(定长4,但不含长度本身) + 消息头内容
//      byteBuf.writeShort(headerSize).writeBytes(header)
//      //段三 消息体
//      byteBuf.writeBytes(body)
//    }
//  }
//
//}
