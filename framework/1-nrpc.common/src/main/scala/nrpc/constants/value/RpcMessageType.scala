package nrpc.constants.value

object RpcMessageType extends Enumeration {
  type RpcMessageType = Value

  val HEART_BEAT = Value("heart_beat")
  val TIME_OUT = Value("timeout")
  val NORMAL = Value("normal")
}
