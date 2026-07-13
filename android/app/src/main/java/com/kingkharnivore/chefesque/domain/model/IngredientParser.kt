package com.kingkharnivore.chefesque.domain.model

data class ParsedIngredient(
    val originalText: String,
    val quantityText: String?,
    val unit: String?,
    val name: String,
)

object IngredientParser {
    private val knownUnits = setOf("tsp", "tbsp", "cup", "cups", "g", "kg", "oz", "lb", "lbs", "ml", "l", "clove", "cloves")

    fun parse(input: String): ParsedIngredient {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return ParsedIngredient(input, null, null, "")
        val tokens = trimmed.split(Regex("\\s+")).toMutableList()
        val quantity = buildQuantity(tokens)
        val unit = tokens.firstOrNull()?.takeIf { it.lowercase() in knownUnits }?.also { tokens.removeAt(0) }
        val name = tokens.joinToString(" ").ifBlank { trimmed }
        return ParsedIngredient(originalText = input, quantityText = quantity, unit = unit, name = name)
    }

    private fun buildQuantity(tokens: MutableList<String>): String? {
        if (tokens.isEmpty()) return null
        val first = tokens.first()
        val second = tokens.getOrNull(1)
        val isNumber = first.toDoubleOrNull() != null || first.contains('/')
        if (!isNumber) return null
        tokens.removeAt(0)
        return if (second?.contains('/') == true) {
            tokens.removeAt(0)
            "$first $second"
        } else first
    }
}
