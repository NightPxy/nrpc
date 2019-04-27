package nrpc.service.mapping

import java.util.concurrent.ConcurrentHashMap


class ServerMapping {
  private val concurrentHashMap = new ConcurrentHashMap[String, MappingPath]()

  def getMapping(path:String):MappingPath = return this.concurrentHashMap.get(path)

  def setMapping(path:String,mapping:MappingPath) = this.concurrentHashMap.putIfAbsent(path,mapping)

  def containsKey(path:String) = this.concurrentHashMap.containsKey(path)
}
