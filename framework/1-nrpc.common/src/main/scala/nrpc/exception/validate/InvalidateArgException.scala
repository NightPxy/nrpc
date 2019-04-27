package nrpc.exception.validate

import nrpc.exception.NRpcException

class InvalidateArgException(msg: String) extends NRpcException(msg) {

}

object InvalidateArgException {
  def apply(msg: String): InvalidateArgException = new InvalidateArgException(msg)
}
