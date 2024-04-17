package org.catplayer.netty.coroutines.shared

import kotlinx.serialization.Serializable
import kotlin.reflect.KClass
import kotlin.reflect.full.superclasses
import kotlin.reflect.jvm.jvmErasure

@Serializable
data class Message(val action: Int, val detail: String?)

abstract class MessageAction<T>(val code: Int) {

    val type: KClass<*> = this::class.superclasses[0].typeParameters[0].upperBounds[0].jvmErasure

    override fun toString(): String {
        return "MessageAction(code=$code, type=$type)"
    }
}


//actual messages

@Serializable
data class Ping(val name: String) {
    companion object : MessageAction<Ping>(1)
}

@Serializable
data class Pong(val welcome: String) {
    companion object : MessageAction<Pong>(2)
}
