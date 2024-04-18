package org.catplayer.netty.coroutines.shared

import io.netty.bootstrap.Bootstrap
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val LOGGER: Logger = LoggerFactory.getLogger("org.catplayer.shared.Bootstrap")

fun bootstrap4Server(
    port: Int,
    route: MessageHandlerDispatcherBuilder.(MessageHandlerDispatcherBuilder) -> Unit
) {

    ServerBootstrap()
        .group(NioEventLoopGroup(1), NioEventLoopGroup())
        .channel(NioServerSocketChannel::class.java)
        .option(ChannelOption.SO_BACKLOG, 128)
        .childOption(ChannelOption.SO_KEEPALIVE, true)
        .childOption(ChannelOption.TCP_NODELAY, true)
        .childHandler(object : ChannelInitializer<SocketChannel>() {

            override fun initChannel(ch: SocketChannel) {

                //read staff
                ch.pipeline().addLast(
                    LengthFieldBasedFrameDecoder(4.kb, 0, 4, 0, 0),
                    MessageDecoder,
                    MessageHandlerDispatcherBuilder.build { route(this) },
                )

                //write staff
                ch.pipeline().addFirst(MessageEncoder)
            }
        })
        .bind(port)
        .addListener {
            LOGGER.info("started server with port $port")
        }
        .sync()
        .channel()

}


fun bootstrap4Client(
    host: String,
    port: Int,
    route: MessageHandlerDispatcherBuilder.(MessageHandlerDispatcherBuilder) -> Unit
): SocketChannel {
    return Bootstrap()
        .group(NioEventLoopGroup(1))
        .channel(NioSocketChannel::class.java)
        .option(ChannelOption.SO_KEEPALIVE, true)
        .option(ChannelOption.TCP_NODELAY, true)
        .handler(object : ChannelInitializer<SocketChannel>() {
            override fun initChannel(ch: SocketChannel) {
                //read staff
                ch.pipeline().addLast(
                    LengthFieldBasedFrameDecoder(4.kb, 0, 4, 0, 0),
                    MessageDecoder,
                    MessageHandlerDispatcherBuilder.build { route(this) },
                )

                //write staff
                ch.pipeline().addLast(MessageEncoder)
            }
        })
        .connect(host, port)
        .addListener {
            LOGGER.info("connected server with port $port success")
        }
        .sync()
        .channel() as SocketChannel
}