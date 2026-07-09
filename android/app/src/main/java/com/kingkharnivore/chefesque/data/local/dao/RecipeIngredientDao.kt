package com.kingkharnivore.chefesque.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kingkharnivore.chefesque.data.local.entity.RecipeIngredientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeIngredientDao {
    @Query("SELECT * FROM recipe_ingredients WHERE recipeId = :recipeId ORDER BY sortOrder ASC")
    fun observeIngredientsForRecipe(recipeId: String): Flow<List<RecipeIngredientEntity>>

    @Query("SELECT * FROM recipe_ingredients WHERE recipeId = :recipeId ORDER BY sortOrder ASC")
    suspend fun getIngredientsForRecipe(recipeId: String): List<RecipeIngredientEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecipeIngredient(ingredient: RecipeIngredientEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecipeIngredients(ingredients: List<RecipeIngredientEntity>)

    @Delete
    suspend fun deleteRecipeIngredient(ingredient: RecipeIngredientEntity)

    @Query("DELETE FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun deleteIngredientsForRecipe(recipeId: String)
}
