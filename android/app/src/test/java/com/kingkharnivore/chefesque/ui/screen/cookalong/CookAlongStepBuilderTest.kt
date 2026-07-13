package com.kingkharnivore.chefesque.ui.screen.cookalong

import com.kingkharnivore.chefesque.data.local.entity.CookSessionEntity
import com.kingkharnivore.chefesque.data.local.entity.RecipeIngredientEntity
import com.kingkharnivore.chefesque.data.local.entity.RecipeStepEntity
import com.kingkharnivore.chefesque.data.local.entity.StepIngredientLinkEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class CookAlongStepBuilderTest {
    @Test
    fun builderMapsLinkedIngredientsToCorrectStepAndIgnoresDanglingLinks() {
        val steps = listOf(step("step-1", "Cook onions."), step("step-2", "Add tomatoes."))
        val ingredients = listOf(ingredient("ingredient-1", "Onion"), ingredient("ingredient-2", "Tomatoes"))
        val links = listOf(
            StepIngredientLinkEntity("step-1", "ingredient-1"),
            StepIngredientLinkEntity("step-2", "ingredient-2"),
            StepIngredientLinkEntity("step-2", "missing-ingredient"),
            StepIngredientLinkEntity("missing-step", "ingredient-1"),
        )

        val result = buildCookAlongSteps(steps, ingredients, links)

        assertEquals(listOf("Onion"), result[0].ingredients.map { it.displayText })
        assertEquals(listOf("Tomatoes"), result[1].ingredients.map { it.displayText })
    }

    @Test
    fun builderPreservesTitleOnlyStepsForCookAlong() {
        val result = buildCookAlongSteps(
            steps = listOf(RecipeStepEntity("step-title", "recipe-id", "", null, null, null, null, null, null, null, 0, title = "Preheat the oven")),
            ingredients = emptyList(),
            links = emptyList(),
        )

        assertEquals("Preheat the oven", result.single().title)
        assertEquals("", result.single().instruction)
    }


    @Test
    fun prepareTimerForStepCreatesIdleTimerForTimedStep() {
        val snapshot = prepareTimerForStep(480)

        assertEquals(480, snapshot.originalSeconds)
        assertEquals(480, snapshot.remainingSeconds)
        assertEquals(CookAlongTimerStatus.IDLE, snapshot.status)
    }

    @Test
    fun prepareTimerForStepClearsTimerForUntimedStep() {
        val snapshot = prepareTimerForStep(null)

        assertEquals(null, snapshot.originalSeconds)
        assertEquals(null, snapshot.remainingSeconds)
        assertEquals(CookAlongTimerStatus.IDLE, snapshot.status)
    }

    @Test
    fun addOneMinuteKeepsRunningTimersRunning() {
        val snapshot = addOneMinuteToTimer(120, 480, CookAlongTimerStatus.RUNNING)

        assertEquals(180, snapshot?.remainingSeconds)
        assertEquals(480, snapshot?.originalSeconds)
        assertEquals(CookAlongTimerStatus.RUNNING, snapshot?.status)
    }

    @Test
    fun addOneMinuteKeepsPausedTimersPaused() {
        val snapshot = addOneMinuteToTimer(120, 480, CookAlongTimerStatus.PAUSED)

        assertEquals(180, snapshot?.remainingSeconds)
        assertEquals(CookAlongTimerStatus.PAUSED, snapshot?.status)
    }

    @Test
    fun addOneMinuteToFinishedTimerMakesItPausedWithOneMinute() {
        val snapshot = addOneMinuteToTimer(0, 480, CookAlongTimerStatus.FINISHED)

        assertEquals(60, snapshot?.remainingSeconds)
        assertEquals(CookAlongTimerStatus.PAUSED, snapshot?.status)
    }


    @Test
    fun restoreTimerConvertsRunningSessionToPaused() {
        val snapshot = restoreTimerSnapshotForStep(session(timerStatus = CookAlongTimerStatus.RUNNING.name), timedStep())

        assertEquals(CookAlongTimerStatus.PAUSED, snapshot.status)
        assertEquals(300, snapshot.remainingSeconds)
    }

    @Test
    fun restoreTimerPreservesStableStatuses() {
        assertEquals(CookAlongTimerStatus.IDLE, restoreTimerSnapshotForStep(session(timerStatus = CookAlongTimerStatus.IDLE.name), timedStep()).status)
        assertEquals(CookAlongTimerStatus.PAUSED, restoreTimerSnapshotForStep(session(timerStatus = CookAlongTimerStatus.PAUSED.name), timedStep()).status)
        assertEquals(CookAlongTimerStatus.FINISHED, restoreTimerSnapshotForStep(session(timerStatus = CookAlongTimerStatus.FINISHED.name), timedStep()).status)
    }

    @Test
    fun restoreTimerClearsSnapshotForUntimedStep() {
        val snapshot = restoreTimerSnapshotForStep(session(), timedStep(timerSeconds = null))

        assertEquals(null, snapshot.originalSeconds)
        assertEquals(null, snapshot.remainingSeconds)
        assertEquals(CookAlongTimerStatus.IDLE, snapshot.status)
    }

    @Test
    fun restoreTimerResetsWhenRecipeTimerChanged() {
        val snapshot = restoreTimerSnapshotForStep(session(timerOriginalSeconds = 300), timedStep(timerSeconds = 480))

        assertEquals(480, snapshot.originalSeconds)
        assertEquals(480, snapshot.remainingSeconds)
        assertEquals(CookAlongTimerStatus.IDLE, snapshot.status)
    }

    private fun step(id: String, instruction: String) = RecipeStepEntity(id, "recipe-id", instruction, null, null, null, null, null, null, null, 0)

    private fun ingredient(id: String, name: String) = RecipeIngredientEntity(id, "recipe-id", null, name, null, null, null, null, null, false, 0)

    private fun timedStep(timerSeconds: Int? = 300) = CookAlongStepUiModel(
        id = "step-id",
        instruction = "Cook.",
        timerSeconds = timerSeconds,
        warning = null,
        equipment = null,
        whileTimerRuns = null,
        checkpoint = null,
        ingredients = emptyList(),
    )

    private fun session(
        timerOriginalSeconds: Int? = 300,
        timerRemainingSeconds: Int? = 300,
        timerStatus: String? = CookAlongTimerStatus.IDLE.name,
    ) = CookSessionEntity(
        id = "session-id",
        recipeId = "recipe-id",
        titleSnapshot = "Recipe",
        startedAt = 0,
        completedAt = null,
        status = "ACTIVE",
        currentStepIndex = 0,
        actualDurationSeconds = null,
        createdFromRecipe = true,
        timerOriginalSeconds = timerOriginalSeconds,
        timerRemainingSeconds = timerRemainingSeconds,
        timerStatus = timerStatus,
        updatedAt = 0,
    )
}
