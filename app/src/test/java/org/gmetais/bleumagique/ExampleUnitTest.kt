package org.gmetais.bleumagique

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun bytConversion() {
        assertEquals(16, "10".toByteArray()[0].toInt())
        assertEquals(127, "7F".toByteArray()[0].toUInt())
        assertEquals(128, "80".toByteArray()[0].toUInt())
        assertEquals(129, "81".toByteArray()[0].toUInt())
        assertEquals(255, "FF".toByteArray()[0].toUInt())
    }
}
