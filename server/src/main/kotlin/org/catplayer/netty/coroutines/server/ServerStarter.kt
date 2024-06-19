package org.catplayer.netty.coroutines.server

import kotlinx.coroutines.delay
import org.catplayer.netty.coroutines.shared.Ping
import org.catplayer.netty.coroutines.shared.Pong
import org.catplayer.netty.coroutines.shared.bootstrap4Server
import org.catplayer.netty.coroutines.shared.writeAndFlushSuspend
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.random.Random


private val LOGGER: Logger = LoggerFactory.getLogger("org.catplayer.netty.coroutines.server.ServerStarter")

fun main() {

    bootstrap4Server(8080) {
        register(Ping) {
            val delayMills = Random(System.currentTimeMillis()).nextLong(1, 10) * 1000
            LOGGER.info("received ping message: [{}], we delay [{}] mills", it, delayMills)
            delay(delayMills)
            writeAndFlushSuspend(Pong) {
                Pong("welcome! ${it.name}")
            }
        }
    }
}