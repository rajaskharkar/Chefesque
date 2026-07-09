package com.kingkharnivore.chefesque.data.local.seed

import com.kingkharnivore.chefesque.data.local.seed.IngredientSeedMapper.toAliases
import com.kingkharnivore.chefesque.data.local.seed.IngredientSeedMapper.toEntity
import com.kingkharnivore.chefesque.data.repository.IngredientRepository
import com.kingkharnivore.chefesque.domain.model.IngredientSource

class IngredientSeeder(
    private val seedDataSource: IngredientSeedDataSource,
    private val ingredientRepository: IngredientRepository,
    private val nowProvider: () -> Long = { System.currentTimeMillis() },
) {
    suspend fun seedIfNeeded() {
        // Seeding is skipped only if curated ingredients already exist. Ingredient + alias inserts
        // are transactional so aliases cannot be permanently skipped after a partial failed seed.
        if (ingredientRepository.countIngredientsBySource(IngredientSource.CURATED.name) > 0) return
        val seedFile = seedDataSource.loadSeedIngredients()
        val now = nowProvider()
        ingredientRepository.seedCuratedIngredients(
            ingredients = seedFile.ingredients.map { it.toEntity(now) },
            aliases = seedFile.ingredients.flatMap { it.toAliases() },
        )
    }
}
