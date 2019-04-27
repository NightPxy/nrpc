package nrpc.service.mapping

import nrpc.annotation.RequestMapping
import nrpc.exception.mapping.RequestMappingDuplicateException
import nrpc.utils.All._

object MappingResolver {

  /**
    * 解析出类级别的请求映射
    * 1.未使用类级别的请求映射时,使用空代替
    * 2.使用类级别的请求映射但未设置时,使用空代替
    */
  private def resolveClassMapping(clazz: Class[_]): String = {
    val mappingAnnotation = clazz.getAnnotation(classOf[RequestMapping]);
    if (mappingAnnotation == null) return ""
    val path = mappingAnnotation.value();
    if (path.isNullOrEmpty) {
      return ""
    }
    else {
      return path
    }
  }

  private def classMappingFormat(clazzMapping: String): String = {
    val sep = Constant.MAPPING_SEP
    var format = clazzMapping
    if (format.isNullOrEmpty) return sep
    format = if (!format.startsWith(sep)) sep + format else format
    format = if (format.endsWith(sep)) format + sep else format
    return format
  }

  /**
    * 解析出方法级别请求映射
    * 方法级请求映射为最终使用的映射结果 = 类级别请求映射 + "/" + 方法级别请求映射
    * 1.未使用方法级别请求映射时,该方法不加入映射
    * 2.使用方法级别请求映射但未设置时,使用方法名代替
    */
  def resolve(mapping:ServerMapping, target: Any, clazz: Class[_]) = {


    val classMapping = classMappingFormat {
      resolveClassMapping(clazz)
    }

    val methods = clazz.getMethods
    methods.foreach(method => {
      val mappingAnnotation = method.getAnnotation(classOf[RequestMapping])
      if (mappingAnnotation != null) {
        var path = mappingAnnotation.value();
        if (path.isNullOrEmpty) {
          path = method.getName
        }
        if (path.startsWith(Constant.MAPPING_SEP)) {
          path = path.substring(1, path.length)
        }
        val fullPath = classMapping + Constant.MAPPING_SEP + path
        if (mapping.containsKey(fullPath)) {
          throw RequestMappingDuplicateException(s" $path is duplicate")
        }
        else {
          println(s"注册成功 $fullPath => ${method.getName}")
          mapping.setMapping(fullPath, MappingPath(fullPath, method, target))
        }
      }
    })
  }
}
