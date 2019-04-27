package nrpc.utils.mq.disruptor

import com.lmax.disruptor.WaitStrategy
import com.lmax.disruptor.dsl.ProducerType


case class DisruptorMemoryMQCfg[T <: DisruptorMemoryMQMessage](
                                                                eventCreator: () => T,
                                                                bufferCount: Int = 2 << 16,
                                                                productType:ProducerType = ProducerType.MULTI,
                                                                waitStrategy: WaitStrategy
                                                              )
