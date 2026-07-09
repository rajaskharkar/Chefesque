package com.kingkharnivore.chefesque.data.repository

import com.kingkharnivore.chefesque.data.local.dao.IngredientDao
import com.kingkharnivore.chefesque.data.local.entity.IngredientAliasEntity
import com.kingkharnivore.chefesque.data.local.entity.IngredientEntity
import com.kingkharnivore.chefesque.data.local.seed.IngredientNormalizer
import com.kingkharnivore.chefesque.data.local.seed.IngredientSeedDataSource
import com.kingkharnivore.chefesque.domain.model.IngredientSource
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class IngredientRepository(private val ingredientDao: IngredientDao) {
    fun observeIngredients(): Flow<List<IngredientEntity>> = ingredientDao.observeIngredients()
    suspend fun getIngredient(id: String): IngredientEntity? = ingredientDao.getIngredient(id)
    suspend fun searchIngredientsByName(query: String, limit: Int = 25): List<IngredientEntity> = ingredientDao.searchIngredientsByName(query, limit)
    suspend fun searchIngredientsByAlias(query: String, limit: Int = 25): List<IngredientEntity> = ingredientDao.searchIngredientsByAlias(query, limit)

    suspend fun searchIngredients(query: String, limit: Int = 25): List<IngredientEntity> {
        val normalizedQuery = IngredientNormalizer.normalize(query)
        if (normalizedQuery.isBlank() || limit <= 0) return emptyList()

        val nameResults = ingredientDao.searchIngredientsByName(normalizedQuery, limit * 2)
        val aliasResults = ingredientDao.searchIngredientsByAlias(normalizedQuery, limit * 2)
        val aliasMatchesByIngredientId = aliasResults.associate { ingredient ->
            ingredient.id to ingredientDao.getAliasesForIngredient(ingredient.id).map { it.normalizedAlias }
        }

        return (nameResults + aliasResults)
            .distinctBy { it.id }
            .sortedWith(compareBy<IngredientEntity> { ingredient -> ingredient.searchRank(normalizedQuery, aliasMatchesByIngredientId[ingredient.id].orEmpty()) }
                .thenBy { it.displayName.lowercase() })
            .take(limit)
    }

    suspend fun upsertCustomIngredient(
        displayName: String,
        category: String? = null,
        defaultUnit: String? = null,
        commonUnits: List<String> = emptyList(),
    ): IngredientEntity {
        val now = System.currentTimeMillis()
        val ingredient = IngredientEntity(
            id = "user_${UUID.randomUUID()}",
            displayName = displayName.trim(),
            canonicalName = IngredientNormalizer.normalize(displayName),
            category = category,
            defaultUnit = defaultUnit,
            commonUnitsJson = IngredientSeedDataSource.commonUnitsToJson(commonUnits),
            source = IngredientSource.USER.name,
            sourceId = null,
            isUserCreated = true,
            createdAt = now,
            updatedAt = now,
        )
        upsertIngredient(ingredient)
        return ingredient
    }

    suspend fun countIngredientsBySource(source: String): Int = ingredientDao.countIngredientsBySource(source)
    suspend fun upsertIngredient(ingredient: IngredientEntity) = ingredientDao.upsertIngredient(ingredient)
    suspend fun upsertIngredients(ingredients: List<IngredientEntity>) = ingredientDao.upsertIngredients(ingredients)
    suspend fun upsertAliases(aliases: List<IngredientAliasEntity>) = ingredientDao.upsertAliases(aliases)

    private fun IngredientEntity.searchRank(query: String, aliases: List<String>): Int {
        val display = IngredientNormalizer.normalize(displayName)
        val canonical = IngredientNormalizer.normalize(canonicalName)
        return when {
            display == query || canonical == query -> 0
            display.startsWith(query) -> 1
            canonical.startsWith(query) -> 2
            aliases.any { it == query } -> 3
            aliases.any { it.startsWith(query) } -> 4
            display.contains(query) -> 5
            canonical.contains(query) -> 6
            aliases.any { it.contains(query) } -> 7
            else -> 8
        }
    }
}
