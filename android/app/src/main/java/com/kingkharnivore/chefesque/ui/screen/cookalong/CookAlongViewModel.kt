package com.kingkharnivore.chefesque.ui.screen.cookalong

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kingkharnivore.chefesque.data.local.entity.RecipeEntity
import com.kingkharnivore.chefesque.data.local.entity.RecipeIngredientEntity
import com.kingkharnivore.chefesque.data.local.entity.RecipeStepEntity
import com.kingkharnivore.chefesque.data.local.entity.StepIngredientLinkEntity
import com.kingkharnivore.chefesque.data.repository.RecipeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class CookAlongUiState(
    val isLoading: Boolean = true,
    val notFound: Boolean = false,
    val recipe: RecipeEntity? = null,
    val steps: List<CookAlongStepUiModel> = emptyList(),
    val currentStepIndex: Int = 0,
    val timerRemainingSeconds: Int? = null,
    val timerOriginalSeconds: Int? = null,
    val timerStatus: CookAlongTimerStatus = CookAlongTimerStatus.IDLE,
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

enum class CookAlongTimerStatus {
    IDLE,
    RUNNING,
    PAUSED,
    FINISHED,
}

data class CookAlongTimerSnapshot(
    val remainingSeconds: Int?,
    val originalSeconds: Int?,
    val status: CookAlongTimerStatus,
)

class CookAlongViewModel(
    private val recipeId: String,
    private val recipeRepository: RecipeRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CookAlongUiState())
    val uiState: StateFlow<CookAlongUiState> = _uiState
    private var timerJob: Job? = null

    init { loadCookAlongGraph() }

    fun goToNextStep() = goToStep(_uiState.value.currentStepIndex + 1)

    fun goToPreviousStep() = goToStep(_uiState.value.currentStepIndex - 1)

    fun goToStep(index: Int) {
        timerJob?.cancel()
        _uiState.update { current ->
            val maxIndex = (current.steps.size - 1).coerceAtLeast(0)
            val nextIndex = if (current.steps.isEmpty()) 0 else index.coerceIn(0, maxIndex)
            current.copy(currentStepIndex = nextIndex).withTimerSnapshot(prepareTimerForStep(current.steps.getOrNull(nextIndex)?.timerSeconds))
        }
    }

    fun startTimer() {
        val shouldStart = _uiState.value.timerOriginalSeconds != null && _uiState.value.timerStatus == CookAlongTimerStatus.IDLE
        if (!shouldStart) return
        _uiState.update { current ->
            current.copy(
                timerRemainingSeconds = current.timerRemainingSeconds ?: current.timerOriginalSeconds,
                timerStatus = CookAlongTimerStatus.RUNNING,
            )
        }
        startTicker()
    }

    fun pauseTimer() {
        if (_uiState.value.timerStatus != CookAlongTimerStatus.RUNNING) return
        timerJob?.cancel()
        _uiState.update { it.copy(timerStatus = CookAlongTimerStatus.PAUSED) }
    }

    fun resumeTimer() {
        val current = _uiState.value
        if (current.timerStatus != CookAlongTimerStatus.PAUSED || current.timerRemainingSeconds == null || current.timerRemainingSeconds <= 0) return
        _uiState.update { it.copy(timerStatus = CookAlongTimerStatus.RUNNING) }
        startTicker()
    }

    fun resetTimer() {
        timerJob?.cancel()
        _uiState.update { current -> current.withTimerSnapshot(prepareTimerForStep(current.currentStep?.timerSeconds)) }
    }

    fun addOneMinute() {
        var shouldRestartTicker = false
        _uiState.update { current ->
            val snapshot = addOneMinuteToTimer(
                remainingSeconds = current.timerRemainingSeconds,
                originalSeconds = current.timerOriginalSeconds,
                status = current.timerStatus,
            ) ?: return@update current
            shouldRestartTicker = snapshot.status == CookAlongTimerStatus.RUNNING
            current.withTimerSnapshot(snapshot)
        }
        if (shouldRestartTicker) startTicker()
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
                        timerJob?.cancel()
                        _uiState.update {
                            it.copy(isLoading = false, notFound = true, recipe = null, steps = emptyList(), currentStepIndex = 0)
                                .withTimerSnapshot(prepareTimerForStep(null))
                        }
                        return@collectLatest
                    }
                    val links = if (graph.steps.isEmpty()) emptyList() else recipeRepository.getIngredientLinksForSteps(graph.steps.map { it.id })
                    val stepModels = buildCookAlongSteps(graph.steps, graph.ingredients, links)
                    timerJob?.cancel()
                    _uiState.update { current ->
                        val clampedIndex = current.currentStepIndex.coerceIn(0, (stepModels.size - 1).coerceAtLeast(0))
                        val nextIndex = if (stepModels.isEmpty()) 0 else clampedIndex
                        current.copy(
                            isLoading = false,
                            notFound = false,
                            recipe = recipe,
                            steps = stepModels,
                            currentStepIndex = nextIndex,
                        ).withTimerSnapshot(prepareTimerForStep(stepModels.getOrNull(nextIndex)?.timerSeconds))
                    }
                }
        }
    }

    private fun startTicker() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1_000)
                var shouldStop = false
                _uiState.update { state ->
                    if (state.timerStatus != CookAlongTimerStatus.RUNNING) {
                        shouldStop = true
                        return@update state
                    }
                    val remaining = state.timerRemainingSeconds ?: run {
                        shouldStop = true
                        return@update state
                    }
                    val next = (remaining - 1).coerceAtLeast(0)
                    shouldStop = next == 0
                    state.copy(
                        timerRemainingSeconds = next,
                        timerStatus = if (next == 0) CookAlongTimerStatus.FINISHED else CookAlongTimerStatus.RUNNING,
                    )
                }
                if (shouldStop) break
            }
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
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


fun prepareTimerForStep(timerSeconds: Int?): CookAlongTimerSnapshot {
    val original = timerSeconds?.takeIf { it > 0 }
    return CookAlongTimerSnapshot(
        remainingSeconds = original,
        originalSeconds = original,
        status = CookAlongTimerStatus.IDLE,
    )
}

fun addOneMinuteToTimer(
    remainingSeconds: Int?,
    originalSeconds: Int?,
    status: CookAlongTimerStatus,
): CookAlongTimerSnapshot? {
    val original = originalSeconds?.takeIf { it > 0 } ?: return null
    val nextRemaining = when (status) {
        CookAlongTimerStatus.FINISHED -> 60
        else -> (remainingSeconds ?: original) + 60
    }
    val nextStatus = when (status) {
        CookAlongTimerStatus.RUNNING -> CookAlongTimerStatus.RUNNING
        CookAlongTimerStatus.FINISHED -> CookAlongTimerStatus.PAUSED
        CookAlongTimerStatus.IDLE, CookAlongTimerStatus.PAUSED -> status
    }
    return CookAlongTimerSnapshot(
        remainingSeconds = nextRemaining,
        originalSeconds = original,
        status = nextStatus,
    )
}

private fun CookAlongUiState.withTimerSnapshot(snapshot: CookAlongTimerSnapshot): CookAlongUiState = copy(
    timerRemainingSeconds = snapshot.remainingSeconds,
    timerOriginalSeconds = snapshot.originalSeconds,
    timerStatus = snapshot.status,
)
