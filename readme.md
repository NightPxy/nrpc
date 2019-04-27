
## 概述  

RPC框架,基于Netty实现   

### 目的  
* 体验RPC设计,了解Netty  
* Coding和设计能力锻炼    

### 主要功能  

* 服务端  
服务反射与Mapping映射  
支持服务端的多段响应  
连接自动心跳关闭  
* 客户端  
多种发送方式`sync`,`async`,`one-way`以及回调超时机制  
发送连接池复用  
长服务的自动心跳保持
* 通用  
日志依赖注入,适应远程日志或自定义日志要求  
简单Metric  


### 项目结构  

*nrpc.common*  
* constants  常量区   
* exception  异常设计相关  
NRpcException(继承Runtime)是所有nrpc异常基类,下分参数验证,协议,映射等具体异常  
* protocol  协议相关(编解码过程,工具等)  
* rpc rpc相关 
请求-响应消息定义,基础环境Env定义,Rpc基础处理中心等  
* utils  
scala的扩展隐式转换  
语法糖模式定义   
内存消息队列封装等  

*nrpc.client*  
* callback  回调以及回调管理器(注册以及过期处理)相关  
* channel.provider 连接池相关   
* handler.send 发送方式相关  
* RpcClient 客户端的核心类   
* RpcClientEnv 客户端的环境相关(配置,默认协议,反射构建的连接池等)  
* RpcClientConf 客户端的配置,视为来自Ioc  

*nrpc.service*  
* mapping  服务映射相关  
* RpcServer 服务端的核心类    
* RpcServerEnv 服务端的环境相关(配置,反射构建的日志,映射器等)
* RpcServerConf 服务端的配置,视为来自Ioc  

## 传输  

### 数据包模型  

传输模型由以下三部分组成  
* 协议行  
  协议行的传输格式与任何协议无关,固定格式为`协议名/协议版本号\r\n` 
  协议行的目的不是为了数据传输,而是标记该数据包使用的协议格式  
  协议行上限不得超过256字节  
* 数据头  
  数据包的键值对数据信息  
* 数据体  
  数据包的数据内容  

### 协议  

#### NTCP  

NTCP协议规定  
* 协议行固定为 `NTCP/1.1\r\n`  
* 协议行之后第一个开始是一个4字节数字,标志整个数据包长度(不包括协议行)  
* 之后是一个2字节数字,标志数据包中的消息头长度  
* 之后数据头和数据体的二字节数据内容  
数据头数据为Java自带序列化后的`HashMap`  
数据体数据为Java自带序列化后的`String`  
(暂时使用Java自带序列化,后续可以使用性能更好的诸如Kyro的序列化方式)  

### 实现  

```scala
/**
  * 协议抽象工厂
  *   提供:
  *       1.协议的编解码器
  *       2.协议的工具实现
  */
abstract class ProtocolAbstractFactory {
  def createCodec():ProtocolCodec
  def createTool(): ProtocolTool
}
/**
  * 协议的编解码器
  */
trait ProtocolCodec {
  def encode(ctx: ChannelHandlerContext, message: TransmissionMessage, byteBuf: ByteBuf)
  def decode(ctx: ChannelHandlerContext, in: ByteBuf):TransmissionMessage
}
/**
  * 协议的工具类方法
  *    比如该协议要求的 请求/响应 的必传参数的验证等
  */
trait ProtocolTool {
  def validateForRequest(message:RpcRequest):Boolean
  def validateForResponse(message:RpcResponse):Boolean
  def isKeepAlive(message:TransmissionMessage):Boolean
}

/**
* 编码核心过程
*/
//根据传输数据包获取目标协议处理器
val protocol = ProtocolFactory.assertGetProtocol(message)
val protocolConent = AsciiString.cached(s"${message.protocol}/${message.protocolVersion}\r\n")
//固定协议行
ByteBufUtil.copy(protocolConent, byteBuf)
protocol.createCodec.encode(ctx, message, byteBuf)

/**
* 解码核心过程
*/
//行断检测
val protocolLineParser = new ProtocolLineParser(new AppendableCharSequence(64), 256)
//获取协议行
val line = protocolLineParser.parse(in)
//根据协议行获取目标协议处理器
val protocol: Protocol = ProtocolFactory.assertGetProtocol(protocolLine(0), protocolLine(1))
//根据目标协议解出数据包
val transmissionMessage = protocol.createCodec.decode(ctx, in)
```

## 应用  


### RPC消息  

RPC消息是`传输数据包模型的上层应用`  
每一个数据包都将转为以下某种消息再进入应用层使用  
*  `RpcRequest` 请求消息  
* `RpcResponse`  响应消息  


```scala
/**
* RPC消息架构在传输消息之上,仅仅是读取或写入传输消息结构中
*/
class RpcRequest extends TransmissionMessage {
  this.direct(MessageDirect.REQUEST)
  this.uniqueRequestId(UUID.randomUUID().toString)  
  
   def messageType() = RpcMessageType.withName {
    this.headerGetOrElse(HeaderKey.MESSAGE_TYPE, RpcMessageType.NORMAL.toString)
  }

  def messageType(messageType: RpcMessageType): this.type =   
    this.headerSet(HeaderKey.MESSAGE_TYPE, messageType)        
  
  .....
}
class RpcResponse extends TransmissionMessage {
  this.direct(MessageDirect.RESPONSE)
  def status() = RpcResponseStatus.withName {
    this.headerGetAsst(HeaderKey.STATUS)
  }

  def status(status: ResponseStatus) = 
    this.headerSet(HeaderKey.STATUS, status)
    
 .....
}
```

### 服务端  

#### Demo  

```scala

@RequestMapping("/test")
class TestController {
  ...
  @RequestMapping("/single")
  def single(request: RpcRequest, context: RpcContext): String = {
    val content = request.content()
    val proccess = s"$content-开始处理"
    println("proccess"+proccess)
    return proccess;
  }

  @RequestMapping("/mul")
  def mul(request: RpcRequest, context: RpcContext): String = {
    val content = Integer.parseInt(request.content())
    val proccess = s"$content-开始处理"
    threadPool.execute(new DoSomethingThread(content,context))
    return proccess;
  }
}

val ioc =  RpcServerConf(
  host = "127.0.0.1",
  port = 10030,
  services = "test.server.controllers.TestController,test.server.controllers.SparkController",
  notAcceptAlive = false,
  heartTimeoutMilSec = 15000,
  bossCount = 1,
  workerCount = 5
)

val server = ioc.build()

server.start()
```

#### 服务反射与Mapping  

* 使用配置`services`声明服务  
* 服务中根据注解`RequestMapping`反射生成服务具体的`Uri-Mapping`  
  如果重复将会抛出`RequestMappingDuplicateException`异常  
* 类`RequestMapping`注解如未定义则默认为类名  
  方法`RequestMapping`注册如未声明则默认为方法名  
  方法未打上`RequestMapping`注册则不会对外提供`Uri-Mapping`  
* 因为性能问题，服务的反射实例是单例的  

```scala
/*
* 这里比较简陋是直接使用method.invoke,这样固定了服务方法参数  
* 更好的做法是再做一层抽象适应不同的参数要求,然后再反射扫描服务时构建不同的执行
* 这样就可以在服务方法中适配诸如
*    def x(request: RpcRequest)
*    def x(request: RpcRequest, context: RpcContext)
*    def x(id:String,name:String) 等等
*/
case class MappingPath(path:String,method:Method,target:Any)
{
  def invoke(arg:AnyRef*) = method.invoke(target,arg:_*)
}
```

#### 服务响应  

同步响应
* `Unit`返回,将自动返回一个Status=200的空响应  
* `String`返回,将返回一个Status=200,content为返回值的响应  
* `RPCResponse`返回,将使用该Response直接返回  

多段响应  
* 多段响应是指服务端针对某一个请求的处理过程中,再次发送多段的响应  
比如一个长时处理,可以逐段发送已进行的处理步骤等   
* 多段响应的客户端必须是异步的  
同步请求,只能收到第一段响应 (同步的Response是由CAS->Null控制的)     
* 多段响应的客户端必须保持心跳,否则不能持续接受到响应  

```scala
/*
* 多段响应Demo
*/

// 服务端
@RequestMapping("/execute")
def execute(request: RpcRequest, context: RpcContext): String = {
	val content = request.content()
	try
	{
	  //长时服务的线程池执行
	  threadPool.execute(new SparkThread(content,context))
	  //先发送第一段响应,表示正在处理  
	  return "Success" 
	}
	catch {
	  case e:Throwable => return e.getMessage
	}
}

private class SparkThread(doCode:String, context: RpcContext) extends Runnable {
	override def run(): Unit = {
	  val code = s"$doCode"
	  val result = CusSparkILoop.run(code)
	  //线程池内的真正执行,继续发送后续处理结果
	  context.sendResponse(result)
	}
}

//客户端 
val confLikeIoc = RpcClientConf(
 ...
  isKeepHeart = true, //客户端必须保持心跳
  heartTimeoutMilSec = 3000
)
val client: RpcClient = confLikeIoc.build();

val code =
  """
   val rdd = spark.sparkContext.parallelize(Array("a,b,c,d", "a,b,c,d"))
   rdd.flatMap(x => x.split(",")).map(x => (x, 1)).reduceByKey(_ + _).collect()
  """.stripMargin

promiseProcess{
  client.requestAsync(new RpcRequest() {
	this.uri("/spark/execute")
	this.content(code)
  }, { response =>
	 println(response.content())
  })
  System.in.read()
}{
  println(client.env.metric.RequestMetric)
  client.close()
}
```


#### 心跳关闭

* 服务端本身没有长短服务的区分  
* 服务端对每一个连接都会维持心跳过期检测(基于配置)  
只要客户端在心跳超时时间内没有发送任何数据包,即关闭连接  
* 服务端允许客户端发送特殊心跳包  
服务端对心跳包的处理是`只读取不响应`,但会刷新心跳超时时间  
* 服务端可以根据配置强制为不接受长服务请求  
开启`notAcceptAlive=true`配置后,服务端将在输出完毕后立即关闭连接  

### 客户端  

#### Demo  

```scala
val confLikeIoc = RpcClientConf(
  protocol = "NTCP",
  protocolVersion = "1.1",
  host = "127.0.0.1",
  port = 10030,
  works = 5, //客户端工作线程5
  log = "nrpc.log.ConsoleLog",
  channelProvider = "nrpc.client.channel.provider.DefaultChannelProvider",
  maxConnection = 5, //连接池最大5
  maxPending = Integer.MAX_VALUE, //
  isKeepHeart = true,
  heartTimeoutMilSec = 3000
)
val client: RpcClient = confLikeIoc.build();

// 同步请求
val response = client.requestSync(new RpcRequest() {
	this.uri("/test/single")
	this.content(i.toString + "-sync")
})
println(response.content())

//异步请求
client.requestAsync(new RpcRequest() {
       this.uri("/test/single")
       this.content(i.toString + "-async")
    }, { response =>
       println(response.content())
    }
})

//单向请求
client.requestOneWay(new RpcRequest() {
	this.uri("/test/single")
	this.content(i.toString + "-oneway")
  })
```

#### 发送方式  

`nrpc.client.handler.send.SendHandler`的具体实现  
* sync 同步请求  
请求验证,注册回调,异步发送,阻塞线程直到超时或收到回调通知  
* async 异步请求  
请求验证,注册回调,异步发送,收到回调响应后调用目标回调的回调函数  
* oneway 单向请求  
请求验证,异步发送(不注册回调,也就是不处理服务端响应)  

```scala
// 发送请求抽象  
class SendHandler(request: RpcRequest,
                  responseAsyncHandler: RpcResponse => Unit,
                  env: RpcClientEnv,
                  callbackManager: CallbackManager,
                  channelProvider: ChannelProvider) {
  protected def preSend():this.type = ...

  protected def registerCallback(timeout:Long):this.type = ...

  protected def sendAsync():this.type = ...

  protected def waitForCallback(timeoutMils: Long):RpcResponse =...
}

//同步发送组合
class SyncSendHandler(...) extends SendHandler(...) {
  def send(timeoutMils: Long): RpcResponse = {
    this
      .preSend()
      .registerCallback(timeoutMils)
      .sendAsync()
      .waitForCallback(timeoutMils)
  }
}
//异步发送组合
class AsyncSendHandler(...) extends SendHandler(...){
  def send(timeoutMils: Long): Unit = {
    this
      .preSend()
      .registerCallback(timeoutMils)
      .sendAsync()
  }
}
//单向发送组合/
class OneWaySendHandler(...) extends SendHandler(...){
  def send(): Unit = {
    this
      .preSend()
      .sendAsync()
  }
}
```

#### 心跳保持  

配置开启`isKeepHeart=true`来维持心跳保持  
配置`heartTimeoutMilSec=?` 控制心跳保持时间,默认3000(3秒)  

开启心跳保持后  
客户端连接如果超过心跳时间后没有任何数据发送,将自动发送一个心跳包  

#### 连接池  

连接池通过配置`channelProvider`来完成  
默认即使用`nrpc.client.channel.provider.DefaultChannelProvider`  
该连接池使用Netty自带的`FixedChannelPool`作为核心实现  
配置`maxConnection`来控制连接数量    

自定义实现需继承实现`nrpc.client.channel.provider.ChannelProvider`  

```scala
abstract class ChannelProvider{

  def init(env: RpcClientEnv, client: Bootstrap, handler: =>ChannelHandler):this.type ;

  def acquire(): Future[Channel]

  def release(channel: Channel)

  def close()
}
```

#### 回调池  

因为Netty本身是纯异步的  
所以对于`sync`和`async`请求,客户端将使用回调注册的方式完成响应  

* 回调的映射关系依靠`RPCRequest.uniqueRequestId`  
这是一个每一个RPC请求自动生成的唯一请求ID(暂时使用UUID)  
* 考虑到服务端的多段响应,回调并非一次性的,所以依赖回调过期机制而不是响应移除    

回调自动过期  
* 每一个回调都会有一个明确的且不可修改的过期时间  
* `ConcurrentHashMap[String, Callback]`,以唯一请求ID关联回调  
如果`putIfAbsent`注册成功,即通过一个内存消息队列`PollMemoryMQ`将回调的注册的时间线处理过程转为单线程形式  
* `util.TreeMap[Long, util.LinkedList[Callback]]`,回调过期时间线   
回调将按照过期时间按秒向下取整后进入时间线某个过期分组中  
之所以采用分组是为了降低小堆扫描数量,回调可能有非常多个就算红黑也是吃不消的,所以必须用分组将堆数量降下来,然后根据当前时间扫描最早过期分组,批量移除分组内所有的过期回调  

`PollMemoryMQ`基于`LinkedBlockingQueue`封装  
使用`LinkedBlockingQueue`而并非使用`Disruptor`是因为清理过程并非一个纯粹的消息订阅过程,即必须保证如果消息全部消费完毕后,也保持定时再次扫描.防止某个消息空窗期中回调不能及时释放   


### 通用  

#### 日志   

日志使用配置`log`声明日志处理类  
默认使用`nrpc.log.ConsoleLog`控制台打印  

自定义写出日志如`log4j`或`远程日志系统`等,可以继承实现`ILog`  

```scala
abstract class ILog {
  def debug(string: => String);
  def info(string: => String);
  def waring(string: => String)
  def error(string: => String)
  def error(throwable: Throwable,string: => String)
}
```

#### Metric  

在处理过程中埋点发送至`DisruptorMemoryMQ`,完成监控统计

#### IO线程  

IO线程适合做短小操作,不用产生线程切换  
业务线程适合长耗操作,这样可以尽快复用IO线程提升吞吐量  

关于这里应该有两个方案  
* 依赖配置  
由配置告知使用IO线程处理还是使用单独业务线程池处理  
不好的地方在于整体依赖(或者做细粒度的配置),单使用哪一种都不太好  
* 用户自决  
在RPC阶段统统使用IO线程,即统一视为短小操作  
如果在面对长耗操作是,用户自开线程处理来解放IO线程  

这里暂时使用第二种方案  
