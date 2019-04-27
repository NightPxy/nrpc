package nrpc.rpc

import nrpc.log.ILog
import nrpc.metric.MetricHub


abstract class RpcHubEnv {
  val name: String
  val log: ILog
  val isThreadDaemon:Boolean

  val metric = new MetricHub(this).start()
}
