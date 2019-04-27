package nrpc.service.mapping

import java.lang.reflect.Method


case class MappingPath(path:String,method:Method,target:Any)
{
  def invoke(arg:AnyRef*) = method.invoke(target,arg:_*)
}
