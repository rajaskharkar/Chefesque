package com.kingkharnivore.chefesque.ui.screen.recipedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kingkharnivore.chefesque.data.local.entity.RecipeEntity
import com.kingkharnivore.chefesque.data.local.entity.RecipeIngredientEntity
import com.kingkharnivore.chefesque.data.repository.RecipeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class RecipeDetailUiState(
    val isLoading: Boolean = true,
    val recipe: RecipeEntity? = null,
    val ingredients: List<RecipeIngredientEntity> = emptyList(),
    val notFound: Boolean = false,
)

class RecipeDetailViewModel(
    recipeId: String,
    recipeRepository: RecipeRepository,
) : ViewModel() {
    val uiState: StateFlow<RecipeDetailUiState> = combine(
        recipeRepository.observeRecipe(recipeId),
        recipeRepository.observeIngredientsForRecipe(recipeId),
    ) { recipe, ingredients ->
        RecipeDetailUiState(
            isLoading = false,
            recipe = recipe?.takeIf { it.archivedAt == null },
            ingredients = ingredients,
            notFound = recipe == null || recipe.archivedAt != null,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecipeDetailUiState())
}

class RecipeDetailViewModelFactory(
    private val recipeId: String,
    private val recipeRepository: RecipeRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeDetailViewModel::class.java)) {
            return RecipeDetailViewModel(recipeId, recipeRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
