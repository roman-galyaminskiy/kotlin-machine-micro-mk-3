package example

import javax.sound.midi.MidiDevice
import javax.sound.midi.MidiSystem


fun main() {
    HidAdapter().use {
        it.start()
        val mikroMk3 = MikroMk3HidDevice(it.getHidDevice(0x17cc, 0x1700))
        mikroMk3.open()
        val ui = UiClass(mikroMk3)

        while (true) {
            mikroMk3.listen()
        }
    }
}

