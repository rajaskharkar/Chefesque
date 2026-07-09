package com.kingkharnivore.chefesque.data.local.dao

import androidx.room.Delete
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kingkharnivore.chefesque.data.local.entity.RecipeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes WHERE archivedAt IS NULL ORDER BY updatedAt DESC")
    fun observeActiveRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE id = :id")
    fun observeRecipe(id: String): Flow<RecipeEntity?>

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipe(id: String): RecipeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecipe(recipe: RecipeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecipes(recipes: List<RecipeEntity>)

    @Query("UPDATE recipes SET archivedAt = :archivedAt, updatedAt = :updatedAt WHERE id = :id")
    suspend fun archiveRecipe(id: String, archivedAt: Long, updatedAt: Long)

    @Delete
    suspend fun deleteRecipe(recipe: RecipeEntity)
}
