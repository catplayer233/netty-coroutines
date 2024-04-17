package org.catplayer.netty.coroutines.client

import kotlinx.serialization.serializer
import org.catplayer.netty.coroutines.shared.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val LOGGER: Logger = LoggerFactory.getLogger("org.catplayer.netty.coroutines.client.Starter")

fun main() {
    val socketChannel = bootstrap4Client("127.0.0.1", 8080) {
        register(Pong) {
            LOGGER.info("get the response from server: [${it.welcome}]")
        }
    }

    socketChannel.writeAndFlush(Message(Ping.code, businessJSON.encodeToString(serializer(), Ping("catplayer"))))
}