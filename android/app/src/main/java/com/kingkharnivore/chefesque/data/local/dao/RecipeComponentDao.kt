package com.kingkharnivore.chefesque.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kingkharnivore.chefesque.data.local.entity.RecipeComponentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeComponentDao {
    @Query("SELECT * FROM recipe_components WHERE parentRecipeId = :recipeId ORDER BY sortOrder ASC")
    fun observeComponentsForRecipe(recipeId: String): Flow<List<RecipeComponentEntity>>

    @Query("SELECT * FROM recipe_components WHERE parentRecipeId = :recipeId ORDER BY sortOrder ASC")
    suspend fun getComponentsForRecipe(recipeId: String): List<RecipeComponentEntity>

    @Query("SELECT * FROM recipe_components WHERE childRecipeId = :recipeId")
    suspend fun getUsagesOfRecipeAsComponent(recipeId: String): List<RecipeComponentEntity>

    @Query("SELECT COUNT(*) FROM recipe_components WHERE childRecipeId = :recipeId")
    suspend fun countUsagesOfRecipeAsComponent(recipeId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecipeComponent(component: RecipeComponentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecipeComponents(components: List<RecipeComponentEntity>)

    @Delete
    suspend fun deleteRecipeComponent(component: RecipeComponentEntity)

    @Query("DELETE FROM recipe_components WHERE parentRecipeId = :recipeId")
    suspend fun deleteComponentsForRecipe(recipeId: String)
}
