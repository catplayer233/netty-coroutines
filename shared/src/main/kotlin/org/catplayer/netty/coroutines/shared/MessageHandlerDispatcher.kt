package org.catplayer.netty.coroutines.shared

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.serializer
import org.slf4j.LoggerFactory
import kotlin.collections.ArrayDeque
import kotlin.reflect.KClass

/**
 * message handle dispatcher, we use kotlin coroutines,
 *
 * here we should make sure message handle logic should be executed in target channel's event loop executor orderly
 *
 */
class MessageHandlerDispatcher(private val actions: Map<Int, ActionHandlerContainer<*>>) :
    SimpleChannelInboundHandler<Message>() {

    private val messageJobs = ArrayDeque<Job>()

    private var currentJob: Job? = null

    private lateinit var executeScope: CoroutineScope

    override fun channelActive(ctx: ChannelHandlerContext) {
        executeScope = CoroutineScope(SupervisorJob() + NettyChannelDispatcher(ctx.channel()))
        super.channelActive(ctx)
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: Message) {
        val actionCode = msg.action
        val handler = actions[actionCode] ?: error("unsupported action code [$actionCode]")
        executeScope.launch(start = CoroutineStart.LAZY) {
            handler.handle(ctx, msg.detail ?: "{}")
        }
            .also {
                it.invokeOnCompletion {
                    currentJob = null
                    tryStartJob()
                }

                messageJobs.add(it)
            }

        tryStartJob()
    }

    private fun tryStartJob() {
        //job is null or job already finished
        if (currentJob?.isCompleted != false) {
            val job = messageJobs.removeFirstOrNull() ?: return
            currentJob = job
            job.start()
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        executeScope.coroutineContext[Job]?.cancel()
        messageJobs.clear()
        super.channelInactive(ctx)
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
