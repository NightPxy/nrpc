package nrpc.utils.pattern


trait PromiseProcessPattern extends NotCareExceptionPattern {
  def promiseProcess(process: => Any)(promise: => Unit) = try {
    process
  }
  finally {
    promise
  }

  type CloseAble = {def close(): Unit}

  def using[T <: CloseAble](target: => T)(process: T => Any) = {
    val targetAutoClose = target
    promiseProcess {
      process(targetAutoClose)
    }{
      notCareException {
        targetAutoClose.close()
      }
    }
  }
}
