package com.kingkharnivore.chefesque.ui.screen.cookalong

import com.kingkharnivore.chefesque.data.local.entity.RecipeIngredientEntity
import java.math.BigDecimal

fun formatCookAlongIngredient(ingredient: RecipeIngredientEntity): String {
    val pieces = buildList {
        ingredient.quantityText?.trim()?.takeIf { it.isNotBlank() }?.let { add(it) }
            ?: ingredient.quantity?.let { add(formatQuantity(it)) }
        ingredient.unit?.trim()?.takeIf { it.isNotBlank() }?.let { add(it) }
        add(ingredient.nameSnapshot.trim().ifBlank { "Ingredient" })
    }
    val base = pieces.joinToString(" ")
    val prepNote = ingredient.prepNote?.trim()?.takeIf { it.isNotBlank() }
    return if (prepNote == null) base else "$base, $prepNote"
}

fun formatCookAlongTimer(seconds: Int?): String? {
    val total = seconds?.takeIf { it > 0 } ?: return null
    val minutes = total / 60
    val remainingSeconds = total % 60
    return when {
        minutes == 0 -> "$remainingSeconds sec"
        remainingSeconds == 0 -> "$minutes min"
        else -> "$minutes min $remainingSeconds sec"
    }
}

fun formatCountdownTime(seconds: Int): String {
    val total = seconds.coerceAtLeast(0)
    val minutes = total / 60
    val remainingSeconds = total % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
}

fun checkpointDisplayText(checkpoint: String?): String? {
    val value = checkpoint?.trim()?.takeIf { it.isNotBlank() } ?: return null
    return if (value.equals("Checkpoint", ignoreCase = true)) "Pause and check before moving on." else value
}

private fun formatQuantity(quantity: Double): String = BigDecimal.valueOf(quantity).stripTrailingZeros().toPlainString()
