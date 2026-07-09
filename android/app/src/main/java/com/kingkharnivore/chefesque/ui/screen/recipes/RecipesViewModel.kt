package com.kingkharnivore.chefesque.ui.screen.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kingkharnivore.chefesque.data.local.entity.RecipeEntity
import com.kingkharnivore.chefesque.data.repository.RecipeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class RecipesUiState(
    val recipes: List<RecipeEntity> = emptyList(),
    val isLoading: Boolean = true,
)

class RecipesViewModel(recipeRepository: RecipeRepository) : ViewModel() {
    val uiState: StateFlow<RecipesUiState> = recipeRepository.observeActiveRecipes()
        .map { RecipesUiState(recipes = it, isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecipesUiState())
}

class RecipesViewModelFactory(private val recipeRepository: RecipeRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipesViewModel::class.java)) return RecipesViewModel(recipeRepository) as T
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
