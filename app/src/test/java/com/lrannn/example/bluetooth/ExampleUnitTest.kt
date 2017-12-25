package com.lrannn.example.bluetooth

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        test()
        assertEquals(4, 2 + 2)
    }


    fun test() {
        val a = 0x01
        val b = 0x02
        val c = 0x04
        val d = a or c or b

        print(d)

    }
}
