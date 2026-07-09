package com.kingkharnivore.chefesque.data.repository

import androidx.room.withTransaction
import com.kingkharnivore.chefesque.data.local.db.ChefesqueDatabase
import com.kingkharnivore.chefesque.data.local.entity.RecipeComponentEntity
import com.kingkharnivore.chefesque.data.local.entity.RecipeEntity
import com.kingkharnivore.chefesque.data.local.entity.RecipeIngredientEntity
import com.kingkharnivore.chefesque.data.local.entity.RecipeStepEntity
import com.kingkharnivore.chefesque.data.local.entity.StepIngredientLinkEntity
import kotlinx.coroutines.flow.Flow

class RecipeRepository(private val database: ChefesqueDatabase) {
    private val recipeDao = database.recipeDao()
    private val ingredientDao = database.recipeIngredientDao()
    private val componentDao = database.recipeComponentDao()
    private val stepDao = database.recipeStepDao()

    fun observeActiveRecipes(): Flow<List<RecipeEntity>> = recipeDao.observeActiveRecipes()
    fun observeRecipe(id: String): Flow<RecipeEntity?> = recipeDao.observeRecipe(id)
    suspend fun getRecipe(id: String): RecipeEntity? = recipeDao.getRecipe(id)
    suspend fun upsertRecipe(recipe: RecipeEntity) = recipeDao.upsertRecipe(recipe)
    suspend fun upsertRecipes(recipes: List<RecipeEntity>) = recipeDao.upsertRecipes(recipes)
    suspend fun archiveRecipe(id: String, archivedAt: Long, updatedAt: Long) = recipeDao.archiveRecipe(id, archivedAt, updatedAt)
    suspend fun deleteRecipe(recipe: RecipeEntity) = recipeDao.deleteRecipe(recipe)

    fun observeIngredientsForRecipe(recipeId: String): Flow<List<RecipeIngredientEntity>> = ingredientDao.observeIngredientsForRecipe(recipeId)
    suspend fun getIngredientsForRecipe(recipeId: String): List<RecipeIngredientEntity> = ingredientDao.getIngredientsForRecipe(recipeId)
    suspend fun upsertRecipeIngredients(ingredients: List<RecipeIngredientEntity>) = ingredientDao.upsertRecipeIngredients(ingredients)

    fun observeComponentsForRecipe(recipeId: String): Flow<List<RecipeComponentEntity>> = componentDao.observeComponentsForRecipe(recipeId)
    suspend fun getComponentsForRecipe(recipeId: String): List<RecipeComponentEntity> = componentDao.getComponentsForRecipe(recipeId)
    suspend fun getUsagesOfRecipeAsComponent(recipeId: String): List<RecipeComponentEntity> = componentDao.getUsagesOfRecipeAsComponent(recipeId)
    suspend fun upsertRecipeComponents(components: List<RecipeComponentEntity>) = componentDao.upsertRecipeComponents(components)

    fun observeStepsForRecipe(recipeId: String): Flow<List<RecipeStepEntity>> = stepDao.observeStepsForRecipe(recipeId)
    suspend fun getStepsForRecipe(recipeId: String): List<RecipeStepEntity> = stepDao.getStepsForRecipe(recipeId)
    suspend fun upsertRecipeSteps(steps: List<RecipeStepEntity>) = stepDao.upsertRecipeSteps(steps)
    suspend fun getIngredientLinksForSteps(stepIds: List<String>): List<StepIngredientLinkEntity> = stepDao.getIngredientLinksForSteps(stepIds)

    suspend fun replaceIngredientsForRecipe(recipeId: String, ingredients: List<RecipeIngredientEntity>) = database.withTransaction {
        ingredientDao.deleteIngredientsForRecipe(recipeId)
        ingredientDao.upsertRecipeIngredients(ingredients)
    }

    suspend fun replaceComponentsForRecipe(recipeId: String, components: List<RecipeComponentEntity>) = database.withTransaction {
        componentDao.deleteComponentsForRecipe(recipeId)
        componentDao.upsertRecipeComponents(components)
    }

    suspend fun replaceStepsForRecipe(recipeId: String, steps: List<RecipeStepEntity>, links: List<StepIngredientLinkEntity>) = database.withTransaction {
        val existingStepIds = stepDao.getStepsForRecipe(recipeId).map { it.id }
        if (existingStepIds.isNotEmpty()) stepDao.deleteLinksForSteps(existingStepIds)
        stepDao.deleteStepsForRecipe(recipeId)
        stepDao.upsertRecipeSteps(steps)
        stepDao.upsertStepIngredientLinks(links)
    }
}
