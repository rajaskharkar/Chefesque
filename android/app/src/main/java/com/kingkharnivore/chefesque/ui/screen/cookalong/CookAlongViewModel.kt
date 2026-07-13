package com.kingkharnivore.chefesque.ui.screen.cookalong

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kingkharnivore.chefesque.data.local.entity.CookSessionEntity
import com.kingkharnivore.chefesque.data.local.entity.RecipeEntity
import com.kingkharnivore.chefesque.data.local.entity.RecipeLifecycle
import com.kingkharnivore.chefesque.data.local.entity.RecipeIngredientEntity
import com.kingkharnivore.chefesque.data.local.entity.RecipeStepEntity
import com.kingkharnivore.chefesque.data.local.entity.StepIngredientLinkEntity
import com.kingkharnivore.chefesque.data.repository.CookSessionRepository
import com.kingkharnivore.chefesque.data.repository.RecipeRepository
import com.kingkharnivore.chefesque.domain.model.CookSessionStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

data class CookAlongUiState(
    val isLoading: Boolean = true,
    val notFound: Boolean = false,
    val recipe: RecipeEntity? = null,
    val steps: List<CookAlongStepUiModel> = emptyList(),
    val currentStepIndex: Int = 0,
    val timerRemainingSeconds: Int? = null,
    val timerOriginalSeconds: Int? = null,
    val timerStatus: CookAlongTimerStatus = CookAlongTimerStatus.IDLE,
    val sessionId: String? = null,
    val resumedSession: Boolean = false,
    val sessionError: String? = null,
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
    private val cookSessionRepository: CookSessionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CookAlongUiState())
    val uiState: StateFlow<CookAlongUiState> = _uiState
    private var timerJob: Job? = null
    private var activeSessionStartedAt: Long? = null

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
        persistCurrentSessionSnapshot()
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
        persistCurrentSessionSnapshot()
        startTicker()
    }

    fun pauseTimer() {
        if (_uiState.value.timerStatus != CookAlongTimerStatus.RUNNING) return
        timerJob?.cancel()
        _uiState.update { it.copy(timerStatus = CookAlongTimerStatus.PAUSED) }
        persistCurrentSessionSnapshot()
    }

    fun resumeTimer() {
        val current = _uiState.value
        if (current.timerStatus != CookAlongTimerStatus.PAUSED || current.timerRemainingSeconds == null || current.timerRemainingSeconds <= 0) return
        _uiState.update { it.copy(timerStatus = CookAlongTimerStatus.RUNNING) }
        persistCurrentSessionSnapshot()
        startTicker()
    }

    fun resetTimer() {
        timerJob?.cancel()
        _uiState.update { current -> current.withTimerSnapshot(prepareTimerForStep(current.currentStep?.timerSeconds)) }
        persistCurrentSessionSnapshot()
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
        persistCurrentSessionSnapshot()
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
                    if (recipe == null || recipe.archivedAt != null || recipe.lifecycleStatus != RecipeLifecycle.PUBLISHED.name || recipe.sourceRecipeId != null) {
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
                    val sessionRestore = if (stepModels.isEmpty()) null else getOrCreateSessionRestore(recipe, stepModels)
                    _uiState.update { current ->
                        val clampedIndex = current.currentStepIndex.coerceIn(0, (stepModels.size - 1).coerceAtLeast(0))
                        val nextIndex = sessionRestore?.currentStepIndex ?: if (stepModels.isEmpty()) 0 else clampedIndex
                        val timerSnapshot = sessionRestore?.timerSnapshot ?: prepareTimerForStep(stepModels.getOrNull(nextIndex)?.timerSeconds)
                        current.copy(
                            isLoading = false,
                            notFound = false,
                            recipe = recipe,
                            steps = stepModels,
                            currentStepIndex = nextIndex,
                            sessionId = sessionRestore?.sessionId ?: current.sessionId,
                            resumedSession = sessionRestore?.resumed == true,
                            sessionError = null,
                        ).withTimerSnapshot(timerSnapshot)
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
            if (_uiState.value.timerStatus == CookAlongTimerStatus.FINISHED) {
                persistCurrentSessionSnapshot()
            }
        }
    }

    fun leaveCookAlong(onComplete: () -> Unit) {
        timerJob?.cancel()
        _uiState.update { current ->
            if (current.timerStatus == CookAlongTimerStatus.RUNNING) current.copy(timerStatus = CookAlongTimerStatus.PAUSED) else current
        }
        viewModelScope.launch {
            persistSessionSnapshot(_uiState.value)
            onComplete()
        }
    }

    fun finishCookAlong(onCompleted: (String) -> Unit, onFallback: () -> Unit) {
        timerJob?.cancel()
        viewModelScope.launch {
            val state = _uiState.value
            val sessionId = state.sessionId
            if (sessionId == null) {
                onFallback()
                return@launch
            }
            val completedAt = System.currentTimeMillis()
            val startedAt = activeSessionStartedAt ?: completedAt
            cookSessionRepository.completeSession(
                sessionId = sessionId,
                status = CookSessionStatus.COMPLETED,
                completedAt = completedAt,
                actualDurationSeconds = ((completedAt - startedAt) / 1_000).toInt().coerceAtLeast(0),
            )
            _uiState.update { it.copy(sessionId = null, resumedSession = false) }
            onCompleted(sessionId)
        }
    }

    private suspend fun getOrCreateSessionRestore(recipe: RecipeEntity, steps: List<CookAlongStepUiModel>): CookSessionRestore? {
        val existingSessionId = _uiState.value.sessionId
        if (existingSessionId != null) {
            return CookSessionRestore(
                sessionId = existingSessionId,
                currentStepIndex = _uiState.value.currentStepIndex.coerceIn(0, steps.lastIndex),
                timerSnapshot = prepareTimerForStep(steps.getOrNull(_uiState.value.currentStepIndex.coerceIn(0, steps.lastIndex))?.timerSeconds),
                resumed = false,
            )
        }
        val activeSession = cookSessionRepository.getActiveSessionForRecipe(recipe.id)
        if (activeSession != null) {
            activeSessionStartedAt = activeSession.startedAt
            val stepIndex = activeSession.currentStepIndex.coerceIn(0, steps.lastIndex)
            return CookSessionRestore(
                sessionId = activeSession.id,
                currentStepIndex = stepIndex,
                timerSnapshot = restoreTimerSnapshotForStep(activeSession, steps[stepIndex]),
                resumed = true,
            )
        }
        val now = System.currentTimeMillis()
        val firstTimerSnapshot = prepareTimerForStep(steps.firstOrNull()?.timerSeconds)
        val session = CookSessionEntity(
            id = UUID.randomUUID().toString(),
            recipeId = recipe.id,
            titleSnapshot = recipe.title,
            startedAt = now,
            completedAt = null,
            status = CookSessionStatus.ACTIVE.name,
            currentStepIndex = 0,
            actualDurationSeconds = null,
            createdFromRecipe = true,
            timerOriginalSeconds = firstTimerSnapshot.originalSeconds,
            timerRemainingSeconds = firstTimerSnapshot.remainingSeconds,
            timerStatus = firstTimerSnapshot.status.name,
            updatedAt = now,
        )
        cookSessionRepository.upsertSession(session)
        activeSessionStartedAt = session.startedAt
        return CookSessionRestore(
            sessionId = session.id,
            currentStepIndex = 0,
            timerSnapshot = firstTimerSnapshot,
            resumed = false,
        )
    }

    private fun persistCurrentSessionSnapshot() {
        val snapshot = _uiState.value
        viewModelScope.launch { persistSessionSnapshot(snapshot) }
    }

    private suspend fun persistSessionSnapshot(state: CookAlongUiState) {
        val sessionId = state.sessionId ?: return
        val currentStep = state.currentStep
        val timerOriginalSeconds = currentStep?.timerSeconds?.let { state.timerOriginalSeconds }
        val timerRemainingSeconds = currentStep?.timerSeconds?.let { state.timerRemainingSeconds }
        val timerStatus = currentStep?.timerSeconds?.let { state.timerStatus.name }
        cookSessionRepository.updateSessionProgress(
            sessionId = sessionId,
            currentStepIndex = state.currentStepIndex,
            timerOriginalSeconds = timerOriginalSeconds,
            timerRemainingSeconds = timerRemainingSeconds,
            timerStatus = timerStatus,
            updatedAt = System.currentTimeMillis(),
        )
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

private data class CookSessionRestore(
    val sessionId: String,
    val currentStepIndex: Int,
    val timerSnapshot: CookAlongTimerSnapshot,
    val resumed: Boolean,
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
            whileTimerRuns = (step.meanwhile ?: step.whileTimerRuns)?.trim()?.takeIf { it.isNotBlank() },
            checkpoint = checkpointDisplayText(step.checkpoint),
            ingredients = stepIngredients,
        )
    }
}

class CookAlongViewModelFactory(
    private val recipeId: String,
    private val recipeRepository: RecipeRepository,
    private val cookSessionRepository: CookSessionRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CookAlongViewModel::class.java)) return CookAlongViewModel(recipeId, recipeRepository, cookSessionRepository) as T
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

fun restoreTimerSnapshotForStep(
    session: CookSessionEntity,
    step: CookAlongStepUiModel,
): CookAlongTimerSnapshot {
    val stepTimerSeconds = step.timerSeconds ?: return prepareTimerForStep(null)
    if (session.timerOriginalSeconds != stepTimerSeconds) return prepareTimerForStep(stepTimerSeconds)
    val remainingSeconds = session.timerRemainingSeconds?.coerceAtLeast(0) ?: stepTimerSeconds
    val restoredStatus = when (session.timerStatus) {
        CookAlongTimerStatus.RUNNING.name -> CookAlongTimerStatus.PAUSED
        CookAlongTimerStatus.PAUSED.name -> CookAlongTimerStatus.PAUSED
        CookAlongTimerStatus.FINISHED.name -> CookAlongTimerStatus.FINISHED
        else -> CookAlongTimerStatus.IDLE
    }
    return CookAlongTimerSnapshot(
        remainingSeconds = remainingSeconds,
        originalSeconds = stepTimerSeconds,
        status = restoredStatus,
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
