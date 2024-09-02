package example

import javax.sound.midi.MidiDevice
import javax.sound.midi.MidiSystem
import javax.sound.midi.SysexMessage

fun main() {
    val midiDeviceInfos: Array<MidiDevice.Info> = MidiSystem.getMidiDeviceInfo()
    val midiOutDevice =
        MidiSystem.getMidiDevice(
            midiDeviceInfos.firstOrNull {
                it.name == DEVICE_NAME &&
                    it.javaClass.name == "com.sun.media.sound.MidiOutDeviceProvider\$MidiOutDeviceInfo"
            },
        )

    val data =
        byteArrayOf(
            0xf0.toByte(),
            0x43, 0x00, 0x00, 0x01, 0x1b,
            99,95,90,90, 99,90,80,0, 50,50,50, 3, 3, 1, 1,0,99,0 ,1,0,7,
            99,95,90,90, 99,90,80,0, 50,50,50, 3, 3, 1, 1,0,99,0 ,1,0,7,
            99,95,90,90, 99,90,80,0, 50,50,50, 3, 3, 1, 1,0,99,0 ,1,0,7,
            99,95,90,90, 99,90,80,0, 50,50,50, 3, 3, 1, 1,0,99,0 ,1,0,7,
            99,95,90,90, 99,90,80,0, 50,50,50, 3, 3, 1, 1,0,99,0 ,1,0,7,
            99,95,90,90, 99,90,80,0, 50,50,50, 3, 3, 1, 1,0,99,0 ,1,0,7,
            0,0,0,0, 50,50,50,50, 2,3,0,0,0,0,0,0,0,0,24,
            'n'.toByte(),'e'.toByte(),'w'.toByte(),'p'.toByte(),'a'.toByte(),'t'.toByte(),'c'.toByte(),'h'.toByte(),'2'.toByte(),' '.toByte(),
            0x3f,
            0xf7.toByte(),
        )

    val sysex = SysexMessage()
    sysex.setMessage(data, data.size)

    if (!(midiOutDevice.isOpen)) {
        midiOutDevice.open()
        midiOutDevice.use {
            println("Device opened, sending sysex")
            it.receiver.send(sysex, -1)
            println("Sent sysex")
        }
    }
    println(data)

}
