package example

class VolcaSampleVoice
// (
// val sample: Int = 0,
// val startPoint: Int = 0,
// val length: Int = 127,
// val highCut: Int = 127,
// val speed: Int = 64,
// val XXX: Int = 64,
// val pitchAttack: Int = 64,
// val pitchDecay: Int = 127,
// val level: Int = 127,
// val pan: Int = 64,
// val amplitudeAttack: Int = 0,
// val amplitudeDecay: Int = 127
// )
{
    enum class Parameters(val value: Int) {
        LEVEL(7),
        PAN(10),
        SAMPLE_START_POINT(40),
        SAMPLE_LENGTH(41),
        HI_CUT(42),
        SPEED(43),
        PITCH_INT(44),
        PITCH_ATTACK(45),
        PITCH_DECAY(46),
        AMPLITUDE_ATTACK(47),
        AMPLITUDE_DECAY(48)
    }

    val parameters: MutableMap<String, Int> = mutableMapOf(
        "SAMPLE" to 0,
        "SAMPLE_START_POINT" to 0,
        "SAMPLE_LENGTH" to 127,
        "HI_CUT" to 127,
        "SPEED" to 64,
        "PITCH_INT" to 64,
        "PITCH_ATTACK" to 64,
        "PITCH_DECAY" to 127,
        "LEVEL" to 127,
        "PAN" to 64,
        "AMPLITUDE_ATTACK" to 0,
        "AMPLITUDE_DECAY" to 127
    )
}