package nrpc.metric


object MetricEventType extends Enumeration {
  type MetricEventType = Value

  val UNDEFINED = Value(0)
  val REQUEST = Value(10)
}
