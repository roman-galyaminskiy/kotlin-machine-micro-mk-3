package example

import javax.sound.midi.*

const val DEVICE_NAME = "DOREMiDi-FFE7"

class UiClass(private val mikroMk3: MikroMk3HidDevice) {
    val vc = VolcaSample()
    val midiDeviceInfos: Array<MidiDevice.Info> = MidiSystem.getMidiDeviceInfo()
    val midiOutDevice = MidiSystem.getMidiDevice(midiDeviceInfos.firstOrNull {
        it.name == DEVICE_NAME
                && it.javaClass.name == "com.sun.media.sound.MidiOutDeviceProvider\$MidiOutDeviceInfo"
    })

    private val padIndexToNumber = mapOf(
        0 to 12,
        1 to 13,
        2 to 14,
        3 to 15,
        4 to 8,
        5 to 9,
        6 to 10,
        7 to 11,
        8 to 4,
        9 to 5,
        10 to 6,
        11 to 7,
        12 to 0,
        13 to 1,
        14 to 2,
        15 to 3
    )

    enum class Mode {
        PAD,
        MAPPING,
        PARAMETERS,
    }

    enum class MappingState {
        SELECT_PAD,
        SELECT_VOICE,
        COPY_FROM,
        COPY_TO
    }

    enum class ParametersState {
        SELECT_PAD,
        PARAMETERS,
        SELECT_FX,
    }

    val parametersOrder = VolcaSampleVoice.Parameters.entries.associateBy { it.ordinal }

    private var mode = Mode.PAD
    private var mappingPadSelected = 0
    private var padToVoiceMapping = Array(16) { 0 }
    private var mappingCopyFrom: Int? = null
    private var mappingCopyTo: Int? = null
    private var mappingState = MappingState.SELECT_PAD

    private var parametersState = ParametersState.SELECT_PAD
    private var parametersActivePad = 0
    private var parametersSelectedParameter = 0
    private var parametersSelectedFxParameter: Int? = null

    private var pressedPads = Array(16) { false }
    private lateinit var receiver: Receiver

    init {
        if (!(midiOutDevice.isOpen)) {
            try {
                midiOutDevice.open()
                receiver = midiOutDevice.receiver
            } catch (e: MidiUnavailableException) {
                println(e)
            }
        }

        mikroMk3.registerButtonPressHandler(Button.PAD_MODE) { switchMode(Mode.PAD) }
        mikroMk3.registerButtonPressHandler(Button.SCENE) { switchMode(Mode.MAPPING) }
        mikroMk3.registerButtonPressHandler(Button.PATTERN) { switchMode(Mode.PARAMETERS) }
        mikroMk3.registerButtonPressHandler(Button.SELECT) { handleSelectButtonPress() }
        mikroMk3.registerPadEventHandler(PadEventType.PAD_PRESSED) { x -> handlePadPress(x.id, x.velocity) }
        mikroMk3.registerPadEventHandler(PadEventType.PAD_RELEASED) { x -> handlePadRelease(x.id) }
        mikroMk3.registerEncoderHandler { x -> handleEncoder(x) }
        display()
    }

    private fun handleSelectButtonPress() {
        if (mode == Mode.MAPPING && mappingState == MappingState.SELECT_PAD) {
            mappingState = MappingState.COPY_FROM
            mikroMk3.setButtonLightOn(Button.SELECT)
            display()
        } else if (mode == Mode.PARAMETERS) {
            parametersState = ParametersState.SELECT_FX
            display()
        }
    }

    private fun handlePadPress(id: Int, velocity: Int?) {
        println("handlePadPress $id $velocity")
        when (mode) {
            Mode.MAPPING -> {
                when (mappingState) {
                    MappingState.SELECT_PAD -> {
                        mappingPadSelected = id
                        display()
                        mappingState = MappingState.SELECT_VOICE
                        Thread.sleep(100)
                        display()
                    }

                    MappingState.SELECT_VOICE -> {
                        padToVoiceMapping[mappingPadSelected] = padIndexToNumber[id]!!
                        println(padToVoiceMapping.joinToString(" "))
                        val myMsg = ShortMessage()
                        myMsg.setMessage(ShortMessage.NOTE_ON, padIndexToNumber[id]!!, 42, 127);
                        receiver.send(myMsg, -1)
                        display()
                        mappingState = MappingState.SELECT_PAD
                        Thread.sleep(100)
                        display()
                    }

                    MappingState.COPY_FROM -> {
                        mappingCopyFrom = id
                        mappingState = MappingState.COPY_TO
                        display()
                    }

                    MappingState.COPY_TO -> {
                        mappingCopyTo = id
                        display()
                        Thread.sleep(100)
                        mappingState = MappingState.SELECT_PAD
                        display()
                    }
                }
            }

            Mode.PARAMETERS -> {
                when (parametersState) {
                    ParametersState.SELECT_PAD -> {
                        parametersActivePad = id
                        display()
                        parametersState = ParametersState.PARAMETERS
                        Thread.sleep(100)
                        display()
                    }

                    ParametersState.PARAMETERS -> {
                        if (id == 12) {
                            parametersState = ParametersState.SELECT_PAD
                            display()
                        } else {
                            parametersSelectedParameter = id
                        }
                        display()
                    }

                    ParametersState.SELECT_FX -> {
                        parametersSelectedFxParameter = id
                        display()
                        Thread.sleep(100)
                        parametersState = ParametersState.SELECT_PAD
                        display()
                    }
                }
            }

            Mode.PAD -> {
                val myMsg = ShortMessage()
                myMsg.setMessage(ShortMessage.NOTE_ON, padToVoiceMapping[id], 42, 127);
                receiver.send(myMsg, -1)
                pressedPads[id] = true
                display()
            }
        }
    }

    private fun handlePadRelease(id: Int) {
        println("handlePadRelease $id")
        if (mode == Mode.PAD) {
            pressedPads[id] = false
            display()
        }
    }

    private fun switchMode(mode: Mode) {
        this.mode = mode
        display()

    }

    private fun display() {
        when (mode) {
            Mode.PAD -> {
                println("PAD MODE")
                mikroMk3.setButtonLightOn(Button.PAD_MODE)
                mikroMk3.setButtonLightOff(Button.SCENE)
                mikroMk3.setButtonLightOff(Button.PATTERN)
                mikroMk3.setButtonLightOff(Button.SELECT)
                val colors = Array(16) { MikroMk3HidDevice.PadColor.OFF }
                for (i in 1..16) {
                    colors[i - 1] = MikroMk3HidDevice.PadColor.entries.toTypedArray()[i]
                }
                for (i in pressedPads.indices) {
                    if (pressedPads[i]) {
                        colors[i] = MikroMk3HidDevice.PadColor.WHITE
                    }
                }
                setPadsLight(colors)
            }

            Mode.MAPPING -> {
                println("MAPPING MODE")
                mikroMk3.setButtonLightOff(Button.PAD_MODE)
                mikroMk3.setButtonLightOn(Button.SCENE)
                mikroMk3.setButtonLightOff(Button.PATTERN)
                mikroMk3.setButtonLightOff(Button.SELECT)

                when (mappingState) {
                    MappingState.SELECT_PAD -> {
                        println("SELECT PAD")
                        val colors = Array(16) { MikroMk3HidDevice.PadColor.YELLOW }
                        colors[mappingPadSelected] = MikroMk3HidDevice.PadColor.WHITE
                        setPadsLight(colors)
                    }

                    MappingState.SELECT_VOICE -> {
                        println("SELECT VOICE")
                        val colors = Array(16) { MikroMk3HidDevice.PadColor.OFF }
                        for (i in 1..10) {
                            colors[padIndexToNumber[i - 1]!!] = MikroMk3HidDevice.PadColor.ORANGE
                        }
                        colors[padIndexToNumber[padToVoiceMapping[mappingPadSelected]]!!] = MikroMk3HidDevice.PadColor.WHITE
                        setPadsLight(colors)
                    }

                    MappingState.COPY_FROM -> {
                        println("COPY FROM")
                        mikroMk3.setButtonLightOn(Button.SELECT)
                        val colors = Array(16) { MikroMk3HidDevice.PadColor.OFF }
                        if (mappingCopyFrom != null) {
                            colors[mappingCopyFrom!!] = MikroMk3HidDevice.PadColor.CYAN
                        } else {
                            colors.fill(MikroMk3HidDevice.PadColor.CYAN)
                        }
                        setPadsLight(colors)
                    }

                    MappingState.COPY_TO -> {
                        println("COPY TO")
                        mikroMk3.setButtonLightOn(Button.SELECT)
                        val colors = Array(16) { MikroMk3HidDevice.PadColor.OFF }
                        colors[mappingCopyFrom!!] = MikroMk3HidDevice.PadColor.CYAN
                        if (mappingCopyTo != null) {
                            colors[mappingCopyTo!!] = MikroMk3HidDevice.PadColor.BLUE
                        }
                        setPadsLight(colors)
                    }
                }
            }

            Mode.PARAMETERS -> {
                println("PARAMETERS MODE")
                mikroMk3.setButtonLightOff(Button.PAD_MODE)
                mikroMk3.setButtonLightOff(Button.SCENE)
                mikroMk3.setButtonLightOn(Button.PATTERN)
                mikroMk3.setButtonLightOff(Button.SELECT)

                when (parametersState) {
                    ParametersState.SELECT_PAD -> {
                        println("SELECT PAD")
                        val colors = Array(16) { MikroMk3HidDevice.PadColor.PLUM }
                        colors[parametersActivePad] = MikroMk3HidDevice.PadColor.WHITE
                        setPadsLight(colors)
                    }

                    ParametersState.PARAMETERS -> {
                        println("PARAMETERS")
                        val colors = Array(16) { MikroMk3HidDevice.PadColor.OFF }
                        for (i in 0..11) {
                            colors[i] = MikroMk3HidDevice.PadColor.PURPLE
                        }
                        colors[12] = MikroMk3HidDevice.PadColor.RED
                        colors[parametersSelectedParameter] = MikroMk3HidDevice.PadColor.WHITE
                        setPadsLight(colors)
                    }

                    ParametersState.SELECT_FX -> {
                        println("SELECT FX")
                        mikroMk3.setButtonLightOn(Button.SELECT)
                        val colors = Array(16) { MikroMk3HidDevice.PadColor.FUCHSIA }
                        if (parametersSelectedFxParameter != null) {
                            colors[parametersSelectedFxParameter!!] = MikroMk3HidDevice.PadColor.WHITE
                        }
                        setPadsLight(colors)
                    }
                }
            }
        }
        mikroMk3.writeStatus()
    }

    private fun handleEncoder(value: Int) {
        if (value != -1 && value != 1) {
            return
        }
        if (mode == Mode.PARAMETERS && parametersState == ParametersState.PARAMETERS) {
            println("Encoder $value")

            val voice = vc.voices[padToVoiceMapping[parametersActivePad]]
            println("voice ${padToVoiceMapping[parametersActivePad]}")
            val param = parametersOrder[parametersSelectedParameter]!!
            println("param ${param.name}")
            val paramValue = voice.parameters[parametersOrder[parametersSelectedParameter]!!.name]!!
            val myMsg = ShortMessage()
            println("chanel ${padToVoiceMapping[parametersActivePad]}, data ${paramValue + value}")
            val newValue = if (paramValue + value > 127) {
                127
            } else if (paramValue + value < 0) {
                0
            } else {
                paramValue + value
            }
            voice.parameters[parametersOrder[parametersSelectedParameter]!!.name] = newValue
            println("chanel ${padToVoiceMapping[parametersActivePad]}, data1 ${param.value} data2 ${newValue}")
            myMsg.setMessage(ShortMessage.CONTROL_CHANGE, padToVoiceMapping[parametersActivePad], param.value, newValue);
            receiver.send(myMsg, -1)
        }

    }

    fun setPadsLight(lights: Array<MikroMk3HidDevice.PadColor>) {
        lights.forEachIndexed { index, light ->
            mikroMk3.setPadLight(index, light, MikroMk3HidDevice.Brightness.BRIGHT)
        }
    }
}