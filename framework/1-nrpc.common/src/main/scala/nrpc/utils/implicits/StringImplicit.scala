package nrpc.utils.implicits

case class StringImplicit(value: String) {
  def isNullOrEmpty() = if (value == null || value.length == 0) true else false
}
