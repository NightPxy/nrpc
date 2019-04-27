package nrpc.utils.pattern

import nrpc.exception.validate.InvalidateArgException


trait AssertPattern {
  def assertStringNotEmpty(assert: => String, msg: String = "assertStringNotEmpty") = {
    val result = assert
    if (result == null || result.isEmpty) {
      throw InvalidateArgException(msg)
    }
  }

  def assertTrue(assert: => Boolean, msg: String = "assertStringNotEmpty") = {
    val result = assert
    if (!result) {
      throw InvalidateArgException(msg)
    }
  }

  def assertGet[T](process: => T, msg: String = "assertGet")(assert: T => Boolean): T = {
    val processResult = process
    val assertResult = assert(processResult)
    if (!assertResult) {
      throw InvalidateArgException(msg)
    }
    return processResult
  }
}
