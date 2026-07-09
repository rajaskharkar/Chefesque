package com.kingkharnivore.chefesque.data.local.seed

import java.text.Normalizer
import java.util.Locale

object IngredientNormalizer {
    private val punctuationRegex = Regex("[\\p{Punct}]+")
    private val whitespaceRegex = Regex("\\s+")

    fun normalize(input: String): String {
        val withoutAccents = Normalizer.normalize(input, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
        return punctuationRegex.replace(withoutAccents, " ")
            .lowercase(Locale.US)
            .trim()
            .replace(whitespaceRegex, " ")
    }
}
