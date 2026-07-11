package com.kingkharnivore.chefesque.ui.screen.cookalong

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

    private fun step(id: String, instruction: String) = RecipeStepEntity(id, "recipe-id", instruction, null, null, null, null, null, null, null, 0)

    private fun ingredient(id: String, name: String) = RecipeIngredientEntity(id, "recipe-id", null, name, null, null, null, null, null, false, 0)
}
