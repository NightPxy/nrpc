package nrpc.exception.protocol

import nrpc.exception.NRpcException


class ProtocolException(msg: String) extends NRpcException(msg) {

}

object ProtocolException {
  def apply(msg: String): ProtocolException = new ProtocolException(msg)
}
