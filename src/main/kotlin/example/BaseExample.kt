/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2020 Gary Rowe
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package example

import org.hid4java.HidServices
import org.hid4java.HidServicesListener
import org.hid4java.event.HidServicesEvent
import java.util.concurrent.TimeUnit

abstract class BaseExample : HidServicesListener {
    fun printPlatform() {
        // System info to assist with library detection

        println("Platform architecture: " + com.sun.jna.Platform.ARCH)
        println("Resource prefix: " + com.sun.jna.Platform.RESOURCE_PREFIX)
        println("Libusb activation: " + com.sun.jna.Platform.isLinux())
    }

    fun waitAndShutdown(hidServices: HidServices) {
        System.out.printf(ANSI_YELLOW + "Waiting 30s to demonstrate attach/detach handling. Watch for slow response after write if configured.%n" + ANSI_RESET)

        // Stop the main thread to demonstrate attach and detach events
        sleepNoInterruption()

        // Shut down and rely on auto-shutdown hook to clear HidApi resources
        System.out.printf(ANSI_YELLOW + "Triggering shutdown...%n" + ANSI_RESET)
        hidServices.shutdown()
    }

    override fun hidDeviceAttached(event: HidServicesEvent) {
        println(ANSI_BLUE + "Device attached: " + event + ANSI_RESET)
    }

    override fun hidDeviceDetached(event: HidServicesEvent) {
        println(ANSI_YELLOW + "Device detached: " + event + ANSI_RESET)
    }

    override fun hidFailure(event: HidServicesEvent) {
        println(ANSI_RED + "HID failure: " + event + ANSI_RESET)
    }

    override fun hidDataReceived(event: HidServicesEvent) {
        System.out.printf(ANSI_PURPLE + "Data received:%n")
        val dataReceived: ByteArray = event.getDataReceived()

        printAsHex(dataReceived)
    }

    companion object {
        const val ANSI_RESET: String = "\u001B[0m"
        const val ANSI_BLACK: String = "\u001B[30m"
        const val ANSI_RED: String = "\u001B[31m"
        const val ANSI_GREEN: String = "\u001B[32m"
        const val ANSI_YELLOW: String = "\u001B[33m"
        const val ANSI_BLUE: String = "\u001B[34m"
        const val ANSI_PURPLE: String = "\u001B[35m"
        const val ANSI_CYAN: String = "\u001B[36m"
        const val ANSI_WHITE: String = "\u001B[37m"

        fun printAsHex(dataReceived: ByteArray) {
            System.out.printf("< [%02x]:", dataReceived.size)
            for (b in dataReceived) {
                System.out.printf(" %02x", b)
            }
            println(ANSI_RESET)
        }

        /**
         * Invokes `unit.`[sleep(sleepFor)][TimeUnit.sleep]
         * uninterruptibly.
         */
        fun sleepNoInterruption() {
            var interrupted = false
            try {
                var remainingNanos: Long = TimeUnit.SECONDS.toNanos(30)
                val end: Long = System.nanoTime() + remainingNanos
                while (true) {
                    try {
                        // TimeUnit.sleep() treats negative timeouts just like zero.
                        TimeUnit.NANOSECONDS.sleep(remainingNanos)
                        return
                    } catch (e: InterruptedException) {
                        interrupted = true
                        remainingNanos = end - System.nanoTime()
                    }
                }
            } finally {
                if (interrupted) {
                    Thread.currentThread().interrupt()
                }
            }
        }
    }
}