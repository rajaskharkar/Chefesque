package com.kingkharnivore.chefesque.ui.screen.cookalong

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kingkharnivore.chefesque.data.local.entity.RecipeEntity
import com.kingkharnivore.chefesque.data.local.entity.RecipeIngredientEntity
import com.kingkharnivore.chefesque.data.local.entity.RecipeStepEntity
import com.kingkharnivore.chefesque.data.local.entity.StepIngredientLinkEntity
import com.kingkharnivore.chefesque.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CookAlongUiState(
    val isLoading: Boolean = true,
    val notFound: Boolean = false,
    val recipe: RecipeEntity? = null,
    val steps: List<CookAlongStepUiModel> = emptyList(),
    val currentStepIndex: Int = 0,
) {
    val hasSteps: Boolean get() = steps.isNotEmpty()
    val currentStep: CookAlongStepUiModel? get() = steps.getOrNull(currentStepIndex)
    val isFirstStep: Boolean get() = currentStepIndex <= 0
    val isLastStep: Boolean get() = steps.isNotEmpty() && currentStepIndex == steps.lastIndex
}

data class CookAlongStepUiModel(
    val id: String,
    val instruction: String,
    val timerSeconds: Int?,
    val warning: String?,
    val equipment: String?,
    val whileTimerRuns: String?,
    val checkpoint: String?,
    val ingredients: List<CookAlongIngredientUiModel>,
)

data class CookAlongIngredientUiModel(
    val id: String,
    val displayText: String,
    val optional: Boolean,
)

class CookAlongViewModel(
    private val recipeId: String,
    private val recipeRepository: RecipeRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CookAlongUiState())
    val uiState: StateFlow<CookAlongUiState> = _uiState

    init { loadCookAlongGraph() }

    fun goToNextStep() = goToStep(_uiState.value.currentStepIndex + 1)

    fun goToPreviousStep() = goToStep(_uiState.value.currentStepIndex - 1)

    fun goToStep(index: Int) {
        _uiState.update { current ->
            val maxIndex = (current.steps.size - 1).coerceAtLeast(0)
            current.copy(currentStepIndex = if (current.steps.isEmpty()) 0 else index.coerceIn(0, maxIndex))
        }
    }

    private fun loadCookAlongGraph() {
        viewModelScope.launch {
            combine(
                recipeRepository.observeRecipe(recipeId),
                recipeRepository.observeIngredientsForRecipe(recipeId),
                recipeRepository.observeStepsForRecipe(recipeId),
            ) { recipe, ingredients, steps -> RecipeCookGraph(recipe, ingredients, steps) }
                .collectLatest { graph ->
                    val recipe = graph.recipe
                    if (recipe == null || recipe.archivedAt != null) {
                        _uiState.update { it.copy(isLoading = false, notFound = true, recipe = null, steps = emptyList(), currentStepIndex = 0) }
                        return@collectLatest
                    }
                    val links = if (graph.steps.isEmpty()) emptyList() else recipeRepository.getIngredientLinksForSteps(graph.steps.map { it.id })
                    val stepModels = buildCookAlongSteps(graph.steps, graph.ingredients, links)
                    _uiState.update { current ->
                        val clampedIndex = current.currentStepIndex.coerceIn(0, (stepModels.size - 1).coerceAtLeast(0))
                        current.copy(
                            isLoading = false,
                            notFound = false,
                            recipe = recipe,
                            steps = stepModels,
                            currentStepIndex = if (stepModels.isEmpty()) 0 else clampedIndex,
                        )
                    }
                }
        }
    }
}

private data class RecipeCookGraph(
    val recipe: RecipeEntity?,
    val ingredients: List<RecipeIngredientEntity>,
    val steps: List<RecipeStepEntity>,
)

fun buildCookAlongSteps(
    steps: List<RecipeStepEntity>,
    ingredients: List<RecipeIngredientEntity>,
    links: List<StepIngredientLinkEntity>,
): List<CookAlongStepUiModel> {
    val ingredientsById = ingredients.associateBy { it.id }
    val linksByStepId = links.groupBy { it.stepId }
    return steps.map { step ->
        val stepIngredients = linksByStepId[step.id].orEmpty().mapNotNull { link ->
            ingredientsById[link.recipeIngredientId]?.let { ingredient ->
                CookAlongIngredientUiModel(ingredient.id, formatCookAlongIngredient(ingredient), ingredient.optional)
            }
        }
        CookAlongStepUiModel(
            id = step.id,
            instruction = step.instruction.trim().ifBlank { "Step instruction missing." },
            timerSeconds = step.timerSeconds?.takeIf { it > 0 },
            warning = step.warning?.trim()?.takeIf { it.isNotBlank() },
            equipment = step.equipment?.trim()?.takeIf { it.isNotBlank() },
            whileTimerRuns = step.whileTimerRuns?.trim()?.takeIf { it.isNotBlank() },
            checkpoint = checkpointDisplayText(step.checkpoint),
            ingredients = stepIngredients,
        )
    }
}

class CookAlongViewModelFactory(
    private val recipeId: String,
    private val recipeRepository: RecipeRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CookAlongViewModel::class.java)) return CookAlongViewModel(recipeId, recipeRepository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
