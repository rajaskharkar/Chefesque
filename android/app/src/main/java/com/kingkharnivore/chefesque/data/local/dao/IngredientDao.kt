package com.kingkharnivore.chefesque.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kingkharnivore.chefesque.data.local.entity.IngredientAliasEntity
import com.kingkharnivore.chefesque.data.local.entity.IngredientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {
    @Query("SELECT * FROM ingredients ORDER BY displayName ASC")
    fun observeIngredients(): Flow<List<IngredientEntity>>

    @Query("SELECT * FROM ingredients WHERE id = :id")
    suspend fun getIngredient(id: String): IngredientEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertIngredient(ingredient: IngredientEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertIngredients(ingredients: List<IngredientEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAliases(aliases: List<IngredientAliasEntity>)

    @Query("SELECT COUNT(*) FROM ingredients WHERE source = :source")
    suspend fun countIngredientsBySource(source: String): Int

    @Query("SELECT * FROM ingredient_aliases WHERE ingredientId = :ingredientId ORDER BY alias ASC")
    suspend fun getAliasesForIngredient(ingredientId: String): List<IngredientAliasEntity>

    @Query("""
        SELECT * FROM ingredients
        WHERE displayName LIKE '%' || :query || '%'
           OR canonicalName LIKE '%' || :query || '%'
        ORDER BY
            CASE
                WHEN displayName LIKE :query || '%' THEN 0
                WHEN canonicalName LIKE :query || '%' THEN 1
                ELSE 2
            END,
            displayName ASC
        LIMIT :limit
    """)
    suspend fun searchIngredientsByName(query: String, limit: Int = 25): List<IngredientEntity>

    @Query("""
        SELECT DISTINCT ingredients.* FROM ingredients
        INNER JOIN ingredient_aliases ON ingredient_aliases.ingredientId = ingredients.id
        WHERE ingredient_aliases.alias LIKE '%' || :query || '%'
           OR ingredient_aliases.normalizedAlias LIKE '%' || :query || '%'
        ORDER BY ingredients.displayName ASC
        LIMIT :limit
    """)
    suspend fun searchIngredientsByAlias(query: String, limit: Int = 25): List<IngredientEntity>
}
