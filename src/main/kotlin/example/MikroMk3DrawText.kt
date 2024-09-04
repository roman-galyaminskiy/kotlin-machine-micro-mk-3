package example

fun main() {
    HidAdapter().use {
        it.start()
        val mikroMk3 = MikroMk3HidDevice(it.getHidDevice(0x17cc, 0x1700))
        println(mikroMk3)
        mikroMk3.open()
        mikroMk3.setButtonLightOn(Button.PATTERN)
        println("opened")
        mikroMk3.reset()
        mikroMk3.set(1, 1, true)
        mikroMk3.write()
        // mikroMk3.close()
    }
}
