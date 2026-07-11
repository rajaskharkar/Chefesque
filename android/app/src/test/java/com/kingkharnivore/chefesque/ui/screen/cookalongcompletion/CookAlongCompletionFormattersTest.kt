package com.kingkharnivore.chefesque.ui.screen.cookalongcompletion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CookAlongCompletionFormattersTest {
    @Test
    fun formatCompletionDuration_handlesNullAndZero() {
        assertNull(formatCompletionDuration(null))
        assertEquals("0 min", formatCompletionDuration(0))
    }

    @Test
    fun formatCompletionDuration_roundsUpToFriendlyMinutes() {
        assertEquals("1 min", formatCompletionDuration(59))
        assertEquals("1 min", formatCompletionDuration(60))
        assertEquals("59 min", formatCompletionDuration(3_599))
    }

    @Test
    fun formatCompletionDuration_formatsHours() {
        assertEquals("1 hr", formatCompletionDuration(3_600))
        assertEquals("1 hr 1 min", formatCompletionDuration(3_660))
        assertEquals("1 hr 30 min", formatCompletionDuration(5_400))
    }

    @Test
    fun formatCompletedSteps_prefersTotalSteps() {
        assertEquals("Completed 6 steps", formatCompletedSteps(currentStepIndex = 1, totalSteps = 6))
        assertEquals("Completed 1 step", formatCompletedSteps(currentStepIndex = 0, totalSteps = 1))
    }

    @Test
    fun formatCompletedSteps_fallsBackToCurrentStepIndex() {
        assertEquals("Completed 3 steps", formatCompletedSteps(currentStepIndex = 2, totalSteps = null))
        assertEquals("Cook Along completed", formatCompletedSteps(currentStepIndex = null, totalSteps = null))
    }

    @Test
    fun resultLabel_mapsKnownValues() {
        assertEquals("Great", resultLabel(CookAlongResultGreat))
        assertEquals("Good", resultLabel(CookAlongResultGood))
        assertEquals("Okay", resultLabel(CookAlongResultOkay))
        assertEquals("Needs work", resultLabel(CookAlongResultNeedsWork))
        assertNull(resultLabel("OTHER"))
        assertNull(resultLabel(null))
    }

    @Test
    fun wouldMakeAgainLabel_mapsKnownValues() {
        assertEquals("Yes", wouldMakeAgainLabel(WouldMakeAgainYes))
        assertEquals("No", wouldMakeAgainLabel(WouldMakeAgainNo))
        assertEquals("Unsure", wouldMakeAgainLabel(WouldMakeAgainUnsure))
        assertNull(wouldMakeAgainLabel("MAYBE"))
        assertNull(wouldMakeAgainLabel(null))
    }

    @Test
    fun trimOptionalNotes_trimsOrReturnsNull() {
        assertEquals("Use less salt", trimOptionalNotes("  Use less salt  "))
        assertNull(trimOptionalNotes("   "))
    }
}
