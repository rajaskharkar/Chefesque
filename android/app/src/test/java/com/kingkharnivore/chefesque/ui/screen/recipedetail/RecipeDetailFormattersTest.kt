package com.kingkharnivore.chefesque.ui.screen.recipedetail

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RecipeDetailFormattersTest {
    @Test
    fun formatStepTimer_hidesNullAndNonPositiveValues() {
        assertNull(formatStepTimer(null))
        assertNull(formatStepTimer(0))
        assertNull(formatStepTimer(-1))
    }

    @Test
    fun formatStepTimer_formatsSecondsMinutesAndMixedDurations() {
        assertEquals("30 sec", formatStepTimer(30))
        assertEquals("1 min", formatStepTimer(60))
        assertEquals("1 min 30 sec", formatStepTimer(90))
        assertEquals("8 min", formatStepTimer(480))
    }
}
