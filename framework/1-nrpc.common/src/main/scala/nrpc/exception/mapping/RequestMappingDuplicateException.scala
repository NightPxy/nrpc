package nrpc.exception.mapping

class RequestMappingDuplicateException(msg:String) extends RuntimeException(msg){
  
}

object RequestMappingDuplicateException {
  def apply(msg: String): RequestMappingDuplicateException = new RequestMappingDuplicateException(msg)
}
