package com.kingkharnivore.chefesque.ui.screen.cookinglogdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kingkharnivore.chefesque.data.local.entity.CookingLogEntity
import com.kingkharnivore.chefesque.data.repository.CookingLogRepository
import com.kingkharnivore.chefesque.data.repository.RecipeRepository
import com.kingkharnivore.chefesque.ui.screen.cookinglog.formatCookingLogDate
import com.kingkharnivore.chefesque.ui.screen.cookinglog.formatCookingLogDuration
import com.kingkharnivore.chefesque.ui.screen.cookinglog.formatCookingLogResult
import com.kingkharnivore.chefesque.ui.screen.cookinglog.formatWouldMakeAgain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

data class CookingLogDetailUiState(
    val isLoading: Boolean = true,
    val notFound: Boolean = false,
    val logId: String = "",
    val recipeId: String? = null,
    val recipeAvailable: Boolean = false,
    val title: String = "",
    val cookedDateText: String = "",
    val durationText: String? = null,
    val resultText: String? = null,
    val wouldMakeAgainText: String? = null,
    val sourceText: String? = null,
    val notesForNextTime: String? = null,
    val changesMade: String? = null,
    val whatWentWell: String? = null,
    val isFavorite: Boolean = false,
)

class CookingLogDetailViewModel(
    logId: String,
    cookingLogRepository: CookingLogRepository,
    recipeRepository: RecipeRepository,
) : ViewModel() {
    val uiState = observeDetailState(logId, cookingLogRepository, recipeRepository)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CookingLogDetailUiState(logId = logId))
}

private fun observeDetailState(
    logId: String,
    cookingLogRepository: CookingLogRepository,
    recipeRepository: RecipeRepository,
): Flow<CookingLogDetailUiState> = flow {
    cookingLogRepository.observeLog(logId).collect { log ->
        if (log == null) {
            emit(CookingLogDetailUiState(isLoading = false, notFound = true, logId = logId))
        } else {
            val recipeExists = log.recipeId?.let { recipeRepository.getRecipe(it) != null } ?: false
            emit(toCookingLogDetailUiState(log = log, recipeExists = recipeExists))
        }
    }
}

fun toCookingLogDetailUiState(
    log: CookingLogEntity?,
    recipeExists: Boolean,
    dateFormatter: (Long) -> String = ::formatCookingLogDate,
): CookingLogDetailUiState {
    if (log == null) return CookingLogDetailUiState(isLoading = false, notFound = true)
    return CookingLogDetailUiState(
        isLoading = false,
        notFound = false,
        logId = log.id,
        recipeId = log.recipeId,
        recipeAvailable = log.recipeId != null && recipeExists,
        title = log.titleSnapshot.trim().ifBlank { "Untitled cook" },
        cookedDateText = dateFormatter(log.cookedAt),
        durationText = formatCookingLogDuration(log.actualDurationSeconds),
        resultText = formatCookingLogResult(log.result),
        wouldMakeAgainText = formatWouldMakeAgain(log.wouldMakeAgain),
        sourceText = if (log.createdFromCookAlong) "Created from Cook Along" else "Manual cooking log",
        notesForNextTime = log.notesForNextTime.cleanedOrNull(),
        changesMade = log.changesMade.cleanedOrNull(),
        whatWentWell = log.whatWentWell.cleanedOrNull(),
        isFavorite = log.isFavorite,
    )
}

private fun String?.cleanedOrNull(): String? = this?.trim()?.takeIf { it.isNotBlank() }

class CookingLogDetailViewModelFactory(
    private val logId: String,
    private val cookingLogRepository: CookingLogRepository,
    private val recipeRepository: RecipeRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CookingLogDetailViewModel::class.java)) {
            return CookingLogDetailViewModel(logId, cookingLogRepository, recipeRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
