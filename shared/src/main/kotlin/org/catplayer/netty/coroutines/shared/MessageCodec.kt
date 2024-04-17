package org.catplayer.netty.coroutines.shared

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import io.netty.handler.codec.MessageToMessageDecoder
import kotlinx.serialization.serializer

@Sharable
object MessageDecoder : MessageToMessageDecoder<ByteBuf>() {

    override fun decode(ctx: ChannelHandlerContext, msg: ByteBuf, out: MutableList<Any>) {

        val bytes = msg.array()

        val message = businessJSON.decodeFromString<Message>(String(bytes))

        out.add(message)
    }
}

@Sharable
object MessageEncoder : MessageToByteEncoder<Message>() {

    override fun encode(ctx: ChannelHandlerContext, msg: Message, out: ByteBuf) {
        val jsonBytes = businessJSON.encodeToString(serializer(), msg).toByteArray()
        out.writeInt(jsonBytes.size)
        out.writeBytes(jsonBytes)
    }
}