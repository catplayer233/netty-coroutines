package org.catplayer.netty.coroutines.shared.test

import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertSame

class KtReflectionTest {

    @Test
    fun companionTest() {
        assertSame(KotlinClass::class, KotlinClass.targetClass)
    }
}


class KotlinClass {

    companion object {
        val targetClass: KClass<*>
            get() = Companion::class.java.enclosingClass.kotlin
    }

}