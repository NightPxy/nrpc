package nrpc.constants.value


object KeepAlive extends Enumeration {
  type KeepAlive = Value

  val NONE = Value(null)
  val KEEP_ALIVE = Value("keep_alive")
}
