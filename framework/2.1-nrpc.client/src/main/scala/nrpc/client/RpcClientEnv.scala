package nrpc.client

import nrpc.client.channel.provider.ChannelProvider
import nrpc.constants.value.KeepAlive.KeepAlive
import nrpc.constants.value.ProtocolVersion.ProtocolVersion
import nrpc.constants.value.RpcProtocolType.RpcProtocolType
import nrpc.log.ILog
import nrpc.protocol.Protocol
import nrpc.rpc.RpcHubEnv


class RpcClientEnv(val name: String,
                   val log: ILog,
                   val currentProtocol: Protocol,
                   val isThreadDaemon: Boolean = true,
                   val alive: KeepAlive,
                   val channelProvider: ChannelProvider,
                   val conf: RpcClientConf) extends RpcHubEnv {

}
