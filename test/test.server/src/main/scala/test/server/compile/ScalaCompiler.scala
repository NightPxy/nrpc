package test.server.compile

import scala.reflect.runtime._
import scala.tools.reflect.ToolBox

object ScalaCompiler {
  type CompiledExpr = () => Any

  private[this] val _compiler = {
    currentMirror.mkToolBox()
  }

  def compile(code: String): CompiledExpr = {
    val tree = _compiler.parse(code)
    _compiler.compile(tree)
  }

  def eval(code: String): Any = {
    compile(code)()
  }

  def evalAs[T](code: String): T = {
    eval(code).asInstanceOf[T]
  }
}
