package com.kingkharnivore.chefesque.ui.screen.addrecipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kingkharnivore.chefesque.data.local.entity.IngredientEntity
import com.kingkharnivore.chefesque.data.local.entity.RecipeEntity
import com.kingkharnivore.chefesque.data.local.entity.RecipeIngredientEntity
import com.kingkharnivore.chefesque.data.repository.IngredientRepository
import com.kingkharnivore.chefesque.data.repository.RecipeRepository
import com.kingkharnivore.chefesque.domain.model.RecipeType
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

private const val IngredientSuggestionLimit = 8

data class AddRecipeUiState(
    val title: String = "",
    val description: String = "",
    val servings: String = "",
    val prepTimeMinutes: String = "",
    val cookTimeMinutes: String = "",
    val recipeType: RecipeType = RecipeType.FULL_DISH,
    val notes: String = "",
    val ingredients: List<IngredientInputState> = emptyList(),
    val isSaving: Boolean = false,
    val titleError: String? = null,
    val servingsError: String? = null,
    val prepTimeError: String? = null,
    val cookTimeError: String? = null,
    val ingredientError: String? = null,
    val saveError: String? = null,
    val savedRecipeId: String? = null,
)

data class IngredientInputState(
    val localId: String,
    val query: String = "",
    val selectedIngredientId: String? = null,
    val selectedIngredientDisplayName: String? = null,
    val suggestions: List<IngredientEntity> = emptyList(),
    val quantityText: String = "",
    val unit: String = "",
    val prepNote: String = "",
    val section: String = "",
    val optional: Boolean = false,
    val isSearching: Boolean = false,
)

class AddRecipeViewModel(
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddRecipeUiState())
    val uiState: StateFlow<AddRecipeUiState> = _uiState.asStateFlow()
    private val searchJobs = mutableMapOf<String, Job>()

    fun updateTitle(value: String) = _uiState.update { it.copy(title = value, titleError = null, saveError = null) }
    fun updateDescription(value: String) = _uiState.update { it.copy(description = value) }
    fun updateServings(value: String) = _uiState.update { it.copy(servings = value, servingsError = null) }
    fun updatePrepTimeMinutes(value: String) = _uiState.update { it.copy(prepTimeMinutes = value, prepTimeError = null) }
    fun updateCookTimeMinutes(value: String) = _uiState.update { it.copy(cookTimeMinutes = value, cookTimeError = null) }
    fun updateRecipeType(value: RecipeType) = _uiState.update { it.copy(recipeType = value) }
    fun updateNotes(value: String) = _uiState.update { it.copy(notes = value) }

    fun addIngredientRow() = _uiState.update {
        it.copy(ingredients = it.ingredients + IngredientInputState(localId = UUID.randomUUID().toString()), ingredientError = null)
    }

    fun removeIngredientRow(localId: String) {
        searchJobs.remove(localId)?.cancel()
        _uiState.update { state -> state.copy(ingredients = state.ingredients.filterNot { it.localId == localId }, ingredientError = null) }
    }

    fun updateIngredientQuery(localId: String, value: String) {
        _uiState.update { state ->
            state.copy(
                ingredients = state.ingredients.map { ingredient ->
                    if (ingredient.localId != localId) ingredient else ingredient.copy(
                        query = value,
                        selectedIngredientId = ingredient.selectedIngredientId.takeIf { value == ingredient.selectedIngredientDisplayName },
                        selectedIngredientDisplayName = ingredient.selectedIngredientDisplayName.takeIf { value == ingredient.selectedIngredientDisplayName },
                        suggestions = if (value.isBlank()) emptyList() else ingredient.suggestions,
                        isSearching = value.isNotBlank(),
                    )
                },
                ingredientError = null,
            )
        }
        searchJobs.remove(localId)?.cancel()
        if (value.isBlank()) {
            _uiState.updateIngredient(localId) { it.copy(suggestions = emptyList(), isSearching = false) }
            return
        }
        searchJobs[localId] = viewModelScope.launch {
            val suggestions = ingredientRepository.searchIngredients(value, IngredientSuggestionLimit)
            _uiState.updateIngredient(localId) { current ->
                if (current.query == value) current.copy(suggestions = suggestions, isSearching = false) else current
            }
        }
    }

    fun selectIngredient(localId: String, ingredient: IngredientEntity) = _uiState.updateIngredient(localId) { row ->
        row.copy(
            query = ingredient.displayName,
            selectedIngredientId = ingredient.id,
            selectedIngredientDisplayName = ingredient.displayName,
            suggestions = emptyList(),
            unit = row.unit.ifBlank { ingredient.defaultUnit.orEmpty() },
            isSearching = false,
        )
    }

    fun updateQuantity(localId: String, value: String) = _uiState.updateIngredient(localId) { it.copy(quantityText = value) }
    fun updateUnit(localId: String, value: String) = _uiState.updateIngredient(localId) { it.copy(unit = value) }
    fun updatePrepNote(localId: String, value: String) = _uiState.updateIngredient(localId) { it.copy(prepNote = value) }
    fun updateSection(localId: String, value: String) = _uiState.updateIngredient(localId) { it.copy(section = value) }
    fun updateOptional(localId: String, value: Boolean) = _uiState.updateIngredient(localId) { it.copy(optional = value) }

    fun saveRecipe() {
        val state = uiState.value
        val trimmedTitle = state.title.trim()
        val servings = state.servings.parsePositiveIntOrNull()
        val prep = state.prepTimeMinutes.parsePositiveIntOrNull()
        val cook = state.cookTimeMinutes.parsePositiveIntOrNull()
        val ingredientValidationError = state.ingredients.firstOrNull { it.query.isBlank() && it.hasAnyIngredientDetail() } != null

        _uiState.update {
            it.copy(
                titleError = if (trimmedTitle.isBlank()) "Recipe name is required." else null,
                servingsError = if (state.servings.isNotBlank() && servings == null) "Servings must be a number." else null,
                prepTimeError = if (state.prepTimeMinutes.isNotBlank() && prep == null) "Prep time must be minutes." else null,
                cookTimeError = if (state.cookTimeMinutes.isNotBlank() && cook == null) "Cook time must be minutes." else null,
                ingredientError = if (ingredientValidationError) "Each ingredient needs a name." else null,
                saveError = null,
            )
        }
        if (trimmedTitle.isBlank() || (state.servings.isNotBlank() && servings == null) || (state.prepTimeMinutes.isNotBlank() && prep == null) || (state.cookTimeMinutes.isNotBlank() && cook == null) || ingredientValidationError) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            runCatching {
                val now = System.currentTimeMillis()
                val recipeId = UUID.randomUUID().toString()
                val recipe = RecipeEntity(
                    id = recipeId,
                    title = trimmedTitle,
                    description = state.description.trimmedOrNull(),
                    servings = servings,
                    prepTimeMinutes = prep,
                    cookTimeMinutes = cook,
                    coverImageUri = null,
                    cuisine = null,
                    difficulty = null,
                    recipeType = state.recipeType.name,
                    notes = state.notes.trimmedOrNull(),
                    createdAt = now,
                    updatedAt = now,
                    archivedAt = null,
                )
                val ingredients = state.ingredients
                    .filter { it.query.isNotBlank() }
                    .mapIndexed { index, row ->
                        val name = row.selectedIngredientDisplayName.takeIf { row.query.trim() == it } ?: row.query.trim()
                        val quantityText = row.quantityText.trimmedOrNull()
                        RecipeIngredientEntity(
                            id = UUID.randomUUID().toString(),
                            recipeId = recipeId,
                            ingredientId = row.selectedIngredientId.takeIf { row.query.trim() == row.selectedIngredientDisplayName },
                            nameSnapshot = name,
                            quantity = quantityText?.toDoubleOrNull(),
                            quantityText = quantityText,
                            unit = row.unit.trimmedOrNull(),
                            prepNote = row.prepNote.trimmedOrNull(),
                            section = row.section.trimmedOrNull(),
                            optional = row.optional,
                            sortOrder = index,
                        )
                    }
                recipeRepository.saveRecipeGraph(recipe, ingredients, emptyList(), emptyList(), emptyList())
                recipeId
            }.onSuccess { savedId ->
                _uiState.update { it.copy(isSaving = false, savedRecipeId = savedId) }
            }.onFailure { throwable ->
                _uiState.update { it.copy(isSaving = false, saveError = throwable.message ?: "Recipe could not be saved.") }
            }
        }
    }
}

class AddRecipeViewModelFactory(
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddRecipeViewModel::class.java)) return AddRecipeViewModel(recipeRepository, ingredientRepository) as T
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

private fun MutableStateFlow<AddRecipeUiState>.updateIngredient(localId: String, transform: (IngredientInputState) -> IngredientInputState) = update { state ->
    state.copy(ingredients = state.ingredients.map { if (it.localId == localId) transform(it) else it })
}

private fun String.trimmedOrNull(): String? = trim().takeIf { it.isNotBlank() }
private fun String.parsePositiveIntOrNull(): Int? = trim().takeIf { it.isNotBlank() }?.toIntOrNull()?.takeIf { it > 0 }
private fun IngredientInputState.hasAnyIngredientDetail(): Boolean = quantityText.isNotBlank() || unit.isNotBlank() || prepNote.isNotBlank() || section.isNotBlank() || optional
