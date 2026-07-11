package com.kingkharnivore.chefesque.ui.screen.cookinglogdetail

import com.kingkharnivore.chefesque.data.local.entity.CookingLogEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CookingLogDetailMapperTest {
    @Test
    fun blankTitleBecomesUntitledCook() {
        val state = toCookingLogDetailUiState(log = sampleLog(titleSnapshot = "  "), recipeExists = false, dateFormatter = { "Cooked date" })

        assertEquals("Untitled cook", state.title)
    }

    @Test
    fun durationResultAndWouldMakeAgainMapToFriendlyLabels() {
        val state = toCookingLogDetailUiState(
            log = sampleLog(actualDurationSeconds = 5400, result = "NEEDS_WORK", wouldMakeAgain = "UNSURE"),
            recipeExists = false,
            dateFormatter = { "Cooked date" },
        )

        assertEquals("1 hr 30 min", state.durationText)
        assertEquals("Needs work", state.resultText)
        assertEquals("Unsure", state.wouldMakeAgainText)
    }

    @Test
    fun sourceTextMapsCookAlongAndManualLogs() {
        val cookAlong = toCookingLogDetailUiState(sampleLog(createdFromCookAlong = true), recipeExists = false, dateFormatter = { "Cooked date" })
        val manual = toCookingLogDetailUiState(sampleLog(createdFromCookAlong = false), recipeExists = false, dateFormatter = { "Cooked date" })

        assertEquals("Created from Cook Along", cookAlong.sourceText)
        assertEquals("Manual cooking log", manual.sourceText)
    }

    @Test
    fun blankNotesBecomeNullAndNonBlankNotesAreTrimmed() {
        val state = toCookingLogDetailUiState(
            log = sampleLog(notesForNextTime = "  Use less salt  ", changesMade = " ", whatWentWell = null),
            recipeExists = false,
            dateFormatter = { "Cooked date" },
        )

        assertEquals("Use less salt", state.notesForNextTime)
        assertNull(state.changesMade)
        assertNull(state.whatWentWell)
    }

    @Test
    fun missingRecipeDoesNotBreakMapping() {
        val state = toCookingLogDetailUiState(
            log = sampleLog(recipeId = "missing-recipe"),
            recipeExists = false,
            dateFormatter = { "Cooked date" },
        )

        assertEquals("missing-recipe", state.recipeId)
        assertFalse(state.recipeAvailable)
        assertEquals("Test Cook", state.title)
    }

    @Test
    fun existingRecipeIsMarkedAvailable() {
        val state = toCookingLogDetailUiState(
            log = sampleLog(recipeId = "recipe-1"),
            recipeExists = true,
            dateFormatter = { "Cooked date" },
        )

        assertTrue(state.recipeAvailable)
    }

    @Test
    fun nullLogMapsToNotFoundState() {
        val state = toCookingLogDetailUiState(log = null, recipeExists = false, dateFormatter = { "Cooked date" })

        assertTrue(state.notFound)
        assertFalse(state.isLoading)
    }

    private fun sampleLog(
        titleSnapshot: String = "Test Cook",
        recipeId: String? = null,
        actualDurationSeconds: Int? = null,
        result: String? = null,
        wouldMakeAgain: String? = null,
        notesForNextTime: String? = null,
        changesMade: String? = null,
        whatWentWell: String? = null,
        createdFromCookAlong: Boolean = true,
    ) = CookingLogEntity(
        id = "log-1",
        recipeId = recipeId,
        cookSessionId = null,
        titleSnapshot = titleSnapshot,
        cookedAt = 123L,
        actualDurationSeconds = actualDurationSeconds,
        result = result,
        wouldMakeAgain = wouldMakeAgain,
        whatWentWell = whatWentWell,
        changesMade = changesMade,
        notesForNextTime = notesForNextTime,
        isFavorite = false,
        createdFromCookAlong = createdFromCookAlong,
        createdAt = 123L,
        updatedAt = 123L,
    )
}
