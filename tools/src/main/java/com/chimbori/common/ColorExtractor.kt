package com.chimbori.common

import java.awt.image.BufferedImage

/**
 * Extracts the dominant color from an image using a simple fast algorithm. It only considers
 * exact matches for color, so if a JPEG contains a small area of homogenous color and a large area
 * of almost-homogenous color, the smaller area’s color will be reported. Since we use this mostly
 * to extract color from flat-color icons, it works well in practice for our dataset.
 */
object ColorExtractor {
  @JvmStatic
  fun getDominantColor(image: BufferedImage): Color? {
    val colorFrequencies = mutableMapOf<Color, Int>()
    val height = image.height
    val width = image.width
    for (i in 0 until width) {
      for (j in 0 until height) {
        val pixel = Color.from(image.getRGB(i, j))
        if (!pixel.isGrey) {
          var counter = colorFrequencies[pixel] ?: 0
          colorFrequencies[pixel] = ++counter
        }
      }
    }
    return getDominantColor(colorFrequencies)
  }

  private fun getDominantColor(map: Map<Color, Int>): Color {
    val list = map.entries.sortedWith(kotlin.Comparator { obj1, obj2 -> obj1.value.compareTo(obj2.value) })
    return if (list.size > 0) {
      list[list.size - 1].key
    } else {
      Color(0, 0, 0)
    } // Defaults to black.
  }

  class Color internal constructor(private val r: Int, private val g: Int, private val b: Int) {
    val isGrey: Boolean
      get() = Math.abs(r - b) <= TOLERANCE_FOR_GREY && Math.abs(r - g) <= TOLERANCE_FOR_GREY

    fun darken(ratio: Float): Color {
      require(!(ratio < 0 || ratio >= 1))
      return Color((r * ratio).toInt(), (g * ratio).toInt(), (b * ratio).toInt())
    }

    override fun toString(): String {
      return String.format("#%02x%02x%02x", r, g, b)
    }

    companion object {
      private const val TOLERANCE_FOR_GREY = 16
      fun from(rgba: Int): Color {
        return Color(rgba shr 16 and 0xff, rgba shr 8 and 0xff, rgba and 0xff)
      }
    }
  }
}