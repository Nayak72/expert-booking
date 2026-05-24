package com.expertconnect.app.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Extension functions used across the app.
 */

/** Format a date string from "YYYY-MM-DD" to "DD MMM YYYY". */
fun String.toDisplayDate(): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val display = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        display.format(sdf.parse(this) ?: Date())
    } catch (e: Exception) {
        this
    }
}

/** Format a price float to a display string e.g. "$75/hr". */
fun Double.toPriceString(): String = "$${"%.0f".format(this)}/hr"

/** Format a rating float to one decimal place. */
fun Double.toRatingString(): String = "%.1f".format(this)

/** Capitalize the first letter of each word. */
fun String.toTitleCase(): String = split(" ").joinToString(" ") { word ->
    word.lowercase().replaceFirstChar { it.uppercase() }
}

/** Truncate string with ellipsis if longer than maxLength. */
fun String.ellipsize(maxLength: Int = 100): String =
    if (length > maxLength) "${take(maxLength)}…" else this

/** Convert comma-separated skills string to a display-friendly list. */
fun String?.toSkillsList(): List<String> =
    this?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
