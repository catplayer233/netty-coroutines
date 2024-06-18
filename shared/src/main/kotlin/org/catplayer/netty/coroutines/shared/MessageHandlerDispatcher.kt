package org.catplayer.netty.coroutines.shared

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.serializer
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

/**
 * message handle dispatcher, we use kotlin coroutines,
 *
 * here we should make sure message handle logic should be executed in target channel's event loop executor orderly
 *
 */
class MessageHandlerDispatcher(private val actions: Map<Int, ActionHandlerContainer<*>>) :
    SimpleChannelInboundHandler<Message>() {

    private lateinit var handlerExecutionCoroutineScope: CoroutineScope

    override fun channelActive(ctx: ChannelHandlerContext) {
        val channel = ctx.channel()
        handlerExecutionCoroutineScope = CoroutineScope(SupervisorJob() + channel.eventLoop().asCoroutineDispatcher())
        super.channelActive(ctx)
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: Message) {
        val actionCode = msg.action
        val handler = actions[actionCode] ?: error("unsupported action code [$actionCode]")

        //fixme: design a special coroutine context
        runBlocking {
            handler.handle(ctx, msg.detail ?: "{}")
        }
    }
}

data class ActionHandlerContainer<T : Any>(
    private val type: KClass<T>,
    private val handler: suspend ChannelHandlerContext.(T) -> Unit
) {

    @OptIn(ExperimentalSerializationApi::class)
    @Suppress("UNCHECKED_CAST")
    suspend fun handle(channelHandlerContext: ChannelHandlerContext, message: String) {
        val typedDetail = businessJSON.decodeFromString(serializer(type, emptyList(), false), message)
            ?.let { it as? T }
            ?: error("unsupported action message [$message]")

        channelHandlerContext.handler(typedDetail)
    }

}

@MessageDsl
class MessageHandlerDispatcherBuilder {

    private val actions: MutableMap<Int, ActionHandlerContainer<*>> = hashMapOf()

    fun <T : Any> register(messageAction: MessageAction<T>, handler: suspend ChannelHandlerContext.(T) -> Unit) {
        val code = messageAction.code
        actions[code]?.also { LOGGER.warn("already registered: [$code]") }
        actions[code] = ActionHandlerContainer(messageAction.type, handler)
    }

    companion object {

        private val LOGGER = LoggerFactory.getLogger(MessageHandlerDispatcherBuilder::class.java)

        @MessageDsl
        fun build(block: MessageHandlerDispatcherBuilder.(MessageHandlerDispatcherBuilder) -> Unit): MessageHandlerDispatcher {

            return MessageHandlerDispatcherBuilder()
                .apply { block(this) }
                .run {
                    MessageHandlerDispatcher(actions)
                }
        }
    }
}
