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
    @Query("SELECT * FROM recipes WHERE archivedAt IS NULL AND lifecycleStatus = 'PUBLISHED' AND sourceRecipeId IS NULL ORDER BY updatedAt DESC")
    fun observeActiveRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE id = :id AND archivedAt IS NULL")
    fun observeRecipe(id: String): Flow<RecipeEntity?>

    @Query("SELECT * FROM recipes WHERE id = :id AND archivedAt IS NULL")
    suspend fun getRecipe(id: String): RecipeEntity?

    @Query("SELECT * FROM recipes WHERE title LIKE '%' || :query || '%' AND archivedAt IS NULL AND lifecycleStatus = 'PUBLISHED' AND sourceRecipeId IS NULL ORDER BY updatedAt DESC")
    suspend fun searchActiveRecipesByTitle(query: String): List<RecipeEntity>


    @Query("SELECT * FROM recipes WHERE archivedAt IS NULL AND lifecycleStatus = 'DRAFT' AND sourceRecipeId IS NULL ORDER BY lastEditedAt DESC")
    fun observeDraftRecipes(): Flow<List<RecipeEntity>>

    @Query("UPDATE recipes SET lifecycleStatus = :status, lastEditedAt = :lastEditedAt, publishedAt = :publishedAt, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateLifecycle(id: String, status: String, lastEditedAt: Long, publishedAt: Long?, updatedAt: Long)

    @Query("UPDATE recipes SET lastEditedTab = :tab, lastEditedAt = :lastEditedAt WHERE id = :id")
    suspend fun updateLastEditedTab(id: String, tab: String, lastEditedAt: Long)

    @Query("SELECT * FROM recipes WHERE sourceRecipeId = :publishedRecipeId AND archivedAt IS NULL LIMIT 1")
    suspend fun getRevisionForRecipe(publishedRecipeId: String): RecipeEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM recipes WHERE sourceRecipeId = :publishedRecipeId AND archivedAt IS NULL)")
    suspend fun hasRevisionForRecipe(publishedRecipeId: String): Boolean

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteRecipeById(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecipe(recipe: RecipeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecipes(recipes: List<RecipeEntity>)

    @Query("UPDATE recipes SET archivedAt = :archivedAt, updatedAt = :updatedAt WHERE id = :id")
    suspend fun archiveRecipe(id: String, archivedAt: Long, updatedAt: Long)

    @Delete
    suspend fun deleteRecipe(recipe: RecipeEntity)
}
