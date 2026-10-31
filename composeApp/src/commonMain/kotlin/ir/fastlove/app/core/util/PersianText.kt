package ir.fastlove.app.core.util

/** Converts ASCII digits in [value] to Persian digits (۰-۹). Pure, multiplatform. */
fun persianDigits(value: String): String = value.map { c ->
    if (c in '0'..'9') ('۰'.code + (c - '0')).toChar() else c
}.joinToString("")

/** Converts Persian/Arabic-Indic digits back to ASCII digits. */
fun latinDigits(value: String): String = value.map { c ->
    when (c) {
        in '۰'..'۹' -> ('0'.code + (c - '۰')).toChar()
        in '٠'..'٩' -> ('0'.code + (c - '٠')).toChar()
        else -> c
    }
}.joinToString("")

/** Formats a Toman amount with Persian thousands separators, e.g. ۱۲۳٬۴۵۶ تومان. */
fun formatToman(amount: Int): String {
    val grouped = groupThousands(amount)
    return persianDigits(grouped).replace(',', '٬') + " تومان"
}

/** Groups an integer with commas every three digits without relying on platform String.format. */
fun groupThousands(amount: Int): String {
    val negative = amount < 0
    val digits = kotlin.math.abs(amount).toString()
    val sb = StringBuilder()
    val n = digits.length
    for (i in 0 until n) {
        if (i > 0 && (n - i) % 3 == 0) sb.append(',')
        sb.append(digits[i])
    }
    return (if (negative) "-" else "") + sb.toString()
}

/** One decimal place, Persian digits, Persian decimal separator (٫). */
fun oneDecimalPersian(value: Float): String {
    val scaled = kotlin.math.round(value * 10f).toInt()
    val whole = scaled / 10
    val frac = kotlin.math.abs(scaled % 10)
    return persianDigits("$whole") + "٫" + persianDigits("$frac")
}
