package nrpc.rpc.codec

import io.netty.buffer.ByteBuf
import io.netty.handler.codec.TooLongFrameException
import io.netty.handler.codec.http.HttpConstants
import io.netty.util.ByteProcessor
import io.netty.util.internal.AppendableCharSequence

/**
  * 协议行解析器
  */
class ProtocolLineParser(seq: AppendableCharSequence, maxLength: Int) extends ByteProcessor {
  private var size = 0


  def parse(buffer: ByteBuf): AppendableCharSequence = {
    val oldSize = size
    seq.reset()
    val i = buffer.forEachByte(this)
    if (i == -1) {
      size = oldSize
      return null
    }
    buffer.readerIndex(i + 1)
    seq
  }

  def reset(): Unit = {
    size = 0
  }

  @throws[Exception]
  override def process(value: Byte): Boolean = {
    val nextByte = (value & 0xFF).toChar
    if (nextByte == HttpConstants.CR) return true
    if (nextByte == HttpConstants.LF) return false
    if ( { size += 1; size } > maxLength) {
      throw newException(maxLength)
    }
    seq.append(nextByte)
    true
  }

  protected def newException(maxLength: Int) =
    new TooLongFrameException("ProtocolLine is larger than " + maxLength + " bytes.")
}
