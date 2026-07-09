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
    val logs: List<CookingLogEntity> = emptyList(),
    val isLoading: Boolean = true,
)

class CookingLogViewModel(cookingLogRepository: CookingLogRepository) : ViewModel() {
    val uiState: StateFlow<CookingLogUiState> = cookingLogRepository.observeAllLogs()
        .map { CookingLogUiState(logs = it, isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CookingLogUiState())
}

class CookingLogViewModelFactory(private val cookingLogRepository: CookingLogRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CookingLogViewModel::class.java)) return CookingLogViewModel(cookingLogRepository) as T
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
