package nrpc.exception.transfer

import nrpc.exception.NRpcException


class RequestTimeoutException(msg: String) extends NRpcException(msg) {

}
