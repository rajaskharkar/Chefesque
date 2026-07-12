package com.kingkharnivore.chefesque.ui.screen.cookinglog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kingkharnivore.chefesque.data.local.entity.CookingLogEntity
import com.kingkharnivore.chefesque.data.repository.CookingLogRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class CookingLogUiState(
    val logs: List<CookingLogCardUiModel> = emptyList(),
    val isLoading: Boolean = true,
)

data class CookingLogCardUiModel(
    val id: String,
    val title: String,
    val cookedDateText: String,
    val durationText: String?,
    val resultText: String?,
    val wouldMakeAgainText: String?,
    val notesPreview: String?,
    val isFavorite: Boolean,
    val createdFromCookAlong: Boolean,
)

class CookingLogViewModel(cookingLogRepository: CookingLogRepository) : ViewModel() {
    val uiState: StateFlow<CookingLogUiState> = cookingLogRepository.observeAllLogs()
        .map { logs -> CookingLogUiState(logs = logs.map(::toCookingLogCardUiModel), isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CookingLogUiState())
}

fun toCookingLogCardUiModel(log: CookingLogEntity): CookingLogCardUiModel = CookingLogCardUiModel(
    id = log.id,
    title = log.titleSnapshot.ifBlank { "Untitled cook" },
    cookedDateText = formatCookingLogDate(log.cookedAt),
    durationText = formatCookingLogDuration(log.actualDurationSeconds),
    resultText = formatCookingLogResult(log.result),
    wouldMakeAgainText = formatWouldMakeAgain(log.wouldMakeAgain),
    notesPreview = formatCookingLogNotesPreview(log.notesForNextTime, log.changesMade, log.whatWentWell),
    isFavorite = log.isFavorite,
    createdFromCookAlong = log.createdFromCookAlong,
)

class CookingLogViewModelFactory(private val cookingLogRepository: CookingLogRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CookingLogViewModel::class.java)) return CookingLogViewModel(cookingLogRepository) as T
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
