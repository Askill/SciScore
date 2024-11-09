package org.example.ColorFactory

import kotlin.math.roundToInt

class ColorFactory(val paletteSize: Int) {
    var counter: Int = 0
    var colorPalette: List<String>
    var _paletteSize: Int = paletteSize


    init {
        colorPalette = generateColorPalette(paletteSize)
    }

    fun getColor(): String {
        counter += 1
        return colorPalette[(counter - 1) % _paletteSize]
    }

    fun generateColorPalette(numberOfColors: Int): List<String> {
        val colors = mutableListOf<String>()

        // Loop through the number of colors, and calculate the hue for each color
        repeat(numberOfColors) { i ->
            // Divide the color wheel into equal segments
            val hue = (i * 360.0 / numberOfColors).roundToInt() // Calculate hue

            // Convert HSL to RGB and then to HEX format
            val rgb = hslToRgb(hue, 0.7, 0.5) // Use saturation and lightness for pleasing colors
            val color = String.format("#%02X%02X%02X", rgb[0], rgb[1], rgb[2])

            colors.add(color)
        }

        return colors
    }

    // Helper function to convert HSL to RGB
    fun hslToRgb(h: Int, s: Double, l: Double): IntArray {
        val c = (1 - Math.abs(2 * l - 1)) * s
        val x = c * (1 - Math.abs(h / 60.0 % 2 - 1))
        val m = l - c / 2

        val (r, g, b) = when (h) {
            in 0..59 -> Triple(c, x, 0.0)
            in 60..119 -> Triple(x, c, 0.0)
            in 120..179 -> Triple(0.0, c, x)
            in 180..239 -> Triple(0.0, x, c)
            in 240..299 -> Triple(x, 0.0, c)
            in 300..359 -> Triple(c, 0.0, x)
            else -> Triple(0.0, 0.0, 0.0)
        }

        val rInt = ((r + m) * 255).roundToInt()
        val gInt = ((g + m) * 255).roundToInt()
        val bInt = ((b + m) * 255).roundToInt()

        return intArrayOf(rInt, gInt, bInt)
    }
}