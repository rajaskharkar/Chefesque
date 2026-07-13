package com.kingkharnivore.chefesque.data.repository

import androidx.room.withTransaction
import com.kingkharnivore.chefesque.data.local.db.ChefesqueDatabase
import com.kingkharnivore.chefesque.data.local.entity.RecipeComponentEntity
import com.kingkharnivore.chefesque.data.local.entity.RecipeEntity
import com.kingkharnivore.chefesque.data.local.entity.RecipeLifecycle
import com.kingkharnivore.chefesque.data.local.entity.RecipeIngredientEntity
import com.kingkharnivore.chefesque.data.local.entity.RecipeStepEntity
import com.kingkharnivore.chefesque.data.local.entity.StepIngredientLinkEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

data class RecipeGraph(
    val recipe: RecipeEntity,
    val ingredients: List<RecipeIngredientEntity>,
    val components: List<RecipeComponentEntity>,
    val steps: List<RecipeStepEntity>,
    val links: List<StepIngredientLinkEntity>,
)

class RecipeRepository(private val database: ChefesqueDatabase) {
    private val recipeDao = database.recipeDao()
    private val ingredientDao = database.recipeIngredientDao()
    private val componentDao = database.recipeComponentDao()
    private val stepDao = database.recipeStepDao()

    fun observeActiveRecipes(): Flow<List<RecipeEntity>> = recipeDao.observeActiveRecipes()
    fun observeDraftRecipes(): Flow<List<RecipeEntity>> = recipeDao.observeDraftRecipes()
    fun observeRecipe(id: String): Flow<RecipeEntity?> = recipeDao.observeRecipe(id)
    suspend fun getRecipe(id: String): RecipeEntity? = recipeDao.getRecipe(id)
    suspend fun getRecipeGraph(id: String): RecipeGraph? {
        val recipe = recipeDao.getRecipe(id) ?: return null
        val ingredients = ingredientDao.getIngredientsForRecipe(id)
        val components = componentDao.getComponentsForRecipe(id)
        val steps = stepDao.getStepsForRecipe(id)
        val links = stepDao.getIngredientLinksForSteps(steps.map { it.id })
        return RecipeGraph(recipe, ingredients, components, steps, links)
    }
    suspend fun searchActiveRecipesByTitle(query: String): List<RecipeEntity> = recipeDao.searchActiveRecipesByTitle(query)
    suspend fun upsertRecipe(recipe: RecipeEntity) = recipeDao.upsertRecipe(recipe)
    suspend fun upsertRecipes(recipes: List<RecipeEntity>) = recipeDao.upsertRecipes(recipes)
    suspend fun publishRecipe(id: String) {
        val now = System.currentTimeMillis()
        recipeDao.updateLifecycle(id, RecipeLifecycle.PUBLISHED.name, now, now, now)
    }

    suspend fun unpublishRecipe(id: String) {
        val now = System.currentTimeMillis()
        recipeDao.updateLifecycle(id, RecipeLifecycle.DRAFT.name, now, null, now)
    }

    suspend fun updateLastEditedTab(id: String, tab: String) = recipeDao.updateLastEditedTab(id, tab, System.currentTimeMillis())


    suspend fun getOrCreateRevisionForPublishedRecipe(publishedRecipeId: String): String = database.withTransaction {
        recipeDao.getRevisionForRecipe(publishedRecipeId)?.id ?: run {
            val graph = getRecipeGraph(publishedRecipeId) ?: error("Recipe not found")
            require(graph.recipe.lifecycleStatus == RecipeLifecycle.PUBLISHED.name && graph.recipe.sourceRecipeId == null) { "Only published recipes can have revisions." }
            val now = System.currentTimeMillis()
            val revisionId = UUID.randomUUID().toString()
            val ingredientIdMap = graph.ingredients.associate { it.id to UUID.randomUUID().toString() }
            val stepIdMap = graph.steps.associate { it.id to UUID.randomUUID().toString() }
            val revision = graph.recipe.copy(
                id = revisionId,
                lifecycleStatus = RecipeLifecycle.DRAFT.name,
                sourceRecipeId = publishedRecipeId,
                hasUnpublishedChanges = false,
                createdAt = now,
                updatedAt = now,
                lastEditedAt = now,
                publishedAt = null,
            )
            saveRecipeGraph(
                revision,
                graph.ingredients.map { it.copy(id = ingredientIdMap.getValue(it.id), recipeId = revisionId) },
                graph.components.map { it.copy(id = UUID.randomUUID().toString(), parentRecipeId = revisionId) },
                graph.steps.map { it.copy(id = stepIdMap.getValue(it.id), recipeId = revisionId) },
                graph.links.mapNotNull { link ->
                    val stepId = stepIdMap[link.stepId]
                    val ingredientId = ingredientIdMap[link.recipeIngredientId]
                    if (stepId != null && ingredientId != null) StepIngredientLinkEntity(stepId, ingredientId) else null
                },
            )
            recipeDao.upsertRecipe(graph.recipe.copy(hasUnpublishedChanges = true))
            revisionId
        }
    }

    suspend fun applyRevisionToPublishedRecipe(revisionId: String): String = database.withTransaction {
        val revisionGraph = getRecipeGraph(revisionId) ?: error("Revision not found")
        val publishedId = revisionGraph.recipe.sourceRecipeId ?: error("Revision has no published source")
        val published = recipeDao.getRecipe(publishedId) ?: error("Published recipe not found")
        val now = System.currentTimeMillis()
        val ingredientIdMap = revisionGraph.ingredients.associate { it.id to UUID.randomUUID().toString() }
        val stepIdMap = revisionGraph.steps.associate { it.id to UUID.randomUUID().toString() }
        saveRecipeGraph(
            revisionGraph.recipe.copy(
                id = publishedId,
                lifecycleStatus = RecipeLifecycle.PUBLISHED.name,
                sourceRecipeId = null,
                hasUnpublishedChanges = false,
                createdAt = published.createdAt,
                updatedAt = now,
                lastEditedAt = now,
                publishedAt = published.publishedAt ?: now,
            ),
            revisionGraph.ingredients.map { it.copy(id = ingredientIdMap.getValue(it.id), recipeId = publishedId) },
            revisionGraph.components.map { it.copy(id = UUID.randomUUID().toString(), parentRecipeId = publishedId) },
            revisionGraph.steps.map { it.copy(id = stepIdMap.getValue(it.id), recipeId = publishedId) },
            revisionGraph.links.mapNotNull { link ->
                val stepId = stepIdMap[link.stepId]
                val ingredientId = ingredientIdMap[link.recipeIngredientId]
                if (stepId != null && ingredientId != null) StepIngredientLinkEntity(stepId, ingredientId) else null
            },
        )
        recipeDao.deleteRecipeById(revisionId)
        publishedId
    }

    suspend fun discardRevisionForPublishedRecipe(publishedRecipeId: String) = database.withTransaction {
        recipeDao.getRevisionForRecipe(publishedRecipeId)?.let { recipeDao.deleteRecipeById(it.id) }
        recipeDao.getRecipe(publishedRecipeId)?.let { recipeDao.upsertRecipe(it.copy(hasUnpublishedChanges = false)) }
    }

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
