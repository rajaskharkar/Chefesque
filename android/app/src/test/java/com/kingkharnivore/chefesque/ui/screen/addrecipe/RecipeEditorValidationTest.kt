package com.kingkharnivore.chefesque.ui.screen.addrecipe

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipeEditorValidationTest {
    @Test
    fun `title only step satisfies publish requirement`() {
        val result = validateRecipeForPublish(
            title = "Roast chicken",
            ingredients = listOf(validIngredient()),
            steps = listOf(StepInputState(title = "Preheat the oven")),
        )

        assertFalse(result.missingSteps)
        assertTrue(result.isValid)
        assertTrue(StepInputState(title = "Preheat the oven").isPublishableStep())
    }

    @Test
    fun `instruction only step satisfies publish requirement`() {
        val result = validateRecipeForPublish(
            title = "Onion Curry",
            ingredients = listOf(validIngredient()),
            steps = listOf(StepInputState(instruction = "Heat the oil.")),
        )

        assertFalse(result.missingSteps)
        assertTrue(result.isValid)
    }

    @Test
    fun `step with title and instruction satisfies publish requirement`() {
        val result = validateRecipeForPublish(
            title = "Sauce",
            ingredients = listOf(validIngredient()),
            steps = listOf(StepInputState(title = "Make the sauce", instruction = "Cook until deeply golden.")),
        )

        assertFalse(result.missingSteps)
        assertTrue(result.isValid)
    }

    @Test
    fun `multiple title only steps satisfy publish requirement`() {
        val result = validateRecipeForPublish(
            title = "Simple prep",
            ingredients = listOf(validIngredient()),
            steps = listOf(
                StepInputState(title = "Preheat the oven"),
                StepInputState(title = "Chop the onions"),
                StepInputState(title = "Serve immediately"),
            ),
        )

        assertFalse(result.missingSteps)
        assertTrue(result.isValid)
    }

    @Test
    fun `one title only step plus blank placeholders remains publishable`() {
        val result = validateRecipeForPublish(
            title = "Simple prep",
            ingredients = listOf(validIngredient()),
            steps = listOf(
                StepInputState(),
                StepInputState(title = "Preheat the oven"),
                StepInputState(),
            ),
        )

        assertFalse(result.missingSteps)
        assertTrue(result.isValid)
    }

    @Test
    fun `instructions with surrounding whitespace satisfy publish requirement`() {
        val result = validateRecipeForPublish(
            title = "Soup",
            ingredients = listOf(validIngredient()),
            steps = listOf(StepInputState(instruction = "  Simmer gently.  ")),
        )

        assertFalse(result.missingSteps)
        assertTrue(result.isValid)
    }

    @Test
    fun `title with surrounding whitespace satisfies publish requirement`() {
        val result = validateRecipeForPublish(
            title = "Soup",
            ingredients = listOf(validIngredient()),
            steps = listOf(StepInputState(title = "  Serve warm  ")),
        )

        assertFalse(result.missingSteps)
        assertTrue(result.isValid)
    }

    @Test
    fun `timer only step does not satisfy publish requirement`() {
        val result = validateRecipeForPublish(
            title = "Soup",
            ingredients = listOf(validIngredient()),
            steps = listOf(StepInputState(timerMinutes = "10")),
        )

        assertTrue(result.missingSteps)
        assertFalse(result.isValid)
        assertTrue(StepInputState(timerMinutes = "10").hasAnyContent())
        assertFalse(StepInputState(timerMinutes = "10").isPublishableStep())
    }

    @Test
    fun `meanwhile only step does not satisfy publish requirement`() {
        val result = validateRecipeForPublish(
            title = "Soup",
            ingredients = listOf(validIngredient()),
            steps = listOf(StepInputState(meanwhile = "Chop parsley.")),
        )

        assertTrue(result.missingSteps)
        assertFalse(result.isValid)
    }

    @Test
    fun `checkpoint only step does not satisfy publish requirement`() {
        val result = validateRecipeForPublish(
            title = "Soup",
            ingredients = listOf(validIngredient()),
            steps = listOf(StepInputState(checkpoint = "The soup tastes balanced.")),
        )

        assertTrue(result.missingSteps)
        assertFalse(result.isValid)
    }

    @Test
    fun `warning only step does not satisfy publish requirement`() {
        val result = validateRecipeForPublish(
            title = "Soup",
            ingredients = listOf(validIngredient()),
            steps = listOf(StepInputState(warning = "Do not burn the garlic.")),
        )

        assertTrue(result.missingSteps)
        assertFalse(result.isValid)
    }

    @Test
    fun `equipment only step does not satisfy publish requirement`() {
        val result = validateRecipeForPublish(
            title = "Soup",
            ingredients = listOf(validIngredient()),
            steps = listOf(StepInputState(equipment = "Dutch oven")),
        )

        assertTrue(result.missingSteps)
        assertFalse(result.isValid)
    }

    @Test
    fun `multiple empty placeholders remain invalid`() {
        val result = validateRecipeForPublish(
            title = "Soup",
            ingredients = listOf(validIngredient()),
            steps = List(3) { StepInputState() },
        )

        assertTrue(result.missingSteps)
        assertFalse(result.isValid)
    }

    @Test
    fun `whitespace only title and instruction are not publishable`() {
        val result = validateRecipeForPublish(
            title = "Soup",
            ingredients = listOf(validIngredient()),
            steps = listOf(StepInputState(title = " \u00A0 ", instruction = "\u00A0\u00A0")),
        )

        assertTrue(result.missingSteps)
        assertFalse(result.isValid)
    }

    @Test
    fun `loaded persisted title only steps satisfy validation`() {
        val loadedSteps = listOf(
            StepInputState(localId = "persisted-1", title = "Cook until tender"),
            StepInputState(localId = "persisted-2", title = "Serve warm"),
        )

        val result = validateRecipeForPublish("Stew", listOf(validIngredient()), loadedSteps)

        assertFalse(result.missingSteps)
        assertTrue(result.isValid)
    }

    @Test
    fun `reordered title only steps still satisfy validation`() {
        val reorderedSteps = listOf(
            StepInputState(localId = "step-3", title = "Serve"),
            StepInputState(localId = "step-1"),
            StepInputState(localId = "step-2"),
        )

        val result = validateRecipeForPublish("Stew", listOf(validIngredient()), reorderedSteps)

        assertFalse(result.missingSteps)
        assertTrue(result.isValid)
    }

    @Test
    fun `adding a title clears missing step validation`() {
        val invalid = validateRecipeForPublish(
            title = "Soup",
            ingredients = listOf(validIngredient()),
            steps = listOf(StepInputState(timerMinutes = "5")),
        )
        val valid = validateRecipeForPublish(
            title = "Soup",
            ingredients = listOf(validIngredient()),
            steps = listOf(StepInputState(timerMinutes = "5", title = "Simmer gently")),
        )

        assertTrue(invalid.missingSteps)
        assertFalse(valid.missingSteps)
        assertNull(valid.stepErrorMessage)
    }

    @Test
    fun `removing the only title or instruction restores missing step validation`() {
        val valid = validateRecipeForPublish(
            title = "Soup",
            ingredients = listOf(validIngredient()),
            steps = listOf(StepInputState(title = "Cook until tender")),
        )
        val invalid = validateRecipeForPublish(
            title = "Soup",
            ingredients = listOf(validIngredient()),
            steps = listOf(StepInputState(checkpoint = "Tender")),
        )

        assertFalse(valid.missingSteps)
        assertTrue(invalid.missingSteps)
    }

    @Test
    fun `add recipe and edit recipe use identical validation function`() {
        val ingredients = listOf(validIngredient())
        val steps = listOf(StepInputState(title = "Roast until golden"))

        val addResult = validateRecipeForPublish("Potatoes", ingredients, steps)
        val editResult = validateRecipeForPublish("Potatoes", ingredients, steps)

        assertTrue(addResult.isValid)
        assertTrue(editResult.isValid)
        assertFalse(addResult.missingSteps)
        assertFalse(editResult.missingSteps)
    }

    @Test
    fun `published revision update recognizes title only steps`() {
        val revisionSteps = listOf(
            StepInputState(localId = "revision-step-1", title = "Bloom the spices"),
            StepInputState(localId = "revision-step-2", meanwhile = "Warm plates."),
        )

        val result = validateRecipeForPublish("Dal", listOf(validIngredient("Lentils")), revisionSteps)

        assertFalse(result.missingSteps)
        assertTrue(result.isValid)
    }

    private fun validIngredient(name: String = "Onions") = IngredientInputState(query = name)
}
