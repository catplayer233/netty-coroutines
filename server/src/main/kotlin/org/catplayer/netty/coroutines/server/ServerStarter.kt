package org.catplayer.netty.coroutines.server

import org.catplayer.netty.coroutines.shared.Ping
import org.catplayer.netty.coroutines.shared.Pong
import org.catplayer.netty.coroutines.shared.bootstrap4Server
import org.catplayer.netty.coroutines.shared.writeSuspend


fun main() {

    bootstrap4Server(8080) {
        register(Ping) {
            writeSuspend(Pong, Pong("welcome! ${it.name}"))
        }

    }
}