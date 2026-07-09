package com.kingkharnivore.chefesque.ui.screen.recipedetail

import com.kingkharnivore.chefesque.data.local.entity.RecipeIngredientEntity
import java.math.BigDecimal

fun recipeMetadataLabels(servings: Int?, prepTimeMinutes: Int?, cookTimeMinutes: Int?): List<String> = buildList {
    servings?.takeIf { it > 0 }?.let { add("Serves $it") }
    prepTimeMinutes?.takeIf { it > 0 }?.let { add("Prep $it min") }
    cookTimeMinutes?.takeIf { it > 0 }?.let { add("Cook $it min") }
}

fun recipeTypeDisplayName(recipeType: String?): String? {
    val normalized = recipeType?.trim()?.takeIf { it.isNotBlank() } ?: return null
    return when (normalized.uppercase()) {
        "FULL_DISH" -> "Full dish"
        "COMPONENT" -> "Component"
        "SAUCE" -> "Sauce"
        "SPICE_BLEND" -> "Spice blend"
        "DOUGH" -> "Dough"
        "MARINADE" -> "Marinade"
        "DRINK" -> "Drink"
        "DESSERT" -> "Dessert"
        "SNACK" -> "Snack"
        "OTHER" -> "Other"
        else -> "Other"
    }
}

fun formatIngredientLine(ingredient: RecipeIngredientEntity): String {
    val pieces = buildList {
        ingredient.quantityText?.trim()?.takeIf { it.isNotBlank() }?.let { add(it) }
            ?: ingredient.quantity?.let { add(formatQuantity(it)) }
        ingredient.unit?.trim()?.takeIf { it.isNotBlank() }?.let { add(it) }
        add(ingredient.nameSnapshot.trim().ifBlank { "Ingredient" })
    }
    val prepNote = ingredient.prepNote?.trim()?.takeIf { it.isNotBlank() }
    return if (prepNote == null) pieces.joinToString(" ") else "${pieces.joinToString(" ")}, $prepNote"
}

fun ingredientSectionGroups(ingredients: List<RecipeIngredientEntity>): List<IngredientSectionGroup> {
    if (ingredients.none { !it.section.isNullOrBlank() }) return listOf(IngredientSectionGroup("Ingredients", ingredients))
    val groups = linkedMapOf<String, MutableList<RecipeIngredientEntity>>()
    ingredients.forEach { ingredient ->
        val section = ingredient.section?.trim()?.takeIf { it.isNotBlank() } ?: "Other"
        groups.getOrPut(section) { mutableListOf() }.add(ingredient)
    }
    return groups.map { IngredientSectionGroup(it.key, it.value) }
}

data class IngredientSectionGroup(val title: String, val ingredients: List<RecipeIngredientEntity>)

private fun formatQuantity(quantity: Double): String = BigDecimal.valueOf(quantity).stripTrailingZeros().toPlainString()


fun formatStepTimer(seconds: Int?): String? {
    val total = seconds?.takeIf { it > 0 } ?: return null
    val minutes = total / 60
    val remainingSeconds = total % 60
    return when {
        minutes == 0 -> "$remainingSeconds sec"
        remainingSeconds == 0 -> "$minutes min"
        else -> "$minutes min $remainingSeconds sec"
    }
}
