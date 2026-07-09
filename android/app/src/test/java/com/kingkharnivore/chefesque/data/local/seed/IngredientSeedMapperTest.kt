package com.kingkharnivore.chefesque.data.local.seed

import com.kingkharnivore.chefesque.data.local.seed.IngredientSeedMapper.toAliases
import com.kingkharnivore.chefesque.data.local.seed.IngredientSeedMapper.toEntity
import com.kingkharnivore.chefesque.domain.model.IngredientSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IngredientSeedMapperTest {
    @Test
    fun toEntity_normalizesHyphenatedCanonicalNameAndTrimsDisplayName() {
        val entity = seedIngredient(
            id = "curated_all_purpose_flour",
            displayName = "  All-purpose flour  ",
            canonicalName = "all-purpose flour",
        ).toEntity(now = 123L)

        assertEquals("All-purpose flour", entity.displayName)
        assertEquals("all purpose flour", entity.canonicalName)
        assertEquals(IngredientSource.CURATED.name, entity.source)
        assertFalse(entity.isUserCreated)
        assertEquals(123L, entity.createdAt)
        assertEquals(123L, entity.updatedAt)
    }

    @Test
    fun toEntity_normalizesAccentedCanonicalName() {
        val entity = seedIngredient(
            id = "curated_jalapeno",
            displayName = "Jalapeño",
            canonicalName = "jalapeño",
        ).toEntity(now = 456L)

        assertEquals("jalapeno", entity.canonicalName)
    }

    @Test
    fun toAliases_normalizesAliasesAndUsesDeterministicIds() {
        val aliases = seedIngredient(
            id = "curated_green_onion",
            displayName = "Green onion",
            canonicalName = "green onion",
            aliases = listOf("Scallion", " scallion ", "Spring-onion"),
        ).toAliases()

        assertEquals(2, aliases.size)
        assertEquals("curated_green_onion_alias_scallion", aliases[0].id)
        assertEquals("scallion", aliases[0].normalizedAlias)
        assertEquals("curated_green_onion_alias_spring_onion", aliases[1].id)
        assertEquals("spring onion", aliases[1].normalizedAlias)
    }

    @Test
    fun seedAsset_hasSafeCuratedIngredientShape() {
        val seedJson = java.io.File("src/main/assets/seed_ingredients.json").readText()
        val seedFile = IngredientSeedDataSource.parse(seedJson)
        val ids = seedFile.ingredients.map { it.id }

        assertTrue(seedFile.ingredients.size >= 150)
        assertEquals(ids.size, ids.toSet().size)
        seedFile.ingredients.forEach { ingredient ->
            assertTrue(ingredient.id.startsWith("curated_"))
            assertTrue(ingredient.displayName.isNotBlank())
            assertTrue(IngredientNormalizer.normalize(ingredient.canonicalName).isNotBlank())
            val normalizedAliases = ingredient.aliases.map { IngredientNormalizer.normalize(it) }
            assertTrue(normalizedAliases.none { it.isBlank() })
            assertEquals(normalizedAliases.size, normalizedAliases.toSet().size)
        }
    }

    private fun seedIngredient(
        id: String,
        displayName: String,
        canonicalName: String,
        aliases: List<String> = emptyList(),
    ) = SeedIngredientDto(
        id = id,
        displayName = displayName,
        canonicalName = canonicalName,
        category = "baking",
        defaultUnit = "cup",
        commonUnits = listOf("cup", "g"),
        aliases = aliases,
    )
}
