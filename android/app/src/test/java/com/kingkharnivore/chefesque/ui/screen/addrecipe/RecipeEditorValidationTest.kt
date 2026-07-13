package com.kingkharnivore.chefesque.ui.screen.addrecipe

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipeEditorValidationTest {
    @Test
    fun `one instructed step satisfies publish requirement`() {
        val result = validateRecipeForPublish(
            title = "Onion Curry",
            ingredients = listOf(validIngredient()),
            steps = listOf(StepInputState(instruction = "Heat the oil.")),
        )

        assertFalse(result.missingSteps)
        assertTrue(result.isValid)
    }

    @Test
    fun `multiple instructed steps satisfy publish requirement`() {
        val steps = listOf(
            StepInputState(instruction = "Heat the oil."),
            StepInputState(instruction = "Add the onions."),
            StepInputState(instruction = "Simmer for ten minutes."),
        )

        val result = validateRecipeForPublish(
            title = "Onion Curry",
            ingredients = listOf(validIngredient()),
            steps = steps,
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
    fun `title only step does not satisfy publish requirement`() {
        val result = validateRecipeForPublish(
            title = "Soup",
            ingredients = listOf(validIngredient()),
            steps = listOf(StepInputState(title = "Start the soup")),
        )

        assertTrue(result.missingSteps)
        assertFalse(result.isValid)
        assertTrue(result.hasStepContent)
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
        assertTrue(result.hasStepContent)
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
        assertTrue(result.hasStepContent)
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
        assertTrue(result.hasStepContent)
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
        assertFalse(result.hasStepContent)
    }

    @Test
    fun `one valid step plus incomplete step cards remains valid`() {
        val result = validateRecipeForPublish(
            title = "Soup",
            ingredients = listOf(validIngredient()),
            steps = listOf(
                StepInputState(title = "Optional prep note"),
                StepInputState(instruction = "Bring the soup to a simmer."),
                StepInputState(timerMinutes = "5"),
            ),
        )

        assertFalse(result.missingSteps)
        assertTrue(result.isValid)
    }

    @Test
    fun `loaded persisted steps satisfy validation`() {
        val loadedSteps = listOf(
            StepInputState(localId = "persisted-1", title = "Cook", instruction = "Cook until tender."),
            StepInputState(localId = "persisted-2", instruction = "Serve warm."),
        )

        val result = validateRecipeForPublish("Stew", listOf(validIngredient()), loadedSteps)

        assertFalse(result.missingSteps)
        assertTrue(result.isValid)
    }

    @Test
    fun `reordered steps still satisfy validation`() {
        val reorderedSteps = listOf(
            StepInputState(localId = "step-3", instruction = "Serve."),
            StepInputState(localId = "step-1", instruction = "Prep."),
            StepInputState(localId = "step-2", instruction = "Cook."),
        )

        val result = validateRecipeForPublish("Stew", listOf(validIngredient()), reorderedSteps)

        assertFalse(result.missingSteps)
        assertTrue(result.isValid)
    }

    @Test
    fun `adding an instruction clears missing step validation`() {
        val invalid = validateRecipeForPublish(
            title = "Soup",
            ingredients = listOf(validIngredient()),
            steps = listOf(StepInputState(title = "Cook")),
        )
        val valid = validateRecipeForPublish(
            title = "Soup",
            ingredients = listOf(validIngredient()),
            steps = listOf(StepInputState(title = "Cook", instruction = "Cook until tender.")),
        )

        assertTrue(invalid.missingSteps)
        assertFalse(valid.missingSteps)
        assertNull(valid.stepErrorMessage)
    }

    @Test
    fun `removing the only valid instruction restores missing step validation`() {
        val valid = validateRecipeForPublish(
            title = "Soup",
            ingredients = listOf(validIngredient()),
            steps = listOf(StepInputState(instruction = "Cook until tender.")),
        )
        val invalid = validateRecipeForPublish(
            title = "Soup",
            ingredients = listOf(validIngredient()),
            steps = listOf(StepInputState(title = "Cook")),
        )

        assertFalse(valid.missingSteps)
        assertTrue(invalid.missingSteps)
        assertTrue(invalid.hasStepContent)
    }

    @Test
    fun `add recipe and edit recipe use identical validation function`() {
        val ingredients = listOf(validIngredient())
        val steps = listOf(StepInputState(instruction = "Roast until golden."))

        val addResult = validateRecipeForPublish("Potatoes", ingredients, steps)
        val editResult = validateRecipeForPublish("Potatoes", ingredients, steps)

        assertTrue(addResult.isValid)
        assertTrue(editResult.isValid)
        assertFalse(addResult.missingSteps)
        assertFalse(editResult.missingSteps)
    }

    @Test
    fun `published revision update recognizes valid steps`() {
        val revisionSteps = listOf(
            StepInputState(localId = "revision-step-1", title = "Bloom spices", instruction = "Bloom the spices for thirty seconds."),
            StepInputState(localId = "revision-step-2", meanwhile = "Warm plates."),
        )

        val result = validateRecipeForPublish("Dal", listOf(validIngredient("Lentils")), revisionSteps)

        assertFalse(result.missingSteps)
        assertTrue(result.isValid)
    }

    @Test
    fun `non breaking spaces alone do not count as visible instruction`() {
        val result = validateRecipeForPublish(
            title = "Soup",
            ingredients = listOf(validIngredient()),
            steps = listOf(StepInputState(instruction = "\u00A0\u00A0")),
        )

        assertTrue(result.missingSteps)
        assertFalse(result.isValid)
    }

    private fun validIngredient(name: String = "Onions") = IngredientInputState(query = name)
}
