package com.kingkharnivore.chefesque.ui.screen.cookalongcompletion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kingkharnivore.chefesque.data.local.entity.CookSessionEntity
import com.kingkharnivore.chefesque.data.local.entity.CookingLogEntity
import com.kingkharnivore.chefesque.data.local.entity.RecipeEntity
import com.kingkharnivore.chefesque.data.repository.CookSessionRepository
import com.kingkharnivore.chefesque.data.repository.CookingLogRepository
import com.kingkharnivore.chefesque.data.repository.RecipeRepository
import com.kingkharnivore.chefesque.domain.model.CookSessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class CookAlongCompletionUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val sessionNotFound: Boolean = false,
    val sessionNotCompleted: Boolean = false,
    val alreadyLogged: Boolean = false,
    val session: CookSessionEntity? = null,
    val recipe: RecipeEntity? = null,
    val title: String = "",
    val completedAtText: String? = null,
    val durationText: String? = null,
    val completedStepsText: String = "Cook Along completed",
    val result: String? = null,
    val wouldMakeAgain: String? = null,
    val notesForNextTime: String = "",
    val saveError: String? = null,
    val savedLogId: String? = null,
)

class CookAlongCompletionViewModel(
    private val sessionId: String,
    private val cookSessionRepository: CookSessionRepository,
    private val cookingLogRepository: CookingLogRepository,
    private val recipeRepository: RecipeRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CookAlongCompletionUiState())
    val uiState: StateFlow<CookAlongCompletionUiState> = _uiState

    init { loadCompletion() }

    fun updateResult(result: String?) {
        _uiState.update { it.copy(result = result, saveError = null) }
    }

    fun updateWouldMakeAgain(wouldMakeAgain: String?) {
        _uiState.update { it.copy(wouldMakeAgain = wouldMakeAgain, saveError = null) }
    }

    fun updateNotesForNextTime(notes: String) {
        _uiState.update { it.copy(notesForNextTime = notes, saveError = null) }
    }

    fun saveCookingLog(onSaved: () -> Unit) {
        val current = _uiState.value
        val session = current.session ?: return _uiState.update { it.copy(saveError = "Cook session not found.") }
        if (current.sessionNotCompleted) return _uiState.update { it.copy(saveError = "This cook is not completed yet.") }
        if (current.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            runCatching {
                val existingLog = cookingLogRepository.getLogForCookSession(session.id)
                if (existingLog != null) {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            alreadyLogged = true,
                            savedLogId = existingLog.id,
                            saveError = null,
                        )
                    }
                    return@launch
                }
                val now = System.currentTimeMillis()
                val log = buildCookAlongCompletionLog(
                    session = session,
                    result = _uiState.value.result,
                    wouldMakeAgain = _uiState.value.wouldMakeAgain,
                    notesForNextTime = _uiState.value.notesForNextTime,
                    now = now,
                )
                cookingLogRepository.upsertLog(log)
                _uiState.update { it.copy(isSaving = false, savedLogId = log.id, saveError = null) }
                onSaved()
            }.onFailure {
                _uiState.update { it.copy(isSaving = false, saveError = "Unable to save cooking log.") }
            }
        }
    }

    private fun loadCompletion() {
        viewModelScope.launch {
            val session = cookSessionRepository.getCookSession(sessionId)
            if (session == null) {
                _uiState.update { it.copy(isLoading = false, sessionNotFound = true) }
                return@launch
            }
            val recipe = session.recipeId?.let { recipeRepository.getRecipe(it) }
            val totalSteps = session.recipeId?.let { recipeRepository.getStepsForRecipe(it).size }
            val existingLog = cookingLogRepository.getLogForCookSession(session.id)
            val title = recipe?.title?.trim()?.takeIf { it.isNotBlank() } ?: session.titleSnapshot
            _uiState.update {
                it.copy(
                    isLoading = false,
                    sessionNotFound = false,
                    sessionNotCompleted = session.status != CookSessionStatus.COMPLETED.name,
                    alreadyLogged = existingLog != null,
                    session = session,
                    recipe = recipe,
                    title = title,
                    completedAtText = formatCompletionDate(session.completedAt),
                    durationText = formatCompletionDuration(session.actualDurationSeconds),
                    completedStepsText = formatCompletedSteps(session.currentStepIndex, totalSteps),
                    savedLogId = existingLog?.id,
                )
            }
        }
    }
}

fun buildCookAlongCompletionLog(
    session: CookSessionEntity,
    result: String?,
    wouldMakeAgain: String?,
    notesForNextTime: String,
    now: Long,
    id: String = UUID.randomUUID().toString(),
): CookingLogEntity = CookingLogEntity(
    id = id,
    recipeId = session.recipeId,
    cookSessionId = session.id,
    titleSnapshot = session.titleSnapshot,
    cookedAt = session.completedAt ?: now,
    actualDurationSeconds = session.actualDurationSeconds,
    result = result,
    wouldMakeAgain = wouldMakeAgain,
    whatWentWell = null,
    changesMade = null,
    notesForNextTime = trimOptionalNotes(notesForNextTime),
    isFavorite = false,
    createdFromCookAlong = true,
    createdAt = now,
    updatedAt = now,
)

class CookAlongCompletionViewModelFactory(
    private val sessionId: String,
    private val cookSessionRepository: CookSessionRepository,
    private val cookingLogRepository: CookingLogRepository,
    private val recipeRepository: RecipeRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CookAlongCompletionViewModel::class.java)) {
            return CookAlongCompletionViewModel(sessionId, cookSessionRepository, cookingLogRepository, recipeRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
