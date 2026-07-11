package com.kingkharnivore.chefesque.ui.screen.cookinglog

import java.text.DateFormat
import java.util.Date
import java.util.Locale

fun formatCookingLogDate(cookedAt: Long): String = DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(cookedAt))

fun formatCookingLogDuration(seconds: Int?): String? {
    val safeSeconds = seconds ?: return null
    if (safeSeconds <= 0) return "0 min"

    val totalMinutes = if (safeSeconds < 60) 1 else safeSeconds / 60
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    return when {
        hours <= 0 -> "$totalMinutes min"
        minutes == 0 -> "$hours ${if (hours == 1) "hr" else "hrs"}"
        else -> "$hours ${if (hours == 1) "hr" else "hrs"} $minutes min"
    }
}

fun formatCookingLogResult(result: String?): String? = when (result?.trim()?.uppercase(Locale.US)) {
    null, "" -> null
    "GREAT" -> "Great"
    "GOOD" -> "Good"
    "OKAY" -> "Okay"
    "NEEDS_WORK" -> "Needs work"
    else -> result.toSafeLabel()
}

fun formatWouldMakeAgain(value: String?): String? = when (value?.trim()?.uppercase(Locale.US)) {
    null, "" -> null
    "YES" -> "Would make again"
    "NO" -> "Would not make again"
    "UNSURE" -> "Unsure"
    else -> value.toSafeLabel()
}

fun formatCookingLogNotesPreview(
    notesForNextTime: String?,
    changesMade: String?,
    whatWentWell: String?,
    maxLength: Int = 120,
): String? {
    val note = listOf(notesForNextTime, changesMade, whatWentWell)
        .firstNotNullOfOrNull { it?.trim()?.takeIf(String::isNotBlank) }
        ?: return null
    if (maxLength <= 0) return ""
    return if (note.length <= maxLength) note else note.take((maxLength - 1).coerceAtLeast(0)).trimEnd() + "…"
}

private fun String?.toSafeLabel(): String? {
    val cleaned = this?.trim()?.replace('_', ' ')?.lowercase(Locale.US)?.takeIf(String::isNotBlank) ?: return null
    return cleaned.split(Regex("\\s+")).joinToString(" ") { word -> word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() } }
}
