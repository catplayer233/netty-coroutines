package org.catplayer.netty.coroutines.shared

import org.slf4j.Logger
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


@OptIn(ExperimentalContracts::class)
inline fun Logger.debugOp(op: Logger.() -> Unit) {
    contract {
        callsInPlace(op, InvocationKind.AT_MOST_ONCE)
    }

    if (isDebugEnabled) {
        op()
    }
}