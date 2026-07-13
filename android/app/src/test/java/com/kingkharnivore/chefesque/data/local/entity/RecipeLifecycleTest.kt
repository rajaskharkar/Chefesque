package com.kingkharnivore.chefesque.data.local.entity

import org.junit.Assert.assertEquals
import org.junit.Test

class RecipeLifecycleTest {
    @Test fun lifecycleNamesMatchPersistedValues() {
        assertEquals("DRAFT", RecipeLifecycle.DRAFT.name)
        assertEquals("PUBLISHED", RecipeLifecycle.PUBLISHED.name)
    }

    @Test fun authoringTabNamesMatchPersistedValues() {
        assertEquals("BASIC_INFO", RecipeAuthoringTab.BASIC_INFO.name)
        assertEquals("INGREDIENTS", RecipeAuthoringTab.INGREDIENTS.name)
        assertEquals("STEPS", RecipeAuthoringTab.STEPS.name)
        assertEquals("NOTES", RecipeAuthoringTab.NOTES.name)
    }
}
