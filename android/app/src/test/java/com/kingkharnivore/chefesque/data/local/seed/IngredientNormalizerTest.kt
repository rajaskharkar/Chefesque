package com.kingkharnivore.chefesque.data.local.seed

import org.junit.Assert.assertEquals
import org.junit.Test

class IngredientNormalizerTest {
    @Test
    fun normalize_trimsLowercasesAndCollapsesWhitespace() {
        assertEquals("green onion", IngredientNormalizer.normalize("  Green   Onion "))
    }

    @Test
    fun normalize_turnsSimplePunctuationIntoSpaces() {
        assertEquals("kosher salt", IngredientNormalizer.normalize("Kosher-Salt"))
        assertEquals("garam masala", IngredientNormalizer.normalize("  Garam   Masala!! "))
    }

    @Test
    fun normalize_keepsUsefulWordsAndRemovesAccents() {
        assertEquals("jalapeno pepper", IngredientNormalizer.normalize("Jalapeño pepper"))
        assertEquals("jalapeno", IngredientNormalizer.normalize("Jalapeño"))
        assertEquals("chilli powder", IngredientNormalizer.normalize("Chilli Powder"))
    }

    @Test
    fun normalize_hyphenAndSpaceQueriesMatch() {
        assertEquals(
            IngredientNormalizer.normalize("all-purpose flour"),
            IngredientNormalizer.normalize("all purpose flour"),
        )
    }
}
