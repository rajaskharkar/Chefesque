package com.kingkharnivore.chefesque.ui.screen.cookinglog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CookingLogFormattersTest {
    @Test
    fun durationFormattingUsesFriendlyWholeMinutes() {
        assertNull(formatCookingLogDuration(null))
        assertEquals("0 min", formatCookingLogDuration(0))
        assertEquals("1 min", formatCookingLogDuration(59))
        assertEquals("1 min", formatCookingLogDuration(60))
        assertEquals("59 min", formatCookingLogDuration(3599))
        assertEquals("1 hr", formatCookingLogDuration(3600))
        assertEquals("1 hr 1 min", formatCookingLogDuration(3660))
        assertEquals("1 hr 30 min", formatCookingLogDuration(5400))
    }

    @Test
    fun resultLabelsAreFriendlyAndUnknownValuesAreSafe() {
        assertEquals("Great", formatCookingLogResult("GREAT"))
        assertEquals("Good", formatCookingLogResult("GOOD"))
        assertEquals("Okay", formatCookingLogResult("OKAY"))
        assertEquals("Needs work", formatCookingLogResult("NEEDS_WORK"))
        assertEquals("Pretty Great", formatCookingLogResult("PRETTY_GREAT"))
        assertNull(formatCookingLogResult(null))
        assertNull(formatCookingLogResult("  "))
    }

    @Test
    fun wouldMakeAgainLabelsAreFriendlyAndUnknownValuesAreSafe() {
        assertEquals("Would make again", formatWouldMakeAgain("YES"))
        assertEquals("Would not make again", formatWouldMakeAgain("NO"))
        assertEquals("Unsure", formatWouldMakeAgain("UNSURE"))
        assertEquals("Maybe Later", formatWouldMakeAgain("MAYBE_LATER"))
        assertNull(formatWouldMakeAgain(null))
        assertNull(formatWouldMakeAgain("  "))
    }

    @Test
    fun notesPreviewUsesPriorityOrder() {
        assertEquals("Use less salt", formatCookingLogNotesPreview("Use less salt", "Added lemon", "Sauce was silky"))
        assertEquals("Added lemon", formatCookingLogNotesPreview(" ", "Added lemon", "Sauce was silky"))
        assertEquals("Sauce was silky", formatCookingLogNotesPreview(null, " ", "Sauce was silky"))
    }

    @Test
    fun notesPreviewTrimsEllipsizesAndReturnsNullForBlankNotes() {
        assertEquals("Trim me", formatCookingLogNotesPreview("  Trim me  ", null, null))
        assertEquals("This is a…", formatCookingLogNotesPreview("This is a long note", null, null, maxLength = 10))
        assertNull(formatCookingLogNotesPreview(" ", null, ""))
    }
}
