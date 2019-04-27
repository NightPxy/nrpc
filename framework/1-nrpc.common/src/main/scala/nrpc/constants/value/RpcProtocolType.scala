package nrpc.constants.value

object RpcProtocolType extends Enumeration {
  type RpcProtocolType = Value
  val NTCP = Value("NTCP")

  val HTTP = Value("HTTP")
}
