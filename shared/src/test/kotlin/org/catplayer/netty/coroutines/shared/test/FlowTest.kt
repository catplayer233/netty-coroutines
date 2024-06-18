package org.catplayer.netty.coroutines.shared.test

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import kotlin.test.Test

class FlowTest {

    @Test
    fun `test flow`() {

        runBlocking {
            flowOf(1, 2, 3, 4, 5).collect {
                delay(1000)
                LOGGER.info("value: [{}]", it)
            }
        }

    }


    companion object {
        private val LOGGER = LoggerFactory.getLogger(FlowTest::class.java)
    }
}