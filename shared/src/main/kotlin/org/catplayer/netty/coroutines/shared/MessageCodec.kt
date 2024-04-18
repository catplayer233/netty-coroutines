package org.catplayer.netty.coroutines.shared

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import io.netty.handler.codec.MessageToMessageDecoder
import kotlinx.serialization.serializer
import java.io.ByteArrayOutputStream

@Sharable
object MessageDecoder : MessageToMessageDecoder<ByteBuf>() {

    override fun decode(ctx: ChannelHandlerContext, msg: ByteBuf, out: MutableList<Any>) {
        val jsonBytesSize = msg.readInt()

        check(jsonBytesSize > 0) {
            "json message can't be empty"
        }

        val jsonBytes = ByteArrayOutputStream(jsonBytesSize)
            .use {
                msg.readBytes(it, jsonBytesSize)

                it.toByteArray()
            }


        val message = businessJSON.decodeFromString<Message>(String(jsonBytes))

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