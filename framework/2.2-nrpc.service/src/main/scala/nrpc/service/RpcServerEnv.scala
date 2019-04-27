package nrpc.service

import nrpc.log.ILog
import nrpc.rpc.RpcHubEnv
import nrpc.service.mapping.{MappingResolver, ServerMapping}

class RpcServerEnv(val name: String,
                   val log: ILog,
                   val isThreadDaemon:Boolean = false,
                   val mapping:ServerMapping,
                   val conf:RpcServerConf) extends RpcHubEnv{

}
