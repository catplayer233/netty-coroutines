package org.catplayer.netty.coroutines.shared

import io.netty.channel.ChannelOutboundInvoker
import kotlinx.serialization.serializer
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


suspend inline fun <reified T : Any> ChannelOutboundInvoker.writeSuspend(messageAction: MessageAction<T>, msg: T) {
    val message = Message(messageAction.code, businessJSON.encodeToString(serializer(), msg))

    val channelFuture = writeAndFlush(message)

    if (channelFuture.isDone) {
        return
    }

    suspendCoroutine { continuation ->
        if (channelFuture.isDone) {
            val cause = channelFuture.cause()
            if (cause != null) {
                continuation.resumeWithException(cause)
            } else {
                continuation.resume(Unit)
            }

            return@suspendCoroutine
        }

        channelFuture.addListener {
            val cause = it.cause()
            if (cause != null) {
                continuation.resumeWithException(cause)
            } else {
                continuation.resume(Unit)
            }
        }
    }
}