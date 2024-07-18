package org.catplayer.netty.coroutines.shared

import io.netty.bootstrap.Bootstrap
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.ServerChannel
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.epoll.EpollSocketChannel
import io.netty.channel.kqueue.KQueue
import io.netty.channel.kqueue.KQueueEventLoopGroup
import io.netty.channel.kqueue.KQueueServerSocketChannel
import io.netty.channel.kqueue.KQueueSocketChannel
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

    val (parentEventLoopGroup, childEventLoopGroup) = inferEventLoopGroup4Server()

    val serverChannelType = inferServerChannelType()

    ServerBootstrap()
        .group(parentEventLoopGroup, childEventLoopGroup)
        .channel(serverChannelType)
        .option(ChannelOption.SO_BACKLOG, 128)
        .childOption(ChannelOption.SO_KEEPALIVE, true)
        .childOption(ChannelOption.TCP_NODELAY, true)
        .childHandler(object : ChannelInitializer<SocketChannel>() {
            override fun initChannel(ch: SocketChannel) {
                ch
                    .pipeline()
                    .addLast(LengthFieldBasedFrameDecoder(4.kb, 0, 4, 0, 0))
                    .addLast(MessageDecoder)
                    .addLast(MessageHandlerDispatcherBuilder.build { route(this) })
                    .addFirst(MessageEncoder)//write, the message write maybe happened before dispatcher
            }
        })
        .bind(port)
        .addListener {
            LOGGER.info(
                "started server with port: [{}], server channel type: [{}], event loop group:  parent: [{}], child: [{}]",
                port,
                serverChannelType.simpleName,
                parentEventLoopGroup::class.java.simpleName,
                childEventLoopGroup::class.java.simpleName
            )
        }
        .sync()
        .channel()

}


fun bootstrap4Client(
    host: String,
    port: Int,
    route: MessageHandlerDispatcherBuilder.(MessageHandlerDispatcherBuilder) -> Unit
): SocketChannel {

    val eventLoopGroup = inferEventLoopGroup4Client()
    val socketChannelType = inferSocketChannelType()
    return Bootstrap()
        .group(eventLoopGroup)
        .channel(socketChannelType)
        .option(ChannelOption.SO_KEEPALIVE, true)
        .option(ChannelOption.TCP_NODELAY, true)
        .handler(object : ChannelInitializer<SocketChannel>() {
            override fun initChannel(ch: SocketChannel) {
                ch.pipeline()
                    .addLast(LengthFieldBasedFrameDecoder(4.kb, 0, 4, 0, 0))
                    .addLast(MessageDecoder)
                    .addLast(MessageHandlerDispatcherBuilder.build { route(this) })
                    .addFirst(MessageEncoder)
            }
        })
        .connect(host, port)
        .addListener {
            LOGGER.info(
                "connected server: [{}] success, event loop group: [{}], socket channel: [{}]",
                "$host:$port",
                eventLoopGroup::class.java.simpleName,
                socketChannelType.simpleName
            )
        }
        .sync()
        .channel() as SocketChannel
}

/**
 * infer the specific event loop group via the environment for server
 *
 * @return first: parent, second: child
 */
private fun inferEventLoopGroup4Server(): Pair<EventLoopGroup, EventLoopGroup> {
    return when {
        Epoll.isAvailable() -> EpollEventLoopGroup(1) to EpollEventLoopGroup()
        KQueue.isAvailable() -> KQueueEventLoopGroup(1) to KQueueEventLoopGroup()
        else -> NioEventLoopGroup(1) to NioEventLoopGroup()
    }
}

private fun inferServerChannelType(): Class<out ServerChannel> {
    return when {
        Epoll.isAvailable() -> EpollServerSocketChannel::class.java
        KQueue.isAvailable() -> KQueueServerSocketChannel::class.java
        else -> NioServerSocketChannel::class.java
    }
}

private fun inferSocketChannelType(): Class<out SocketChannel> {
    return when {
        Epoll.isAvailable() -> EpollSocketChannel::class.java
        KQueue.isAvailable() -> KQueueSocketChannel::class.java
        else -> NioSocketChannel::class.java
    }
}

private fun inferEventLoopGroup4Client(): EventLoopGroup {
    return when {
        Epoll.isAvailable() -> EpollEventLoopGroup(1)
        KQueue.isAvailable() -> KQueueEventLoopGroup(1)
        else -> NioEventLoopGroup(1)
    }
}