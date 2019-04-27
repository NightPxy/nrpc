package nrpc.constants.value

object RequestMethod extends Enumeration {
  type RequestMethod = Value

  val PUT = Value("PUT")
  val GET = Value("GET")
  val POST = Value("POST")
}
