package nrpc.log

class ConsoleLog extends ILog {
  override def debug(string: => String): Unit = {
//    println {
//      s"**debug**:$string"
//    }
  }

  override def waring(string: => String): Unit = println {
    s"**waring**:$string"
  }

  override def info(string:  => String): Unit = println {
    s"**info**:$string"
  }

  override def error(string:  => String): Unit = println {
    s"**error**:$string"
  }

  override def error(throwable: Throwable, string:  => String): Unit = {
    println {
      s"**error**:$string message:${throwable.getMessage}"
    }
    throwable.printStackTrace()
  }
}
