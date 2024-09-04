package example

// Define the FONT as a 3D array of ByteArray elements
val FONT =
    arrayOf(
        arrayOf(
            "   xxx  ".toByteArray(),
            "  x   x ".toByteArray(),
            " x     x".toByteArray(),
            " x     x".toByteArray(),
            " x     x".toByteArray(),
            " x     x".toByteArray(),
            "  x   x ".toByteArray(),
            "   xxx  ".toByteArray(),
        ),
        arrayOf(
            "     xx ".toByteArray(),
            "     xx ".toByteArray(),
            "    x x ".toByteArray(),
            "  xx  x ".toByteArray(),
            "      x ".toByteArray(),
            "      x ".toByteArray(),
            "      x ".toByteArray(),
            "  xxxxxx".toByteArray(),
        ),
        arrayOf(
            "   xxxx ".toByteArray(),
            " x     x".toByteArray(),
            " x     x".toByteArray(),
            "      x ".toByteArray(),
            "    x   ".toByteArray(),
            "  x     ".toByteArray(),
            " x      ".toByteArray(),
            " xxxxxxx".toByteArray(),
        ),
        arrayOf(
            "  xxxxx ".toByteArray(),
            " x     x".toByteArray(),
            "      x ".toByteArray(),
            "   xxxx ".toByteArray(),
            "       x".toByteArray(),
            "       x".toByteArray(),
            " x    x ".toByteArray(),
            "  xxxx  ".toByteArray(),
        ),
        arrayOf(
            " x     x".toByteArray(),
            " x     x".toByteArray(),
            " x     x".toByteArray(),
            " x    xx".toByteArray(),
            "  xxxx x".toByteArray(),
            "       x".toByteArray(),
            "       x".toByteArray(),
            "       x".toByteArray(),
        ),
        arrayOf(
            " xxxxxxx".toByteArray(),
            " x      ".toByteArray(),
            " x      ".toByteArray(),
            " xxxxxx ".toByteArray(),
            "       x".toByteArray(),
            "       x".toByteArray(),
            "       x".toByteArray(),
            " xxxxxx ".toByteArray(),
        ),
        arrayOf(
            "  xxxxx ".toByteArray(),
            " x     x".toByteArray(),
            " x      ".toByteArray(),
            " x xxx  ".toByteArray(),
            " xx   xx".toByteArray(),
            " x     x".toByteArray(),
            " x     x".toByteArray(),
            "  xxxxx ".toByteArray(),
        ),
        arrayOf(
            " xxxxxxx".toByteArray(),
            "       x".toByteArray(),
            "       x".toByteArray(),
            "      x ".toByteArray(),
            "     x  ".toByteArray(),
            "    x   ".toByteArray(),
            "   x    ".toByteArray(),
            "  x     ".toByteArray(),
        ),
        arrayOf(
            "  xxxxx ".toByteArray(),
            " x     x".toByteArray(),
            " x     x".toByteArray(),
            "  xxxxx ".toByteArray(),
            " x     x".toByteArray(),
            " x     x".toByteArray(),
            " x     x".toByteArray(),
            "  xxxxx ".toByteArray(),
        ),
        arrayOf(
            "  xxxxx ".toByteArray(),
            " x     x".toByteArray(),
            " x     x".toByteArray(),
            " x     x".toByteArray(),
            "  xxxxxx".toByteArray(),
            "       x".toByteArray(),
            " x     x".toByteArray(),
            "  xxxxx ".toByteArray(),
        ),
    )

class Font {
    companion object {
        fun writeDigit(
            screen: MikroMk3HidDevice,
            y: Int,
            x: Int,
            num: Int,
            scale: Int,
        ) {
            val sym = FONT[num]
            for (i in 0 until (8 * scale)) {
                for (j in 0 until (8 * scale)) {
                    val bit = sym[i / scale][j / scale] != ' '.code.toByte()
                    screen.set(i + y, j + x, bit)
                }
            }
        }
    }
}
