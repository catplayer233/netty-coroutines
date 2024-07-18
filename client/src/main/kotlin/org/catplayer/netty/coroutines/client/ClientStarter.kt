package org.catplayer.netty.coroutines.client

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.runBlocking
import org.catplayer.netty.coroutines.shared.Ping
import org.catplayer.netty.coroutines.shared.Pong
import org.catplayer.netty.coroutines.shared.bootstrap4Client
import org.catplayer.netty.coroutines.shared.writeAndFlushSuspend
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

private val LOGGER: Logger = LoggerFactory.getLogger("org.catplayer.netty.coroutines.client.Starter")

fun main() {

    val times = 3

    val count = atomic(0)

    val socketChannel = bootstrap4Client("127.0.0.1", 8080) {
        register(Pong) {
            LOGGER.info("get the response from server: [${it.welcome}]")
            count += 1

            if (count.value == times) {
                channel().close().addListener { future ->

                    val exitCode = if (future.isSuccess) 0 else -1

                    exitProcess(exitCode)
                }
            }
        }
    }

    runBlocking {
        repeat(times) {
            socketChannel.writeAndFlushSuspend(Ping) {
                Ping("$it")
            }
        }
    }

}