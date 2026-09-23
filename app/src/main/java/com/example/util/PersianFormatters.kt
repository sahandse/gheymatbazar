package com.example.util

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object PersianFormatters {

    private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    fun toPersianDigits(input: String): String {
        val sb = java.lang.StringBuilder(input.length)
        for (c in input) {
            if (c in '0'..'9') {
                sb.append(persianDigits[c - '0'])
            } else if (c == ',') {
                sb.append('٬')
            } else if (c == '%') {
                sb.append('٪')
            } else {
                sb.append(c)
            }
        }
        return sb.toString()
    }

    fun formatPrice(price: Double): String {
        val longVal = price.toLong()
        val decimalFormat = DecimalFormat("#,###")
        val formatted = decimalFormat.format(longVal)
        return toPersianDigits(formatted)
    }

    fun formatPercentage(pct: Double?): String {
        if (pct == null || pct == 0.0) return ""
        val sign = if (pct > 0) "+" else ""
        val df = DecimalFormat("0.##")
        val formatted = "$sign${df.format(pct)}%"
        return toPersianDigits(formatted)
    }

    fun formatDecimal(value: Double, maxFractionDigits: Int = 3): String {
        val pattern = if (maxFractionDigits > 0) "#,##0." + "#".repeat(maxFractionDigits) else "#,##0"
        val df = DecimalFormat(pattern)
        return toPersianDigits(df.format(value))
    }

    fun parseUserInputToDouble(raw: String): Double? {
        val normalized = raw
            .replace("۰", "0")
            .replace("۱", "1")
            .replace("۲", "2")
            .replace("۳", "3")
            .replace("۴", "4")
            .replace("۵", "5")
            .replace("۶", "6")
            .replace("۷", "7")
            .replace("۸", "8")
            .replace("۹", "9")
            .replace(",", "")
            .replace("٬", "")
            .replace("/", ".")
            .trim()
        return normalized.toDoubleOrNull()
    }

    fun formatTime(timestamp: Long): String {
        if (timestamp <= 0) return ""
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        // Tehran timezone or local device timezone
        val tehranTz = TimeZone.getTimeZone("Asia/Tehran")
        if (tehranTz != null) {
            sdf.timeZone = tehranTz
        }
        val formatted = sdf.format(Date(timestamp))
        return toPersianDigits(formatted)
    }

    fun formatFullDateTime(timestamp: Long): String {
        if (timestamp <= 0) return ""
        val sdf = SimpleDateFormat("HH:mm:ss - yyyy/MM/dd", Locale.getDefault())
        val tehranTz = TimeZone.getTimeZone("Asia/Tehran")
        if (tehranTz != null) {
            sdf.timeZone = tehranTz
        }
        val formatted = sdf.format(Date(timestamp))
        return toPersianDigits(formatted)
    }
}
