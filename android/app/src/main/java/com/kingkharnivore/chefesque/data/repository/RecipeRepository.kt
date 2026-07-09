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
    suspend fun searchActiveRecipesByTitle(query: String): List<RecipeEntity> = recipeDao.searchActiveRecipesByTitle(query)
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
    suspend fun countUsagesOfRecipeAsComponent(recipeId: String): Int = componentDao.countUsagesOfRecipeAsComponent(recipeId)
    suspend fun upsertRecipeComponents(components: List<RecipeComponentEntity>) = componentDao.upsertRecipeComponents(components)

    fun observeStepsForRecipe(recipeId: String): Flow<List<RecipeStepEntity>> = stepDao.observeStepsForRecipe(recipeId)
    suspend fun getStepsForRecipe(recipeId: String): List<RecipeStepEntity> = stepDao.getStepsForRecipe(recipeId)
    suspend fun upsertRecipeSteps(steps: List<RecipeStepEntity>) = stepDao.upsertRecipeSteps(steps)
    suspend fun getIngredientLinksForSteps(stepIds: List<String>): List<StepIngredientLinkEntity> = stepDao.getIngredientLinksForSteps(stepIds)

    suspend fun saveRecipeGraph(
        recipe: RecipeEntity,
        ingredients: List<RecipeIngredientEntity>,
        components: List<RecipeComponentEntity>,
        steps: List<RecipeStepEntity>,
        stepIngredientLinks: List<StepIngredientLinkEntity>,
    ) = database.withTransaction {
        val existingStepIds = stepDao.getStepsForRecipe(recipe.id).map { it.id }
        if (existingStepIds.isNotEmpty()) stepDao.deleteLinksForSteps(existingStepIds)

        recipeDao.upsertRecipe(recipe)
        ingredientDao.deleteIngredientsForRecipe(recipe.id)
        componentDao.deleteComponentsForRecipe(recipe.id)
        stepDao.deleteStepsForRecipe(recipe.id)

        ingredientDao.upsertRecipeIngredients(ingredients)
        componentDao.upsertRecipeComponents(components)
        stepDao.upsertRecipeSteps(steps)
        stepDao.upsertStepIngredientLinks(stepIngredientLinks)
    }

    // Warning: this deletes RecipeIngredient rows and can cascade-delete StepIngredientLink rows.
    // Prefer saveRecipeGraph(...) when editing a full recipe.
    suspend fun replaceIngredientsForRecipe(recipeId: String, ingredients: List<RecipeIngredientEntity>) = database.withTransaction {
        ingredientDao.deleteIngredientsForRecipe(recipeId)
        ingredientDao.upsertRecipeIngredients(ingredients)
    }

    // Warning: this deletes RecipeComponent rows. Historical cook-session component statuses use snapshots and nullable links.
    // Prefer saveRecipeGraph(...) when editing a full recipe.
    suspend fun replaceComponentsForRecipe(recipeId: String, components: List<RecipeComponentEntity>) = database.withTransaction {
        componentDao.deleteComponentsForRecipe(recipeId)
        componentDao.upsertRecipeComponents(components)
    }

    // Warning: this deletes RecipeStep rows and their StepIngredientLink rows before inserting replacements.
    // Prefer saveRecipeGraph(...) when editing a full recipe.
    suspend fun replaceStepsForRecipe(recipeId: String, steps: List<RecipeStepEntity>, links: List<StepIngredientLinkEntity>) = database.withTransaction {
        val existingStepIds = stepDao.getStepsForRecipe(recipeId).map { it.id }
        if (existingStepIds.isNotEmpty()) stepDao.deleteLinksForSteps(existingStepIds)
        stepDao.deleteStepsForRecipe(recipeId)
        stepDao.upsertRecipeSteps(steps)
        stepDao.upsertStepIngredientLinks(links)
    }
}
