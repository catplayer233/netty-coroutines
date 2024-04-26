package org.catplayer.netty.coroutines.shared

import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelOutboundInvoker
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.serializer


suspend inline fun <reified T : Any> ChannelOutboundInvoker.writeAndFlushSuspend(
    messageAction: MessageAction<T>,
    contentProvider: () -> T
) {
    val message = Message(messageAction.code, businessJSON.encodeToString(serializer(), contentProvider()))

    val channelFuture = writeAndFlush(message)

    if (channelFuture.isDone) {
        return
    }

    //the writing not finished, we suspend current continuation
    suspendCancellableCoroutine { continuation ->

        val resumeByChannelFutureResult: ChannelFuture.() -> Unit = {
            val error = cause()
            val result = if (error != null) {
                Result.failure(error)
            } else {
                Result.success(Unit)
            }

            continuation.resumeWith(result)
        }

        //check before we add hooks for channelFuture
        if (channelFuture.isDone) {
            channelFuture.resumeByChannelFutureResult()
            return@suspendCancellableCoroutine
        }

        //add hook when the future finished
        channelFuture.addListener {
            channelFuture.resumeByChannelFutureResult()
        }

        continuation.invokeOnCancellation { channelFuture.cancel(false) }
    }
}