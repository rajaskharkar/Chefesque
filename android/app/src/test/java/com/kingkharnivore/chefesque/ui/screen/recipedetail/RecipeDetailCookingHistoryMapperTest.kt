package com.kingkharnivore.chefesque.ui.screen.recipedetail

import com.kingkharnivore.chefesque.data.local.entity.CookingLogEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipeDetailCookingHistoryMapperTest {
    @Test
    fun cookingLogUiModelMapsDisplayFields() {
        val model = toRecipeCookingLogUiModel(
            log = sampleLog(
                id = "log-1",
                cookedAt = 10L,
                actualDurationSeconds = 3660,
                result = "GOOD",
                wouldMakeAgain = "YES",
                notesForNextTime = "Use less salt",
            ),
            dateFormatter = { "Date $it" },
        )

        assertEquals("log-1", model.id)
        assertEquals("Date 10", model.cookedDateText)
        assertEquals("1 hr 1 min", model.durationText)
        assertEquals("Good", model.resultText)
        assertEquals("Would make again", model.wouldMakeAgainText)
        assertEquals("Use less salt", model.notesPreview)
    }

    @Test
    fun blankNotesProduceNullNotesPreview() {
        val model = toRecipeCookingLogUiModel(
            log = sampleLog(notesForNextTime = " ", changesMade = null, whatWentWell = ""),
            dateFormatter = { "Date" },
        )

        assertNull(model.notesPreview)
    }

    @Test
    fun cookAlongAndFavoriteFlagsArePreserved() {
        val model = toRecipeCookingLogUiModel(
            log = sampleLog(createdFromCookAlong = true, isFavorite = true),
            dateFormatter = { "Date" },
        )

        assertTrue(model.createdFromCookAlong)
        assertTrue(model.isFavorite)
    }

    @Test
    fun cookingHistoryUsesLatestThreeLogsNewestFirst() {
        val history = buildRecipeCookingHistoryUi(
            logs = listOf(
                sampleLog(id = "old", cookedAt = 1L, createdAt = 1L),
                sampleLog(id = "newest", cookedAt = 5L, createdAt = 1L),
                sampleLog(id = "tie-newer-created", cookedAt = 4L, createdAt = 3L),
                sampleLog(id = "tie-older-created", cookedAt = 4L, createdAt = 2L),
            ),
            dateFormatter = { "Date $it" },
        )

        assertEquals(listOf("newest", "tie-newer-created", "tie-older-created"), history.recentLogs.map { it.id })
        assertEquals(4, history.totalLogCount)
    }

    @Test
    fun lastCookedTextComesFromNewestLog() {
        val history = buildRecipeCookingHistoryUi(
            logs = listOf(
                sampleLog(id = "older", cookedAt = 1L),
                sampleLog(id = "newer", cookedAt = 2L),
            ),
            dateFormatter = { "Date $it" },
        )

        assertEquals("Date 2", history.lastCookedText)
    }

    @Test
    fun emptyLogsProduceEmptyHistoryUi() {
        val history = buildRecipeCookingHistoryUi(emptyList(), dateFormatter = { "Date" })

        assertTrue(history.recentLogs.isEmpty())
        assertEquals(0, history.totalLogCount)
        assertNull(history.lastCookedText)
    }

    private fun sampleLog(
        id: String = "log",
        cookedAt: Long = 1L,
        createdAt: Long = 1L,
        actualDurationSeconds: Int? = null,
        result: String? = null,
        wouldMakeAgain: String? = null,
        notesForNextTime: String? = null,
        changesMade: String? = null,
        whatWentWell: String? = null,
        createdFromCookAlong: Boolean = false,
        isFavorite: Boolean = false,
    ) = CookingLogEntity(
        id = id,
        recipeId = "recipe-1",
        cookSessionId = null,
        titleSnapshot = "Test Recipe",
        cookedAt = cookedAt,
        actualDurationSeconds = actualDurationSeconds,
        result = result,
        wouldMakeAgain = wouldMakeAgain,
        whatWentWell = whatWentWell,
        changesMade = changesMade,
        notesForNextTime = notesForNextTime,
        isFavorite = isFavorite,
        createdFromCookAlong = createdFromCookAlong,
        createdAt = createdAt,
        updatedAt = createdAt,
    )
}
