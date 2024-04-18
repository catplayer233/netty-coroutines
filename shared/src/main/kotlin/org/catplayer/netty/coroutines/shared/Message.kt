package org.catplayer.netty.coroutines.shared

import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Serializable
data class Message(val action: Int, val detail: String?)

interface MessageAction<T : Any> {

    val code: Int

    val type: KClass<T>
}

inline fun <reified T : Any> MessageAction(code: Int): MessageAction<T> {
    return object : MessageAction<T> {
        override val code: Int
            get() = code
        override val type: KClass<T>
            get() = T::class
    }
}


//actual messages

@Serializable
data class Ping(val name: String) {
    companion object : MessageAction<Ping> by MessageAction(1)
}

@Serializable
data class Pong(val welcome: String) {
    companion object : MessageAction<Pong> by MessageAction(2)
}
