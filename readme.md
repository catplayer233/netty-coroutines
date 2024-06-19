# Netty Coroutines

This is a very simple framework for building a tcp server and client connected via json on top of Netty.
This framework using kotlin coroutines as the logic execute environment,
so you can write all callbacks as normal sync code flow which is very easy for us to develop backend application.

## Modules

- shared

  core code, dto define between server and client
- server

    the server part: listen a port
- client

    connect to the server, acquire or operate as you want

## Usage

### DTO
- client
```kotlin
//ping from client
@Serializable
data class Ping(val name: String) {
    companion object : MessageAction<Ping> by MessageAction(1)
}
```
- server
```kotlin
//pong from server
@Serializable
data class Pong(val welcome: String) {
    companion object : MessageAction<Pong> by MessageAction(2)
}
```

### server

```kotlin
//start listen port: 8080
bootstrap4Server(8080) {
    //register a handle for incoming Ping message
    register(Ping) {
        val delayMills = Random(System.currentTimeMillis()).nextLong(1, 10) * 1000
        LOGGER.info("received ping message: [{}], we delay [{}] mills", it, delayMills)
        delay(delayMills)
        //response a Ping message
        writeAndFlushSuspend(Pong) {
            Pong("welcome! ${it.name}")
        }
    }
}
```

### client

```kotlin
//connect to localhost with port 8080
val socketChannel = bootstrap4Client("127.0.0.1", 8080) {
    // register a handle for incoming Pong message
    register(Pong) {
        LOGGER.info("get the response from server: [${it.welcome}]")
    }
}

//try to send 10 Ping messages to server
runBlocking {
    (1..10).forEach {
        socketChannel.writeAndFlushSuspend(Ping) {
            Ping("$it")
        }
    }
}
```