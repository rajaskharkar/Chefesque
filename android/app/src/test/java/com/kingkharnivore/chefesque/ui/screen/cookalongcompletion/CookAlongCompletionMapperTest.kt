package com.kingkharnivore.chefesque.ui.screen.cookalongcompletion

import com.kingkharnivore.chefesque.data.local.entity.CookSessionEntity
import com.kingkharnivore.chefesque.domain.model.CookSessionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CookAlongCompletionMapperTest {
    @Test
    fun buildCookAlongCompletionLog_mapsSessionFields() {
        val session = completedSession()

        val log = buildCookAlongCompletionLog(
            session = session,
            result = CookAlongResultGood,
            wouldMakeAgain = WouldMakeAgainYes,
            notesForNextTime = "  Add lemon  ",
            now = 2_000L,
            id = "log-1",
        )

        assertEquals("log-1", log.id)
        assertEquals("recipe-1", log.recipeId)
        assertEquals("session-1", log.cookSessionId)
        assertEquals("Pasta", log.titleSnapshot)
        assertEquals(1_500L, log.cookedAt)
        assertEquals(900, log.actualDurationSeconds)
        assertEquals(CookAlongResultGood, log.result)
        assertEquals(WouldMakeAgainYes, log.wouldMakeAgain)
        assertEquals("Add lemon", log.notesForNextTime)
        assertTrue(log.createdFromCookAlong)
        assertFalse(log.isFavorite)
        assertEquals(2_000L, log.createdAt)
        assertEquals(2_000L, log.updatedAt)
        assertNull(log.whatWentWell)
        assertNull(log.changesMade)
    }

    @Test
    fun buildCookAlongCompletionLog_usesNowWhenCompletedAtMissingAndClearsBlankNotes() {
        val log = buildCookAlongCompletionLog(
            session = completedSession(completedAt = null),
            result = null,
            wouldMakeAgain = null,
            notesForNextTime = "   ",
            now = 2_000L,
            id = "log-2",
        )

        assertEquals(2_000L, log.cookedAt)
        assertNull(log.notesForNextTime)
    }

    private fun completedSession(completedAt: Long? = 1_500L) = CookSessionEntity(
        id = "session-1",
        recipeId = "recipe-1",
        titleSnapshot = "Pasta",
        startedAt = 600L,
        completedAt = completedAt,
        status = CookSessionStatus.COMPLETED.name,
        currentStepIndex = 2,
        actualDurationSeconds = 900,
        createdFromRecipe = true,
        timerOriginalSeconds = null,
        timerRemainingSeconds = null,
        timerStatus = null,
        updatedAt = 1_500L,
    )
}
