package nrpc.constants.value

object RpcResponseStatus extends Enumeration  {
  type ResponseStatus = Value

  val OK = Value("200")
  val NOT_FOUND = Value("404")
  val INTERNAL_ERROR = Value("500")
}
