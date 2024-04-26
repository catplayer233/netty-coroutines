package org.catplayer.netty.coroutines.server

import org.catplayer.netty.coroutines.shared.Ping
import org.catplayer.netty.coroutines.shared.Pong
import org.catplayer.netty.coroutines.shared.bootstrap4Server
import org.catplayer.netty.coroutines.shared.writeAndFlushSuspend
import org.slf4j.Logger
import org.slf4j.LoggerFactory


private val LOGGER: Logger = LoggerFactory.getLogger("org.catplayer.netty.coroutines.server.ServerStarter")

fun main() {

    bootstrap4Server(8080) {
        register(Ping) {
            LOGGER.info("received ping message: [{}]", it)
            writeAndFlushSuspend(Pong) {
                Pong("welcome! ${it.name}")
            }
        }

    }
}