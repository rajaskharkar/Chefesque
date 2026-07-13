package com.kingkharnivore.chefesque.ui.screen.editrecipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kingkharnivore.chefesque.data.local.entity.IngredientEntity
import com.kingkharnivore.chefesque.data.local.entity.RecipeComponentEntity
import com.kingkharnivore.chefesque.data.local.entity.RecipeEntity
import com.kingkharnivore.chefesque.data.local.entity.RecipeIngredientEntity
import com.kingkharnivore.chefesque.data.local.entity.RecipeLifecycle
import com.kingkharnivore.chefesque.data.local.entity.RecipeStepEntity
import com.kingkharnivore.chefesque.data.local.entity.StepIngredientLinkEntity
import com.kingkharnivore.chefesque.data.repository.IngredientRepository
import com.kingkharnivore.chefesque.data.repository.RecipeRepository
import com.kingkharnivore.chefesque.domain.model.RecipeType
import com.kingkharnivore.chefesque.ui.screen.addrecipe.IngredientInputState
import com.kingkharnivore.chefesque.ui.screen.addrecipe.RecipeEditorTab
import com.kingkharnivore.chefesque.ui.screen.addrecipe.StepInputState
import com.kingkharnivore.chefesque.ui.screen.addrecipe.hasAnyContent
import com.kingkharnivore.chefesque.ui.screen.addrecipe.validateRecipeForPublish
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val IngredientSuggestionLimit = 8

data class EditRecipeUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val notFound: Boolean = false,
    val recipeId: String = "",
    val publishedRecipeId: String? = null,
    val isPublishedRevision: Boolean = false,
    val title: String = "",
    val description: String = "",
    val servings: String = "",
    val prepTimeMinutes: String = "",
    val cookTimeMinutes: String = "",
    val recipeType: RecipeType = RecipeType.FULL_DISH,
    val notes: String = "",
    val ingredients: List<IngredientInputState> = emptyList(),
    val components: List<RecipeComponentEntity> = emptyList(),
    val steps: List<StepInputState> = emptyList(),
    val titleError: String? = null,
    val servingsError: String? = null,
    val prepTimeError: String? = null,
    val cookTimeError: String? = null,
    val ingredientError: String? = null,
    val stepError: String? = null,
    val saveError: String? = null,
    val hasUnsavedChanges: Boolean = false,
    val activeTab: RecipeEditorTab = RecipeEditorTab.BASIC_INFO,
    val autosaveStatus: String = "Not saved yet",
    val publishReviewVisible: Boolean = false,
    val saved: Boolean = false,
    val publishedRecipeEventId: String? = null,
    val updatedRecipeEventId: String? = null,
    val discardedRecipeEventId: String? = null,
)

class EditRecipeViewModel(
    private val routeRecipeId: String,
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditRecipeUiState(recipeId = routeRecipeId))
    val uiState: StateFlow<EditRecipeUiState> = _uiState.asStateFlow()
    private val searchJobs = mutableMapOf<String, Job>()
    private var loadedRecipe: RecipeEntity? = null
    private var originalStepsById: Map<String, RecipeStepEntity> = emptyMap()
    private var saveInFlight = false
    private var saveAgain = false

    init {
        loadRecipeGraph()
        viewModelScope.launch {
            uiState.map { it.persistenceKey() }
                .distinctUntilChanged()
                .drop(1)
                .debounce(750)
                .collect { if (uiState.value.hasMeaningfulContent()) saveCurrent(exitAfterSave = false) }
        }
    }

    private fun loadRecipeGraph() = viewModelScope.launch {
        runCatching {
            val initial = recipeRepository.getRecipe(routeRecipeId) ?: return@runCatching null
            val editRecipeId = if (initial.lifecycleStatus == RecipeLifecycle.PUBLISHED.name && initial.sourceRecipeId == null) {
                recipeRepository.getOrCreateRevisionForPublishedRecipe(initial.id)
            } else {
                initial.id
            }
            recipeRepository.getRecipeGraph(editRecipeId)
        }.onSuccess { graph ->
            if (graph == null) {
                _uiState.update { it.copy(isLoading = false, notFound = true) }
                return@onSuccess
            }
            loadedRecipe = graph.recipe
            originalStepsById = graph.steps.associateBy { it.id }
            val linksByStep = graph.links.groupBy { it.stepId }
            val publishedId = graph.recipe.sourceRecipeId
            _uiState.update {
                it.copy(
                    isLoading = false,
                    recipeId = graph.recipe.id,
                    publishedRecipeId = publishedId,
                    isPublishedRevision = publishedId != null,
                    title = graph.recipe.title,
                    description = graph.recipe.description.orEmpty(),
                    servings = graph.recipe.servings?.toString().orEmpty(),
                    prepTimeMinutes = graph.recipe.prepTimeMinutes?.toString().orEmpty(),
                    cookTimeMinutes = graph.recipe.cookTimeMinutes?.toString().orEmpty(),
                    recipeType = RecipeType.entries.firstOrNull { type -> type.name == graph.recipe.recipeType } ?: RecipeType.FULL_DISH,
                    notes = graph.recipe.notes.orEmpty(),
                    ingredients = graph.ingredients.map { ingredient ->
                        IngredientInputState(
                            localId = ingredient.id,
                            query = ingredient.nameSnapshot,
                            selectedIngredientId = ingredient.ingredientId,
                            selectedIngredientDisplayName = ingredient.nameSnapshot.takeIf { ingredient.ingredientId != null },
                            quantityText = ingredient.quantityText.orEmpty(),
                            unit = ingredient.unit.orEmpty(),
                            prepNote = ingredient.prepNote.orEmpty(),
                            section = ingredient.section.orEmpty(),
                            optional = ingredient.optional,
                        )
                    },
                    components = graph.components,
                    steps = graph.steps.map { step ->
                        val timer = step.timerSeconds.toTimerInputs()
                        StepInputState(
                            localId = step.id,
                            title = step.title.orEmpty(),
                            instruction = step.instruction,
                            timerMinutes = timer.first,
                            timerSeconds = timer.second,
                            warning = step.warning.orEmpty(),
                            equipment = step.equipment.orEmpty(),
                            meanwhile = step.meanwhile ?: step.whileTimerRuns.orEmpty(),
                            checkpoint = step.checkpoint.orEmpty(),
                            linkedIngredientLocalIds = linksByStep[step.id].orEmpty().map { link -> link.recipeIngredientId }.toSet(),
                        )
                    },
                    activeTab = RecipeEditorTab.entries.firstOrNull { tab -> tab.name == graph.recipe.lastEditedTab } ?: RecipeEditorTab.BASIC_INFO,
                    hasUnsavedChanges = false,
                    autosaveStatus = if (publishedId != null) "Changes saved" else "Draft saved",
                )
            }
        }.onFailure { throwable ->
            _uiState.update { it.copy(isLoading = false, saveError = throwable.message ?: "Recipe could not be loaded.") }
        }
    }

    fun selectTab(tab: RecipeEditorTab) {
        flushBeforeTabChange(tab)
    }

    private fun flushBeforeTabChange(tab: RecipeEditorTab) {
        _uiState.update { it.copy(activeTab = tab, hasUnsavedChanges = true) }
        viewModelScope.launch { saveCurrent(exitAfterSave = false) }
    }

    fun updateTitle(value: String) = markChanged { it.copy(title = value, titleError = null, saveError = null) }
    fun updateDescription(value: String) = markChanged { it.copy(description = value) }
    fun updateServings(value: String) = markChanged { it.copy(servings = value, servingsError = null) }
    fun updatePrepTimeMinutes(value: String) = markChanged { it.copy(prepTimeMinutes = value, prepTimeError = null) }
    fun updateCookTimeMinutes(value: String) = markChanged { it.copy(cookTimeMinutes = value, cookTimeError = null) }
    fun updateRecipeType(value: RecipeType) = markChanged { it.copy(recipeType = value) }
    fun updateNotes(value: String) = markChanged { it.copy(notes = value) }
    fun addIngredientRow() = markChanged { it.copy(ingredients = it.ingredients + IngredientInputState(), ingredientError = null) }
    fun removeIngredientRow(localId: String) = markChanged { state -> state.copy(ingredients = state.ingredients.filterNot { it.localId == localId }, steps = state.steps.map { it.copy(linkedIngredientLocalIds = it.linkedIngredientLocalIds - localId) }, ingredientError = null) }

    fun updateIngredientQuery(localId: String, value: String) {
        markChanged { state ->
            state.copy(ingredients = state.ingredients.map { ingredient ->
                if (ingredient.localId != localId) ingredient else ingredient.copy(query = value, selectedIngredientId = null, selectedIngredientDisplayName = null, suggestions = if (value.isBlank()) emptyList() else ingredient.suggestions, isSearching = value.isNotBlank())
            }, ingredientError = null)
        }
        searchJobs.remove(localId)?.cancel()
        if (value.isBlank()) return
        val job = viewModelScope.launch {
            val suggestions = ingredientRepository.searchIngredients(value, IngredientSuggestionLimit)
            _uiState.updateIngredient(localId) { if (it.query == value) it.copy(suggestions = suggestions, isSearching = false) else it }
        }
        searchJobs[localId] = job
    }

    fun selectIngredient(localId: String, ingredient: IngredientEntity) = markIngredient(localId) { it.copy(query = ingredient.displayName, selectedIngredientId = ingredient.id, selectedIngredientDisplayName = ingredient.displayName, suggestions = emptyList(), unit = it.unit.ifBlank { ingredient.defaultUnit.orEmpty() }, isSearching = false) }
    fun updateQuantity(localId: String, value: String) = markIngredient(localId) { it.copy(quantityText = value) }
    fun updateUnit(localId: String, value: String) = markIngredient(localId) { it.copy(unit = value) }
    fun updatePrepNote(localId: String, value: String) = markIngredient(localId) { it.copy(prepNote = value) }
    fun updateSection(localId: String, value: String) = markIngredient(localId) { it.copy(section = value) }
    fun updateOptional(localId: String, value: Boolean) = markIngredient(localId) { it.copy(optional = value) }
    fun addStep() = markChanged { it.copy(steps = it.steps + StepInputState(), stepError = null) }
    fun removeStep(localId: String) = markChanged { it.copy(steps = it.steps.filterNot { step -> step.localId == localId }, stepError = null) }
    fun moveStepUp(localId: String) = markChanged { it.copy(steps = it.steps.moveItem(localId, -1), stepError = null) }
    fun moveStepDown(localId: String) = markChanged { it.copy(steps = it.steps.moveItem(localId, 1), stepError = null) }
    fun updateStepTitle(localId: String, value: String) = markStep(localId) { it.copy(title = value) }
    fun updateStepInstruction(localId: String, value: String) = markStep(localId) { it.copy(instruction = value) }
    fun updateStepTimerMinutes(localId: String, value: String) = markStep(localId) { it.copy(timerMinutes = value) }
    fun updateStepTimerSeconds(localId: String, value: String) = markStep(localId) { it.copy(timerSeconds = value) }
    fun updateStepWarning(localId: String, value: String) = markStep(localId) { it.copy(warning = value) }
    fun updateStepEquipment(localId: String, value: String) = markStep(localId) { it.copy(equipment = value) }
    fun updateStepMeanwhile(localId: String, value: String) = markStep(localId) { it.copy(meanwhile = value) }
    fun updateStepCheckpoint(localId: String, value: String) = markStep(localId) { it.copy(checkpoint = value) }
    fun toggleStepIngredientLink(stepLocalId: String, ingredientLocalId: String) = markStep(stepLocalId) { val links = it.linkedIngredientLocalIds; it.copy(linkedIngredientLocalIds = if (ingredientLocalId in links) links - ingredientLocalId else links + ingredientLocalId) }

    fun saveDraft() = viewModelScope.launch { saveCurrent(exitAfterSave = true) }

    fun requestPublishOrUpdate() {
        if (validateForPublish()) _uiState.update { it.copy(publishReviewVisible = true) }
    }

    fun confirmPublishOrUpdate() = viewModelScope.launch {
        _uiState.update { it.copy(publishReviewVisible = false) }
        if (!saveCurrent(exitAfterSave = false)) return@launch
        val state = uiState.value
        runCatching {
            if (state.isPublishedRevision) recipeRepository.applyRevisionToPublishedRecipe(state.recipeId) else recipeRepository.publishRecipe(state.recipeId).let { state.recipeId }
        }.onSuccess { publishedId ->
            _uiState.update { it.copy(isSaving = false, updatedRecipeEventId = if (state.isPublishedRevision) publishedId else null, publishedRecipeEventId = if (state.isPublishedRevision) null else publishedId, autosaveStatus = "Changes saved") }
        }.onFailure { throwable ->
            _uiState.update { it.copy(isSaving = false, saveError = throwable.message ?: "Recipe could not be updated.", autosaveStatus = "Couldn’t save changes") }
        }
    }

    fun discardRevision() = viewModelScope.launch {
        val publishedId = uiState.value.publishedRecipeId ?: return@launch
        runCatching { recipeRepository.discardRevisionForPublishedRecipe(publishedId) }
            .onSuccess { _uiState.update { it.copy(discardedRecipeEventId = publishedId) } }
            .onFailure { throwable -> _uiState.update { it.copy(saveError = throwable.message ?: "Changes could not be discarded.") } }
    }

    private suspend fun saveCurrent(exitAfterSave: Boolean): Boolean {
        if (saveInFlight) { saveAgain = true; return true }
        val recipe = loadedRecipe ?: return false
        val state = uiState.value
        if (!state.hasMeaningfulContent()) return true
        val servings = state.servings.parsePositiveIntOrNull()
        val prep = state.prepTimeMinutes.parsePositiveIntOrNull()
        val cook = state.cookTimeMinutes.parsePositiveIntOrNull()
        if ((state.servings.isNotBlank() && servings == null) || (state.prepTimeMinutes.isNotBlank() && prep == null) || (state.cookTimeMinutes.isNotBlank() && cook == null)) return false
        saveInFlight = true
        _uiState.update { it.copy(isSaving = true, autosaveStatus = "Saving…") }
        return runCatching {
            val now = System.currentTimeMillis()
            val updatedRecipe = recipe.copy(title = state.title.trim(), description = state.description.trimmedOrNull(), servings = servings, prepTimeMinutes = prep, cookTimeMinutes = cook, recipeType = state.recipeType.name, notes = state.notes.trimmedOrNull(), updatedAt = now, lastEditedAt = now, lastEditedTab = state.activeTab.name)
            recipeRepository.saveRecipeGraph(updatedRecipe, state.toIngredients(recipe.id), state.components, state.toSteps(recipe.id), state.toLinks())
            loadedRecipe = updatedRecipe
        }.onSuccess {
            saveInFlight = false
            _uiState.update { it.copy(isSaving = false, hasUnsavedChanges = false, saved = exitAfterSave, autosaveStatus = if (state.isPublishedRevision) "Changes saved" else "Draft saved") }
            if (saveAgain) { saveAgain = false; saveCurrent(exitAfterSave = false) }
        }.onFailure { throwable ->
            saveInFlight = false
            _uiState.update { it.copy(isSaving = false, autosaveStatus = "Couldn’t save changes", saveError = throwable.message ?: "Recipe could not be saved.") }
        }.isSuccess
    }

    private fun validateForPublish(): Boolean {
        val state = uiState.value
        val validation = validateRecipeForPublish(state.title, state.ingredients, state.steps)
        _uiState.update {
            it.copy(
                activeTab = validation.firstMissingTab ?: it.activeTab,
                titleError = if (validation.missingTitle) "Add a recipe name before publishing." else null,
                ingredientError = if (validation.missingIngredients) "Add at least one ingredient before publishing." else null,
                stepError = validation.stepErrorMessage,
            )
        }
        return validation.isValid
    }

    private fun markChanged(transform: (EditRecipeUiState) -> EditRecipeUiState) = _uiState.update { transform(it).copy(hasUnsavedChanges = true, saved = false, publishedRecipeEventId = null, updatedRecipeEventId = null, discardedRecipeEventId = null) }
    private fun markIngredient(localId: String, transform: (IngredientInputState) -> IngredientInputState) = markChanged { state -> state.copy(ingredients = state.ingredients.map { if (it.localId == localId) transform(it) else it }, ingredientError = null) }
    private fun markStep(localId: String, transform: (StepInputState) -> StepInputState) = markChanged { state -> state.copy(steps = state.steps.map { if (it.localId == localId) transform(it) else it }, stepError = null) }
}

class EditRecipeViewModelFactory(private val recipeId: String, private val recipeRepository: RecipeRepository, private val ingredientRepository: IngredientRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditRecipeViewModel::class.java)) return EditRecipeViewModel(recipeId, recipeRepository, ingredientRepository) as T
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

private fun MutableStateFlow<EditRecipeUiState>.updateIngredient(localId: String, transform: (IngredientInputState) -> IngredientInputState) = update { state -> state.copy(ingredients = state.ingredients.map { if (it.localId == localId) transform(it) else it }) }
private fun String.trimmedOrNull(): String? = trim().takeIf { it.isNotBlank() }
private fun String.parsePositiveIntOrNull(): Int? = trim().takeIf { it.isNotBlank() }?.toIntOrNull()?.takeIf { it > 0 }
private fun IngredientInputState.hasAnyIngredientDetail(): Boolean = quantityText.isNotBlank() || unit.isNotBlank() || prepNote.isNotBlank() || section.isNotBlank() || optional
private fun List<StepInputState>.moveItem(localId: String, direction: Int): List<StepInputState> { val currentIndex = indexOfFirst { it.localId == localId }; if (currentIndex == -1) return this; val targetIndex = (currentIndex + direction).coerceIn(indices); if (targetIndex == currentIndex) return this; return toMutableList().also { val item = it.removeAt(currentIndex); it.add(targetIndex, item) } }
private fun StepInputState.parsedTimerSeconds(): Int? { val minutes = timerMinutes.trim().takeIf { it.isNotBlank() }?.toIntOrNull() ?: 0; val seconds = timerSeconds.trim().takeIf { it.isNotBlank() }?.toIntOrNull() ?: 0; return (minutes * 60 + seconds).takeIf { it > 0 } }
private fun Int?.toTimerInputs(): Pair<String, String> = when { this == null || this <= 0 -> "" to ""; this < 60 -> "" to toString(); this % 60 == 0 -> (this / 60).toString() to ""; else -> (this / 60).toString() to (this % 60).toString() }
private fun EditRecipeUiState.persistenceKey(): String = listOf(recipeId, title, description, servings, prepTimeMinutes, cookTimeMinutes, recipeType.name, notes, activeTab.name, ingredients.map { listOf(it.localId, it.query, it.quantityText, it.unit, it.prepNote, it.section, it.optional) }, components, steps.map { listOf(it.localId, it.title, it.instruction, it.timerMinutes, it.timerSeconds, it.warning, it.equipment, it.meanwhile, it.checkpoint, it.linkedIngredientLocalIds.sorted()) }).joinToString("|")
private fun EditRecipeUiState.hasMeaningfulContent(): Boolean = title.isNotBlank() || description.isNotBlank() || servings.isNotBlank() || prepTimeMinutes.isNotBlank() || cookTimeMinutes.isNotBlank() || notes.isNotBlank() || ingredients.any { it.query.isNotBlank() || it.hasAnyIngredientDetail() } || steps.any { it.hasAnyContent() }
private fun EditRecipeUiState.toIngredients(recipeId: String): List<RecipeIngredientEntity> = ingredients.filter { it.query.isNotBlank() }.mapIndexed { index, row -> val name = row.selectedIngredientDisplayName.takeIf { row.query.trim() == it } ?: row.query.trim(); val quantityText = row.quantityText.trimmedOrNull(); RecipeIngredientEntity(row.localId, recipeId, row.selectedIngredientId.takeIf { row.query.trim() == row.selectedIngredientDisplayName }, name, quantityText?.toDoubleOrNull(), quantityText, row.unit.trimmedOrNull(), row.prepNote.trimmedOrNull(), row.section.trimmedOrNull(), row.optional, index) }
private fun EditRecipeUiState.toSteps(recipeId: String): List<RecipeStepEntity> = steps.filter { it.hasAnyContent() }.mapIndexed { index, row -> RecipeStepEntity(row.localId, recipeId, row.instruction.trim(), row.parsedTimerSeconds(), null, null, row.checkpoint.trimmedOrNull(), row.warning.trimmedOrNull(), row.equipment.trimmedOrNull(), null, index, row.title.trimmedOrNull(), row.meanwhile.trimmedOrNull()) }
private fun EditRecipeUiState.toLinks(): List<StepIngredientLinkEntity> { val ingredientIds = ingredients.filter { it.query.isNotBlank() }.map { it.localId }.toSet(); return steps.filter { it.hasAnyContent() }.flatMap { step -> step.linkedIngredientLocalIds.filter { it in ingredientIds }.map { StepIngredientLinkEntity(step.localId, it) } } }
