package org.catplayer.netty.coroutines.shared

import io.netty.channel.Channel
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executor
import kotlin.coroutines.CoroutineContext

/**
 * a channel bind a netty channel dispatcher make sure all message will be handled as channel's
 */
class NettyChannelDispatcher(private val channel: Channel) : ExecutorCoroutineDispatcher() {

    private val dispatcher = channel.eventLoop().asCoroutineDispatcher()

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        dispatcher.dispatch(context, block)
    }

    override val executor: Executor
        get() = dispatcher.executor

    override fun close() {
        //do nothing
    }
}