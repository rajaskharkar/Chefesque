package com.kingkharnivore.chefesque.ui.screen.cookalong

import com.kingkharnivore.chefesque.data.local.entity.RecipeIngredientEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CookAlongFormattersTest {
    @Test
    fun timerFormatterHidesMissingAndNonPositiveValues() {
        assertNull(formatCookAlongTimer(null))
        assertNull(formatCookAlongTimer(0))
        assertNull(formatCookAlongTimer(-10))
    }

    @Test
    fun timerFormatterFormatsSecondsMinutesAndMixedValues() {
        assertEquals("30 sec", formatCookAlongTimer(30))
        assertEquals("1 min", formatCookAlongTimer(60))
        assertEquals("1 min 30 sec", formatCookAlongTimer(90))
        assertEquals("8 min", formatCookAlongTimer(480))
    }

    @Test
    fun ingredientFormatterUsesQuantityTextUnitNameAndPrep() {
        val ingredient = ingredient(quantityText = "2", unit = "cloves", nameSnapshot = "Garlic", prepNote = "minced")
        assertEquals("2 cloves Garlic, minced", formatCookAlongIngredient(ingredient))
    }

    @Test
    fun ingredientFormatterFallsBackSafely() {
        val ingredient = ingredient(quantityText = " ", quantity = 1.5, unit = " ", nameSnapshot = " ", prepNote = " ")
        assertEquals("1.5 Ingredient", formatCookAlongIngredient(ingredient))
    }

    @Test
    fun checkpointFormatterUsesFriendlyFallbackForMarker() {
        assertEquals("Pause and check before moving on.", checkpointDisplayText("Checkpoint"))
        assertEquals("Check sauce texture.", checkpointDisplayText(" Check sauce texture. "))
    }

    private fun ingredient(
        quantity: Double? = null,
        quantityText: String? = null,
        unit: String? = null,
        nameSnapshot: String = "Ingredient",
        prepNote: String? = null,
    ) = RecipeIngredientEntity(
        id = "ingredient-id",
        recipeId = "recipe-id",
        ingredientId = null,
        nameSnapshot = nameSnapshot,
        quantity = quantity,
        quantityText = quantityText,
        unit = unit,
        prepNote = prepNote,
        section = null,
        optional = false,
        sortOrder = 0,
    )
}
