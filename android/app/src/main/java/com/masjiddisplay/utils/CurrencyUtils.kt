package com.masjiddisplay.utils

import java.text.NumberFormat
import java.util.Locale

/**
 * Format number as Indonesian Rupiah currency
 */
fun formatCurrency(amount: Long): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    format.maximumFractionDigits = 0
    return format.format(amount)
}
