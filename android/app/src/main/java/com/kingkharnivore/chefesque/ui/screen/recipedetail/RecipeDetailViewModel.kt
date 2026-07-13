package com.kingkharnivore.chefesque.ui.screen.recipedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kingkharnivore.chefesque.data.local.entity.CookingLogEntity
import com.kingkharnivore.chefesque.data.local.entity.RecipeEntity
import com.kingkharnivore.chefesque.data.local.entity.RecipeLifecycle
import com.kingkharnivore.chefesque.data.local.entity.RecipeIngredientEntity
import com.kingkharnivore.chefesque.data.local.entity.RecipeStepEntity
import com.kingkharnivore.chefesque.data.repository.CookingLogRepository
import com.kingkharnivore.chefesque.data.repository.RecipeRepository
import com.kingkharnivore.chefesque.ui.screen.cookinglog.formatCookingLogDate
import com.kingkharnivore.chefesque.ui.screen.cookinglog.formatCookingLogDuration
import com.kingkharnivore.chefesque.ui.screen.cookinglog.formatCookingLogNotesPreview
import com.kingkharnivore.chefesque.ui.screen.cookinglog.formatCookingLogResult
import com.kingkharnivore.chefesque.ui.screen.cookinglog.formatWouldMakeAgain
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class RecipeDetailUiState(
    val isLoading: Boolean = true,
    val recipe: RecipeEntity? = null,
    val ingredients: List<RecipeIngredientEntity> = emptyList(),
    val steps: List<RecipeStepEntity> = emptyList(),
    val notFound: Boolean = false,
    val recentLogs: List<RecipeCookingLogUiModel> = emptyList(),
    val totalLogCount: Int = 0,
    val lastCookedText: String? = null,
)

data class RecipeCookingLogUiModel(
    val id: String,
    val cookedDateText: String,
    val durationText: String?,
    val resultText: String?,
    val wouldMakeAgainText: String?,
    val notesPreview: String?,
    val createdFromCookAlong: Boolean,
    val isFavorite: Boolean,
)

data class RecipeCookingHistoryUi(
    val recentLogs: List<RecipeCookingLogUiModel> = emptyList(),
    val totalLogCount: Int = 0,
    val lastCookedText: String? = null,
)

class RecipeDetailViewModel(
    recipeId: String,
    recipeRepository: RecipeRepository,
    cookingLogRepository: CookingLogRepository,
) : ViewModel() {
    val uiState: StateFlow<RecipeDetailUiState> = combine(
        recipeRepository.observeRecipe(recipeId),
        recipeRepository.observeIngredientsForRecipe(recipeId),
        recipeRepository.observeStepsForRecipe(recipeId),
        cookingLogRepository.observeLogsForRecipe(recipeId),
    ) { recipe, ingredients, steps, logsForRecipe ->
        val cookingHistory = buildRecipeCookingHistoryUi(logsForRecipe)
        RecipeDetailUiState(
            isLoading = false,
            recipe = recipe?.takeIf { it.archivedAt == null && it.lifecycleStatus == RecipeLifecycle.PUBLISHED.name && it.sourceRecipeId == null },
            ingredients = ingredients,
            steps = steps,
            notFound = recipe == null || recipe.archivedAt != null || recipe.lifecycleStatus != RecipeLifecycle.PUBLISHED.name || recipe.sourceRecipeId != null,
            recentLogs = cookingHistory.recentLogs,
            totalLogCount = cookingHistory.totalLogCount,
            lastCookedText = cookingHistory.lastCookedText,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecipeDetailUiState())
}

fun buildRecipeCookingHistoryUi(
    logs: List<CookingLogEntity>,
    dateFormatter: (Long) -> String = ::formatCookingLogDate,
): RecipeCookingHistoryUi {
    if (logs.isEmpty()) return RecipeCookingHistoryUi()
    val sortedLogs = logs.sortedWith(compareByDescending<CookingLogEntity> { it.cookedAt }.thenByDescending { it.createdAt })
    val recentLogs = sortedLogs.take(3).map { toRecipeCookingLogUiModel(it, dateFormatter) }
    return RecipeCookingHistoryUi(
        recentLogs = recentLogs,
        totalLogCount = logs.size,
        lastCookedText = recentLogs.firstOrNull()?.cookedDateText,
    )
}

fun toRecipeCookingLogUiModel(
    log: CookingLogEntity,
    dateFormatter: (Long) -> String = ::formatCookingLogDate,
): RecipeCookingLogUiModel = RecipeCookingLogUiModel(
    id = log.id,
    cookedDateText = dateFormatter(log.cookedAt),
    durationText = formatCookingLogDuration(log.actualDurationSeconds),
    resultText = formatCookingLogResult(log.result),
    wouldMakeAgainText = formatWouldMakeAgain(log.wouldMakeAgain),
    notesPreview = formatCookingLogNotesPreview(log.notesForNextTime, log.changesMade, log.whatWentWell),
    createdFromCookAlong = log.createdFromCookAlong,
    isFavorite = log.isFavorite,
)

class RecipeDetailViewModelFactory(
    private val recipeId: String,
    private val recipeRepository: RecipeRepository,
    private val cookingLogRepository: CookingLogRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeDetailViewModel::class.java)) {
            return RecipeDetailViewModel(recipeId, recipeRepository, cookingLogRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
