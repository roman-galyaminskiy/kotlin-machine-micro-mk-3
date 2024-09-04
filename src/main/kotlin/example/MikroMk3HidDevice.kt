package example

import org.hid4java.HidDevice
import java.io.Closeable
import java.time.Instant
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or

const val REPORT_ID = 0x80.toByte()

class MikroMk3HidDevice(
    private val device: HidDevice,
) : Closeable {
    enum class Brightness(
        val value: Byte,
    ) {
        OFF(0x00),
        DIM(0x7c),
        NORMAL(0x7e),
        BRIGHT(0x7f),
    }

    enum class PadColor(
        val value: Int,
    ) {
        OFF(0),
        RED(1),
        ORANGE(2),
        LIGHT_ORANGE(3),
        WARM_YELLOW(4),
        YELLOW(5),
        LIME(6),
        GREEN(7),
        MINT(8),
        CYAN(9),
        TURQUOISE(10),
        BLUE(11),
        PLUM(12),
        VIOLET(13),
        PURPLE(14),
        MAGENTA(15),
        FUCHSIA(16),
        WHITE(17),
    }

    private val buffer = ByteArray(512) { 0xff.toByte() }

    // private val eventHandlers: MutableMap<HidEventType, (HidEvent) -> Unit> = mutableMapOf()

    private val buttonPressHandlers: MutableMap<Button, () -> Unit> = mutableMapOf()

    fun registerButtonPressHandler(
        button: Button,
        handler: () -> Unit,
    ) {
        buttonPressHandlers[button] = handler
    }

    private val padEventHandlers: MutableMap<PadEventType, (PadEvent) -> Unit> = mutableMapOf()

    fun registerPadEventHandler(
        eventType: PadEventType,
        handler: (PadEvent) -> Unit,
    ) {
        padEventHandlers[eventType] = handler
    }

    private var encoderHandler: ((Int) -> Unit)? = null

    fun registerEncoderHandler(handler: (diff: Int) -> Unit) {
        encoderHandler = handler
    }

    private var lastEncoderUpdate: Instant? = null

    private val status = ByteArray(80)

    private var prevEncoderValue: Int? = null

    fun open() {
        device.open()
        device.setNonBlocking(true)
    }

    fun listen() {
        while (true) {
            val buffer = device.read().toByteArray()

            if (buffer.isNotEmpty()) {
                if (buffer[0] == 0x01.toByte()) {
                    // button mode
                    // println(buffer.joinToString(" ") { it.toString(16)})
                    label@for (i in 0..6) {
                        // bytes
                        for (j in 0..8) { // 8
                            // bits
                            val idx = i * 8 + j
                            val button = Button.entries.getOrNull(idx)

                            if (button != null) {
                                val status = buffer[i + 1].toInt() and (1 shl j) > 0
                                if (status) {
                                    // println(button)

                                    buttonPressHandlers[button]?.let { it() }

                                    if (button == Button.ENCODER_TOUCH) {
                                        val encoderVal = buffer[7]

                                        var diff: Int? = null

                                        if (prevEncoderValue != null) {
                                            diff = encoderVal.toInt() - prevEncoderValue!!
                                            prevEncoderValue = encoderVal.toInt()
                                            // if (diff > 0) {
                                            //     println("Encoder: +$diff")
                                            // } else if (diff < 0) {
                                            //     println("Encoder: $diff")
                                            // }
                                        } else {
                                            prevEncoderValue = encoderVal.toInt()
                                        }

                                        if (lastEncoderUpdate != null) {
                                            val now = Instant.now()
                                            if (now.toEpochMilli() - lastEncoderUpdate!!.toEpochMilli() > 100) {
                                                encoderHandler?.let { it(diff!!) }
                                                lastEncoderUpdate = null
                                            }
                                        } else {
                                            lastEncoderUpdate = Instant.now()
                                        }

                                        if (prevEncoderValue == 15) {
                                            prevEncoderValue = -1
                                        }
                                    }
                                    continue@label
                                }
                            }
                        }
                    }
                } else if (buffer[0] == 0x02.toByte()) {
                    // pad mode
                    // for (i in (1..buffer.size step 3)) {
                    val index = buffer[1]
                    val eventType = buffer[2].toInt() and 0xf0
                    val value = ((buffer[3].toInt() and 0x0f) shl 8) + buffer[4].toInt()

                    when (eventType) {
                        0x10 -> {
                            println("NoteOn")
                            padEventHandlers[PadEventType.PAD_PRESSED]?.let { it(PadEvent(index.toInt(), value)) }
                        }
                        0x30 -> {
                            println("NoteOff")
                            padEventHandlers[PadEventType.PAD_RELEASED]?.let { it(PadEvent(index.toInt(), value)) }
                        }
                        0x40 -> println("Aftertouch")
                        0x20 -> println("PressOff")
                        0x00 -> println("PressOn")
                        else -> println("Unknown event type")
                    }
                    // }
                }
            }
        }
    }

    override fun close() {
        if (!device.isClosed) {
            device.close()
        }
    }

    fun writeStatus() = device.write(status, status.size, REPORT_ID)

    fun setPadLight(
        id: Int,
        color: PadColor,
        brightness: Brightness,
    ) {
        val value = if (brightness == Brightness.OFF) 0 else (color.value shl 2) + (brightness.value.toInt() and 0b11)
        status[39 + id] = value.toByte()
    }

    fun setButtonLightOn(button: Button) {
        status[button.id] = Brightness.BRIGHT.value
    }

    fun setButtonLightOff(button: Button) {
        status[button.id] = Brightness.OFF.value
    }

    fun set(
        i: Int,
        j: Int,
        value: Boolean,
    ) {
        println(0xff)
        println(0xff.toByte())
        println(buffer.asList())
        val chunk = i / 8
        val imod = (i % 8).toByte()
        println("imod = $imod")
        val idx = chunk * 128 + j
        println("idx = $idx")
        val mask = (1 shl imod.toInt()).toByte()
        println("mask = $mask")
        if (value) {
            buffer[idx] = buffer[idx] and mask.inv()
        } else {
            buffer[idx] = buffer[idx] or mask
        }
        println(buffer.asList())
    }

    fun reset() {
        buffer.fill(0xff.toByte())
    }

    fun write() {
        val hi = HEADER_HI + buffer.copyOfRange(0, 256)
        println(hi.size)
        val lo = HEADER_LO + buffer.copyOfRange(256, 512)
        println(lo.size)
        println(hi.asList())
        println(lo.asList())
        device.write(hi, hi.size, REPORT_ID)
        device.write(lo, lo.size, REPORT_ID)
    }

    companion object {
        private val HEADER_HI = byteArrayOf(0xe0.toByte(), 0x00, 0x00, 0x00, 0x00, 0x80.toByte(), 0x00, 0x02, 0x00)
        private val HEADER_LO = byteArrayOf(0xe0.toByte(), 0x00, 0x00, 0x02, 0x00, 0x80.toByte(), 0x00, 0x02, 0x00)
    }
}
