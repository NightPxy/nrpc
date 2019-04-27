package nrpc.protocol

import java.util

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import nrpc.rpc.TransmissionMessage

/**
  * 协议的编解码器
  */
trait ProtocolCodec {
  def encode(ctx: ChannelHandlerContext, message: TransmissionMessage, byteBuf: ByteBuf)
  def decode(ctx: ChannelHandlerContext, in: ByteBuf):TransmissionMessage
}
