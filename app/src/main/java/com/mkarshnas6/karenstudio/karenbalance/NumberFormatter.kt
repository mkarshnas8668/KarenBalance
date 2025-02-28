package com.mkarshnas6.karenstudio.karenbalance

object NumberFormatter {

    fun formatNumber(number: Long): String {
        return String.format("%,d", number)
    }

    fun formatNumber(number: String): String {
        return try {
            formatNumber(number.toLong())
        } catch (e: NumberFormatException) {
            "Invalid number"
        }
    }
}

fun Long.format_number(): String {
    return NumberFormatter.formatNumber(this)
}

fun String.format_number(): String {
    return try {
        this.toLong().format_number()
    } catch (e: NumberFormatException) {
        "Invalid number"
    }
}
