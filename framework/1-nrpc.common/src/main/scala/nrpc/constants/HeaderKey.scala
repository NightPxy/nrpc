package nrpc.constants

object HeaderKey extends Enumeration {
  type HeaderKey = Value

  val PROTOCOL = Value("protocol")
  val PROTOCOL_VERSION = Value("protocol_version")

  val URI = Value("uri")
  val METHOD = Value("method")
  val DIRECT = Value("direct")

  /*
      RPC Request Header
  */

  val UNIQUE_REQUEST_ID = Value("unique_request_id")
  val MESSAGE_TYPE = Value("message_type")

  /*
      RPC Response Header
  */

  val STATUS =  Value("status")
  val KeepAlive =  Value("keepAlive")
  val CONTENT_TYPE =  Value("Content-Type")
  val UPGRADE = Value("UPGRADE")
}
