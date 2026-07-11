package com.kingkharnivore.chefesque.ui.screen.cookinglog

import java.text.DateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ceil

fun formatCookingLogDate(cookedAt: Long): String = DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(cookedAt))

fun formatCookingLogDuration(seconds: Int?): String? {
    if (seconds == null) return null
    val minutes = ceil(seconds.coerceAtLeast(0) / 60.0).toInt()
    if (minutes < 60) return "$minutes min"
    val hours = minutes / 60
    val remainingMinutes = minutes % 60
    return if (remainingMinutes == 0) "$hours hr" else "$hours hr $remainingMinutes min"
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
