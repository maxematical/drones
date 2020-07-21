package drones.test

import drones.LogOutputStream
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.io.PrintWriter

class TestLog {
    @Test
    fun testSimpleLogging() {
        val logger = LoggerFactory.getLogger("test")
        val out = mutableListOf<String>()
        val logOutputStream = LogOutputStream(logger, Level.INFO, out, 24)
        val printer = PrintWriter(logOutputStream)

        printer.println("Hello, world!")
        printer.flush()
        Assertions.assertEquals(listOf("Hello, world!"), out)
        printer.println("ABC")
        printer.flush()
        Assertions.assertEquals(listOf("Hello, world!", "ABC"), out)

        out.clear()
        printer.print('\n')
        printer.flush()
        Assertions.assertEquals(listOf(""), out)
    }
}
