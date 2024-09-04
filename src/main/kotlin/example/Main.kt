package example

fun main() {
    HidAdapter().use {
        it.start()
        val mikroMk3 = MikroMk3HidDevice(it.getHidDevice(0x17cc, 0x1700))
        println(mikroMk3)
        mikroMk3.open()

        mikroMk3.setButtonLightOn(Button.PATTERN)
        mikroMk3.writeStatus()

        mikroMk3.set(1, 1, true)
        // mikroMk3.reset()
        mikroMk3.write()
        // writeDigit(mikroMk3, 0, 0, 1, 4)
        // mikroMk3.writeStatus()
        // val ui = UiClass(mikroMk3)
        //
        // while (true) {
        //     mikroMk3.listen()
        // }
    }
}
