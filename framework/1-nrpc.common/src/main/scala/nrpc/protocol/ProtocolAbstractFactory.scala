package nrpc.protocol

/**
  * 协议抽象工厂
  *   提供:
  *       1.协议的编解码器
  *       2.协议的工具实现
  */
abstract class ProtocolAbstractFactory {
  def createCodec():ProtocolCodec
  def createTool(): ProtocolTool
}
