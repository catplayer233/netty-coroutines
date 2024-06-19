package org.catplayer.netty.coroutines.shared

import io.netty.channel.Channel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import kotlin.coroutines.CoroutineContext

/**
 * a channel bind a netty channel dispatcher make sure all message will be handled as channel's
 *
 *
 * in a common netty channel the event will happen in such ways:
 *
 * in a queue:
 *
 *
 */
class NettyChannelDispatcher(private val channel: Channel) : CoroutineDispatcher() {
    /**
     * we only dispatch the target coroutines when current thread is not the target event loop's execute thread
     */
    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        return !channel.eventLoop().inEventLoop()
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        //run in target event loop
        channel.eventLoop().execute(block)
    }
}