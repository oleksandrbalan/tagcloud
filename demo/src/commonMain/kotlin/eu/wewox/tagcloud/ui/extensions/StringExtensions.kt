package eu.wewox.tagcloud.ui.extensions

import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Formats a float value to a string with defined number of decimal numbers.
 */
fun Float.formatToDecimals(decimals: Int = 1): String {
    val integerDigits = this.toInt()
    val floatDigits = ((this - integerDigits) * 10f.pow(decimals)).roundToInt()
    return "$integerDigits.${prefixNumber(floatDigits, decimals)}"
}

private fun prefixNumber(id: Int, length: Int): String {
    val number = id.toString()
    val zeroes = (length - number.length).coerceAtLeast(0)
    val prefix = List(zeroes) { "0" }.joinToString(separator = "")
    return "$prefix$number"
}
