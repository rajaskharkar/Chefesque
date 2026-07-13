package com.kingkharnivore.chefesque.ui.screen.addrecipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kingkharnivore.chefesque.data.local.entity.IngredientEntity
import com.kingkharnivore.chefesque.data.local.entity.RecipeEntity
import com.kingkharnivore.chefesque.data.local.entity.RecipeIngredientEntity
import com.kingkharnivore.chefesque.data.local.entity.RecipeStepEntity
import com.kingkharnivore.chefesque.data.local.entity.StepIngredientLinkEntity
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
    val steps: List<StepInputState> = emptyList(),
    val isSaving: Boolean = false,
    val titleError: String? = null,
    val servingsError: String? = null,
    val prepTimeError: String? = null,
    val cookTimeError: String? = null,
    val ingredientError: String? = null,
    val stepError: String? = null,
    val activeTab: RecipeEditorTab = RecipeEditorTab.BASIC_INFO,
    val autosaveStatus: String = "Draft saved",
    val publishReviewVisible: Boolean = false,
    val saveError: String? = null,
    val savedRecipeId: String? = null,
)

enum class RecipeEditorTab { BASIC_INFO, INGREDIENTS, STEPS, NOTES }

data class IngredientInputState(
    val localId: String = UUID.randomUUID().toString(),
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


data class StepInputState(
    val localId: String = UUID.randomUUID().toString(),
    val instruction: String = "",
    val timerMinutes: String = "",
    val timerSeconds: String = "",
    val warning: String = "",
    val equipment: String = "",
    val whileTimerRuns: String = "",
    val checkpoint: Boolean = false,
    val linkedIngredientLocalIds: Set<String> = emptySet(),
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
    fun selectTab(tab: RecipeEditorTab) = _uiState.update { it.copy(activeTab = tab) }
    fun showPublishReview() = _uiState.update { it.copy(publishReviewVisible = true) }
    fun hidePublishReview() = _uiState.update { it.copy(publishReviewVisible = false) }

    fun addIngredientRow() = _uiState.update {
        it.copy(ingredients = it.ingredients + IngredientInputState(), ingredientError = null)
    }

    fun removeIngredientRow(localId: String) {
        searchJobs.remove(localId)?.cancel()
        _uiState.update { state -> state.copy(
                ingredients = state.ingredients.filterNot { it.localId == localId },
                steps = state.steps.map { step -> step.copy(linkedIngredientLocalIds = step.linkedIngredientLocalIds - localId) },
                ingredientError = null,
            ) }
    }

    fun updateIngredientQuery(localId: String, value: String) {
        _uiState.update { state ->
            state.copy(
                ingredients = state.ingredients.map { ingredient ->
                    if (ingredient.localId != localId) {
                        ingredient
                    } else {
                        val matchesSelected = value.trim() == ingredient.selectedIngredientDisplayName
                        ingredient.copy(
                            query = value,
                            selectedIngredientId = ingredient.selectedIngredientId.takeIf { matchesSelected },
                            selectedIngredientDisplayName = ingredient.selectedIngredientDisplayName.takeIf { matchesSelected },
                            suggestions = if (value.isBlank()) emptyList() else ingredient.suggestions,
                            isSearching = value.isNotBlank(),
                        )
                    }
                },
                ingredientError = null,
            )
        }
        searchJobs.remove(localId)?.cancel()
        if (value.isBlank()) {
            _uiState.updateIngredient(localId) { it.copy(suggestions = emptyList(), isSearching = false) }
            return
        }
        val job = viewModelScope.launch {
            val suggestions = ingredientRepository.searchIngredients(value, IngredientSuggestionLimit)
            _uiState.updateIngredient(localId) { current ->
                if (current.query == value) current.copy(suggestions = suggestions, isSearching = false) else current
            }
        }
        searchJobs[localId] = job
        job.invokeOnCompletion {
            if (searchJobs[localId] == job) {
                searchJobs.remove(localId)
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

    fun addStep() = _uiState.update { it.copy(steps = it.steps + StepInputState(), stepError = null) }

    fun removeStep(localId: String) = _uiState.update { state -> state.copy(steps = state.steps.filterNot { it.localId == localId }, stepError = null) }

    fun moveStepUp(localId: String) = _uiState.update { state -> state.copy(steps = state.steps.moveItem(localId, -1), stepError = null) }

    fun moveStepDown(localId: String) = _uiState.update { state -> state.copy(steps = state.steps.moveItem(localId, 1), stepError = null) }

    fun updateStepInstruction(localId: String, value: String) = _uiState.updateStep(localId) { it.copy(instruction = value) }
    fun updateStepTimerMinutes(localId: String, value: String) = _uiState.updateStep(localId) { it.copy(timerMinutes = value) }
    fun updateStepTimerSeconds(localId: String, value: String) = _uiState.updateStep(localId) { it.copy(timerSeconds = value) }
    fun updateStepWarning(localId: String, value: String) = _uiState.updateStep(localId) { it.copy(warning = value) }
    fun updateStepEquipment(localId: String, value: String) = _uiState.updateStep(localId) { it.copy(equipment = value) }
    fun updateStepWhileTimerRuns(localId: String, value: String) = _uiState.updateStep(localId) { it.copy(whileTimerRuns = value) }
    fun updateStepCheckpoint(localId: String, value: Boolean) = _uiState.updateStep(localId) { it.copy(checkpoint = value) }

    fun toggleStepIngredientLink(stepLocalId: String, ingredientLocalId: String) = _uiState.updateStep(stepLocalId) { step ->
        val links = step.linkedIngredientLocalIds
        step.copy(linkedIngredientLocalIds = if (ingredientLocalId in links) links - ingredientLocalId else links + ingredientLocalId)
    }

    fun publishRecipe() { saveRecipe(publish = true) }

    fun saveRecipe(publish: Boolean = false) {
        if (uiState.value.isSaving) return

        val state = uiState.value
        val trimmedTitle = state.title.trim()
        val servings = state.servings.parsePositiveIntOrNull()
        val prep = state.prepTimeMinutes.parsePositiveIntOrNull()
        val cook = state.cookTimeMinutes.parsePositiveIntOrNull()
        val ingredientValidationError = state.ingredients.firstOrNull { it.query.isBlank() && it.hasAnyIngredientDetail() } != null
        val nonBlankSteps = state.steps.filterNot { it.isBlankStep() }
        val stepInstructionError = nonBlankSteps.any { it.instruction.isBlank() }
        val timerMinutesError = nonBlankSteps.any { it.timerMinutes.trim().takeIf(String::isNotBlank)?.toIntOrNull()?.let { minutes -> minutes < 0 } ?: (it.timerMinutes.isNotBlank()) }
        val timerSecondsError = nonBlankSteps.any { step ->
            step.timerSeconds.trim().takeIf(String::isNotBlank)?.toIntOrNull()?.let { it !in 0..59 } ?: step.timerSeconds.isNotBlank()
        }

        _uiState.update {
            it.copy(
                titleError = if (publish && trimmedTitle.isBlank()) "Add a recipe name before publishing." else null,
                servingsError = if (state.servings.isNotBlank() && servings == null) "Servings must be a number." else null,
                prepTimeError = if (state.prepTimeMinutes.isNotBlank() && prep == null) "Prep time must be minutes." else null,
                cookTimeError = if (state.cookTimeMinutes.isNotBlank() && cook == null) "Cook time must be minutes." else null,
                ingredientError = when {
                    publish && state.ingredients.none { it.query.isNotBlank() } -> "Add at least one ingredient before publishing."
                    ingredientValidationError -> "Each ingredient needs a name."
                    else -> null
                },
                stepError = when {
                    publish && nonBlankSteps.isEmpty() -> "Add at least one step before publishing."
                    stepInstructionError -> "Each step needs an instruction."
                    timerMinutesError -> "Timer minutes must be a number."
                    timerSecondsError -> "Timer seconds must be 0–59."
                    else -> null
                },
                saveError = null,
            )
        }
        if ((publish && trimmedTitle.isBlank()) || (state.servings.isNotBlank() && servings == null) || (state.prepTimeMinutes.isNotBlank() && prep == null) || (state.cookTimeMinutes.isNotBlank() && cook == null) || (publish && state.ingredients.none { it.query.isNotBlank() }) || (publish && nonBlankSteps.isEmpty()) || ingredientValidationError || stepInstructionError || timerMinutesError || timerSecondsError) return

        _uiState.update { it.copy(isSaving = true, saveError = null) }
        viewModelScope.launch {
            runCatching {
                val now = System.currentTimeMillis()
                val recipeId = UUID.randomUUID().toString()
                val recipe = RecipeEntity(
                    id = recipeId,
                    title = trimmedTitle.ifBlank { "Untitled Recipe" },
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
                    lifecycleStatus = if (publish) "PUBLISHED" else "DRAFT",
                    lastEditedAt = now,
                    publishedAt = if (publish) now else null,
                    lastEditedTab = state.activeTab.name,
                )
                val ingredients = state.ingredients
                    .filter { it.query.isNotBlank() }
                    .mapIndexed { index, row ->
                        val name = row.selectedIngredientDisplayName.takeIf { row.query.trim() == it } ?: row.query.trim()
                        val quantityText = row.quantityText.trimmedOrNull()
                        RecipeIngredientEntity(
                            id = row.localId,
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
                val savedIngredientIds = ingredients.map { it.id }.toSet()
                val stepsWithInput = nonBlankSteps.mapIndexed { index, row ->
                    row to RecipeStepEntity(
                        id = row.localId,
                        recipeId = recipeId,
                        instruction = row.instruction.trim(),
                        timerSeconds = row.parsedTimerSeconds(),
                        temperatureValue = null,
                        temperatureUnit = null,
                        checkpoint = if (row.checkpoint) "Checkpoint" else null,
                        warning = row.warning.trimmedOrNull(),
                        equipment = row.equipment.trimmedOrNull(),
                        meanwhile = row.whileTimerRuns.trimmedOrNull(),
                        whileTimerRuns = row.whileTimerRuns.trimmedOrNull(),
                        sortOrder = index,
                    )
                }
                val steps = stepsWithInput.map { it.second }
                val links = stepsWithInput.flatMap { (inputStep, savedStep) ->
                    inputStep.linkedIngredientLocalIds
                        .filter { it in savedIngredientIds }
                        .map { ingredientId -> StepIngredientLinkEntity(stepId = savedStep.id, recipeIngredientId = ingredientId) }
                }
                recipeRepository.saveRecipeGraph(recipe, ingredients, emptyList(), steps, links)
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

private fun MutableStateFlow<AddRecipeUiState>.updateStep(localId: String, transform: (StepInputState) -> StepInputState) = update { state ->
    state.copy(steps = state.steps.map { if (it.localId == localId) transform(it) else it }, stepError = null)
}

private fun List<StepInputState>.moveItem(localId: String, direction: Int): List<StepInputState> {
    val currentIndex = indexOfFirst { it.localId == localId }
    if (currentIndex == -1) return this
    val targetIndex = (currentIndex + direction).coerceIn(indices)
    if (targetIndex == currentIndex) return this
    return toMutableList().also { list ->
        val item = list.removeAt(currentIndex)
        list.add(targetIndex, item)
    }
}

private fun StepInputState.isBlankStep(): Boolean = instruction.isBlank() &&
    timerMinutes.isBlank() &&
    timerSeconds.isBlank() &&
    warning.isBlank() &&
    equipment.isBlank() &&
    whileTimerRuns.isBlank() &&
    !checkpoint &&
    linkedIngredientLocalIds.isEmpty()

private fun StepInputState.parsedTimerSeconds(): Int? {
    val minutes = timerMinutes.trim().takeIf { it.isNotBlank() }?.toIntOrNull() ?: 0
    val seconds = timerSeconds.trim().takeIf { it.isNotBlank() }?.toIntOrNull() ?: 0
    val totalSeconds = minutes * 60 + seconds
    return totalSeconds.takeIf { it > 0 }
}
