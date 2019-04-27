package nrpc.utils.pattern

trait NotCareExceptionPattern {
  def notCareException(notCareExceptionProcess: => Unit): Unit = {
    try {
      notCareExceptionProcess
    } catch {
      case e: Throwable => e.printStackTrace()
    }
  }
}
