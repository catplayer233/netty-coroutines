package org.catplayer.netty.coroutines.shared

import org.slf4j.Logger
import org.slf4j.LoggerFactory

val Int.kb: Int
    get() = this * 1024

val Int.mb: Int
    get() = kb * 1024


inline fun <reified T> Logger(): Logger {
    return LoggerFactory.getLogger(T::class.java)
}