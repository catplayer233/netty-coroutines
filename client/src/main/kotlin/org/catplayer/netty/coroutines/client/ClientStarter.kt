package org.catplayer.netty.coroutines.client

import org.catplayer.netty.coroutines.shared.Ping
import org.catplayer.netty.coroutines.shared.Pong
import org.catplayer.netty.coroutines.shared.bootstrap4Client
import org.catplayer.netty.coroutines.shared.writeSuspend
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val LOGGER: Logger = LoggerFactory.getLogger("org.catplayer.netty.coroutines.client.Starter")

suspend fun main() {
    val socketChannel = bootstrap4Client("127.0.0.1", 8080) {
        register(Pong) {
            LOGGER.info("get the response from server: [${it.welcome}]")
        }
    }

    socketChannel.writeSuspend(Ping, Ping("catplayer"))

}