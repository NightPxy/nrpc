package nrpc.log


abstract class ILog {
  def debug(string: => String);
  def info(string: => String);
  def waring(string: => String)
  def error(string: => String)
  def error(throwable: Throwable,string: => String)
}
