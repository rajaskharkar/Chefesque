package com.kingkharnivore.chefesque.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kingkharnivore.chefesque.data.local.entity.RecipeStepEntity
import com.kingkharnivore.chefesque.data.local.entity.StepIngredientLinkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeStepDao {
    @Query("SELECT * FROM recipe_steps WHERE recipeId = :recipeId ORDER BY sortOrder ASC")
    fun observeStepsForRecipe(recipeId: String): Flow<List<RecipeStepEntity>>

    @Query("SELECT * FROM recipe_steps WHERE recipeId = :recipeId ORDER BY sortOrder ASC")
    suspend fun getStepsForRecipe(recipeId: String): List<RecipeStepEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecipeStep(step: RecipeStepEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecipeSteps(steps: List<RecipeStepEntity>)

    @Delete
    suspend fun deleteRecipeStep(step: RecipeStepEntity)

    @Query("DELETE FROM recipe_steps WHERE recipeId = :recipeId")
    suspend fun deleteStepsForRecipe(recipeId: String)

    @Query("SELECT * FROM step_ingredient_links WHERE stepId = :stepId")
    suspend fun getIngredientLinksForStep(stepId: String): List<StepIngredientLinkEntity>

    @Query("SELECT * FROM step_ingredient_links WHERE stepId IN (:stepIds)")
    suspend fun getIngredientLinksForSteps(stepIds: List<String>): List<StepIngredientLinkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStepIngredientLinks(links: List<StepIngredientLinkEntity>)

    @Query("DELETE FROM step_ingredient_links WHERE stepId = :stepId")
    suspend fun deleteLinksForStep(stepId: String)

    @Query("DELETE FROM step_ingredient_links WHERE stepId IN (:stepIds)")
    suspend fun deleteLinksForSteps(stepIds: List<String>)
}
