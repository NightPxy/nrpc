package nrpc.utils

import nrpc.utils.implicits._
import nrpc.utils.pattern.{AssertPattern, NotCareExceptionPattern, PromiseProcessPattern}


object All extends NotCareExceptionPattern
  with PromiseProcessPattern
  with AssertPattern {

  implicit def $SerializeImplicit(target: java.io.Serializable) = SerializeImplicit(target)

  implicit def $BytesSerializeImplicit(target: Array[Byte]) = BytesSerializeImplicit(target)

  implicit def $StringUtils(value: String) = StringImplicit(value)


}
