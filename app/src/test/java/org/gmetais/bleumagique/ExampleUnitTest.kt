package org.gmetais.bleumagique

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

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
    fun byteConversion() {
        assertEquals(16, "10".toByteArray()[0].toInt())
        assertEquals(127, "7F".toByteArray()[0].toUInt())
        assertEquals(128, "80".toByteArray()[0].toUInt())
        assertEquals(129, "81".toByteArray()[0].toUInt())
        assertEquals(255, "FF".toByteArray()[0].toUInt())
    }

    @Test
    fun time() {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val year = (calendar.get(Calendar.YEAR) - 2000)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        assertEquals(18, year)
        assertEquals(4, month)
        assertEquals(29, day)
        assertEquals(18, hour)
        assertEquals(3, dayOfWeek)
    }
}
