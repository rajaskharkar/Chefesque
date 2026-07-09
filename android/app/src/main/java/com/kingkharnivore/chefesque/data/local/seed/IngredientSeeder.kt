package com.kingkharnivore.chefesque.data.local.seed

import com.kingkharnivore.chefesque.data.local.entity.IngredientAliasEntity
import com.kingkharnivore.chefesque.data.local.entity.IngredientEntity
import com.kingkharnivore.chefesque.data.repository.IngredientRepository
import com.kingkharnivore.chefesque.domain.model.IngredientSource

class IngredientSeeder(
    private val seedDataSource: IngredientSeedDataSource,
    private val ingredientRepository: IngredientRepository,
    private val nowProvider: () -> Long = { System.currentTimeMillis() },
) {
    suspend fun seedIfNeeded() {
        if (ingredientRepository.countIngredientsBySource(IngredientSource.CURATED.name) > 0) return
        val seedFile = seedDataSource.loadSeedIngredients()
        val now = nowProvider()
        ingredientRepository.upsertIngredients(seedFile.ingredients.map { it.toEntity(now) })
        ingredientRepository.upsertAliases(seedFile.ingredients.flatMap { it.toAliases() })
    }

    private fun SeedIngredientDto.toEntity(now: Long): IngredientEntity = IngredientEntity(
        id = id,
        displayName = displayName,
        canonicalName = canonicalName,
        category = category,
        defaultUnit = defaultUnit,
        commonUnitsJson = IngredientSeedDataSource.commonUnitsToJson(commonUnits),
        source = IngredientSource.CURATED.name,
        sourceId = id,
        isUserCreated = false,
        createdAt = now,
        updatedAt = now,
    )

    private fun SeedIngredientDto.toAliases(): List<IngredientAliasEntity> = aliases
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinctBy { IngredientNormalizer.normalize(it) }
        .map { alias ->
            val normalized = IngredientNormalizer.normalize(alias)
            IngredientAliasEntity(
                id = "${id}_alias_${normalized.replace(' ', '_')}",
                ingredientId = id,
                alias = alias,
                normalizedAlias = normalized,
                language = "en",
            )
        }
}
