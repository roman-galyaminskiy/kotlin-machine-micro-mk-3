package example

import org.hid4java.*
import org.w3c.dom.events.Event
import java.io.Closeable


class HidAdapter : BaseExample(), Closeable {
    private val hidServices = HidManager.getHidServices()

    init {
        hidServices.addHidServicesListener(this)
    }

    fun start() {
        println(ANSI_GREEN + "Manually starting HID services." + ANSI_RESET)
        hidServices.start()
        println(ANSI_GREEN + "Enumerating attached devices..." + ANSI_RESET)
    }


    fun getHidDevice(vendorId: Int, productId: Int): HidDevice {
        return hidServices.getHidDevice(vendorId, productId, null)
    }

    override fun close() {
        waitAndShutdown(hidServices)
    }
}