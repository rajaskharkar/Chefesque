package com.kingkharnivore.chefesque.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class IngredientParserTest {
    @Test fun parsesQuantityUnitAndName() {
        val parsed = IngredientParser.parse("2 tbsp olive oil")
        assertEquals("2", parsed.quantityText)
        assertEquals("tbsp", parsed.unit)
        assertEquals("olive oil", parsed.name)
    }

    @Test fun preservesFallbackName() {
        val parsed = IngredientParser.parse("salt to taste")
        assertEquals(null, parsed.quantityText)
        assertEquals(null, parsed.unit)
        assertEquals("salt to taste", parsed.name)
    }

    @Test fun parsesMixedFraction() {
        val parsed = IngredientParser.parse("1 1/2 cups flour")
        assertEquals("1 1/2", parsed.quantityText)
        assertEquals("cups", parsed.unit)
        assertEquals("flour", parsed.name)
    }
}
