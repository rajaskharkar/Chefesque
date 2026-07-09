package com.kingkharnivore.chefesque.data.repository

import com.kingkharnivore.chefesque.data.local.dao.IngredientDao
import com.kingkharnivore.chefesque.data.local.entity.IngredientAliasEntity
import com.kingkharnivore.chefesque.data.local.entity.IngredientEntity
import kotlinx.coroutines.flow.Flow

class IngredientRepository(private val ingredientDao: IngredientDao) {
    fun observeIngredients(): Flow<List<IngredientEntity>> = ingredientDao.observeIngredients()
    suspend fun getIngredient(id: String): IngredientEntity? = ingredientDao.getIngredient(id)
    suspend fun searchIngredientsByName(query: String, limit: Int = 25): List<IngredientEntity> = ingredientDao.searchIngredientsByName(query, limit)
    suspend fun searchIngredientsByAlias(query: String, limit: Int = 25): List<IngredientEntity> = ingredientDao.searchIngredientsByAlias(query, limit)
    suspend fun upsertIngredient(ingredient: IngredientEntity) = ingredientDao.upsertIngredient(ingredient)
    suspend fun upsertIngredients(ingredients: List<IngredientEntity>) = ingredientDao.upsertIngredients(ingredients)
    suspend fun upsertAliases(aliases: List<IngredientAliasEntity>) = ingredientDao.upsertAliases(aliases)
}
