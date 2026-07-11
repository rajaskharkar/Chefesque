package com.kingkharnivore.chefesque.ui.screen.cookalongcompletion

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

const val CookAlongResultGreat = "GREAT"
const val CookAlongResultGood = "GOOD"
const val CookAlongResultOkay = "OKAY"
const val CookAlongResultNeedsWork = "NEEDS_WORK"

const val WouldMakeAgainYes = "YES"
const val WouldMakeAgainNo = "NO"
const val WouldMakeAgainUnsure = "UNSURE"

val CookAlongResultChoices = listOf(
    CookAlongResultGreat,
    CookAlongResultGood,
    CookAlongResultOkay,
    CookAlongResultNeedsWork,
)

val WouldMakeAgainChoices = listOf(
    WouldMakeAgainYes,
    WouldMakeAgainNo,
    WouldMakeAgainUnsure,
)

private val CompletionDateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)

fun formatCompletionDuration(seconds: Int?): String? {
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

fun formatCompletionDate(timestampMs: Long?): String? = timestampMs?.let {
    Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).format(CompletionDateFormatter)
}

fun formatCompletedSteps(currentStepIndex: Int?, totalSteps: Int?): String {
    val completedSteps = when {
        totalSteps != null && totalSteps > 0 -> totalSteps
        currentStepIndex != null && currentStepIndex >= 0 -> currentStepIndex + 1
        else -> null
    }
    return completedSteps?.let { "Completed $it ${if (it == 1) "step" else "steps"}" } ?: "Cook Along completed"
}

fun resultLabel(value: String?): String? = when (value) {
    CookAlongResultGreat -> "Great"
    CookAlongResultGood -> "Good"
    CookAlongResultOkay -> "Okay"
    CookAlongResultNeedsWork -> "Needs work"
    else -> null
}

fun wouldMakeAgainLabel(value: String?): String? = when (value) {
    WouldMakeAgainYes -> "Yes"
    WouldMakeAgainNo -> "No"
    WouldMakeAgainUnsure -> "Unsure"
    else -> null
}

fun trimOptionalNotes(notes: String): String? = notes.trim().ifBlank { null }
