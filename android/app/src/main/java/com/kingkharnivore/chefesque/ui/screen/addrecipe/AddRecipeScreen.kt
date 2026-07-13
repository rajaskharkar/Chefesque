package com.kingkharnivore.chefesque.ui.screen.addrecipe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kingkharnivore.chefesque.data.local.entity.IngredientEntity
import com.kingkharnivore.chefesque.domain.model.IngredientSource
import com.kingkharnivore.chefesque.domain.model.RecipeType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeScreen(
    uiState: AddRecipeUiState,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    onSaveComplete: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onServingsChange: (String) -> Unit,
    onPrepTimeChange: (String) -> Unit,
    onCookTimeChange: (String) -> Unit,
    onRecipeTypeChange: (RecipeType) -> Unit,
    onNotesChange: (String) -> Unit,
    onAddIngredient: () -> Unit,
    onRemoveIngredient: (String) -> Unit,
    onIngredientQueryChange: (String, String) -> Unit,
    onIngredientSelected: (String, IngredientEntity) -> Unit,
    onQuantityChange: (String, String) -> Unit,
    onUnitChange: (String, String) -> Unit,
    onPrepNoteChange: (String, String) -> Unit,
    onSectionChange: (String, String) -> Unit,
    onOptionalChange: (String, Boolean) -> Unit,
    onAddStep: () -> Unit,
    onRemoveStep: (String) -> Unit,
    onMoveStepUp: (String) -> Unit,
    onMoveStepDown: (String) -> Unit,
    onStepInstructionChange: (String, String) -> Unit,
    onStepTimerMinutesChange: (String, String) -> Unit,
    onStepTimerSecondsChange: (String, String) -> Unit,
    onStepWarningChange: (String, String) -> Unit,
    onStepEquipmentChange: (String, String) -> Unit,
    onStepWhileTimerRunsChange: (String, String) -> Unit,
    onStepCheckpointChange: (String, Boolean) -> Unit,
    onToggleStepIngredientLink: (String, String) -> Unit,
    onTabSelected: (RecipeEditorTab) -> Unit = {},
    onPublishClick: () -> Unit = onSaveClick,
    onDismissPublishReview: () -> Unit = {},
    modifier: Modifier = Modifier,
    screenTitle: String = "Add Recipe",
    saveActionLabel: String = "Save",
    saveButtonLabel: String = "Save Recipe",
    savingButtonLabel: String = "Saving recipe…",
) {
    LaunchedEffect(uiState.savedRecipeId) {
        if (uiState.savedRecipeId != null) onSaveComplete()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                actions = {
                    Text(uiState.autosaveStatus, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = onSaveClick, enabled = !uiState.isSaving) { Text(if (uiState.isSaving) "Saving" else saveActionLabel) }
                },
            )
        },
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TabRow(selectedTabIndex = uiState.activeTab.ordinal) {
                RecipeEditorTab.entries.forEach { tab ->
                    Tab(selected = uiState.activeTab == tab, onClick = { onTabSelected(tab) }, text = { Text(tab.label()) })
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                when (uiState.activeTab) {
                    RecipeEditorTab.BASIC_INFO -> RecipeBasicInfoSection(uiState, onTitleChange, onDescriptionChange, onServingsChange, onPrepTimeChange, onCookTimeChange, onRecipeTypeChange)
                    RecipeEditorTab.INGREDIENTS -> RecipeIngredientEditorSection(uiState, onAddIngredient, onRemoveIngredient, onIngredientQueryChange, onIngredientSelected, onQuantityChange, onUnitChange, onPrepNoteChange, onSectionChange, onOptionalChange)
                    RecipeEditorTab.STEPS -> RecipeStepEditorSection(uiState, onAddStep, onRemoveStep, onMoveStepUp, onMoveStepDown, onStepInstructionChange, onStepTimerMinutesChange, onStepTimerSecondsChange, onStepWarningChange, onStepEquipmentChange, onStepWhileTimerRunsChange, onStepCheckpointChange, onToggleStepIngredientLink)
                    RecipeEditorTab.NOTES -> RecipeNotesSection(notes = uiState.notes, onNotesChange = onNotesChange)
                }
                uiState.saveError?.let { ErrorText(it) }
            }
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onSaveClick, enabled = !uiState.isSaving, modifier = Modifier.weight(1f)) { Text("Save as Draft") }
                Button(onClick = onPublishClick, enabled = !uiState.isSaving, modifier = Modifier.weight(1f)) { Text("Publish Recipe") }
            }
        }
        if (uiState.publishReviewVisible) {
            ModalBottomSheet(onDismissRequest = onDismissPublishReview) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Ready to publish?", style = MaterialTheme.typography.headlineSmall)
                    Text("Included", style = MaterialTheme.typography.titleMedium)
                    Text("${uiState.ingredients.count { it.query.isNotBlank() }} ingredients")
                    Text("${uiState.steps.count { !it.isBlankStepForUi() }} steps")
                    Text("Suggestions are optional and will not block publishing.")
                    Button(onClick = onPublishClick, modifier = Modifier.fillMaxWidth()) { Text("Publish Recipe") }
                    TextButton(onClick = onDismissPublishReview, modifier = Modifier.fillMaxWidth()) { Text("Keep Editing") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipeBasicInfoSection(
    uiState: AddRecipeUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onServingsChange: (String) -> Unit,
    onPrepTimeChange: (String) -> Unit,
    onCookTimeChange: (String) -> Unit,
    onRecipeTypeChange: (RecipeType) -> Unit,
) {
    var typeExpanded by remember { mutableStateOf(false) }
    SectionCard(title = "Basic info") {
        OutlinedTextField(value = uiState.title, onValueChange = onTitleChange, label = { Text("Recipe name *") }, isError = uiState.titleError != null, supportingText = { uiState.titleError?.let { Text(it) } }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = uiState.description, onValueChange = onDescriptionChange, label = { Text("Short description") }, minLines = 2, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumberField("Servings", uiState.servings, onServingsChange, uiState.servingsError, Modifier.weight(1f))
            NumberField("Prep time (min)", uiState.prepTimeMinutes, onPrepTimeChange, uiState.prepTimeError, Modifier.weight(1f))
        }
        NumberField("Cook time (min)", uiState.cookTimeMinutes, onCookTimeChange, uiState.cookTimeError, Modifier.fillMaxWidth())
        ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = !typeExpanded }) {
            OutlinedTextField(
                value = uiState.recipeType.displayName(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Recipe type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
            )
            ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                RecipeType.entries.forEach { type ->
                    DropdownMenuItem(text = { Text(type.displayName()) }, onClick = { onRecipeTypeChange(type); typeExpanded = false })
                }
            }
        }
    }
}

@Composable
private fun RecipeIngredientEditorSection(
    uiState: AddRecipeUiState,
    onAddIngredient: () -> Unit,
    onRemoveIngredient: (String) -> Unit,
    onIngredientQueryChange: (String, String) -> Unit,
    onIngredientSelected: (String, IngredientEntity) -> Unit,
    onQuantityChange: (String, String) -> Unit,
    onUnitChange: (String, String) -> Unit,
    onPrepNoteChange: (String, String) -> Unit,
    onSectionChange: (String, String) -> Unit,
    onOptionalChange: (String, Boolean) -> Unit,
) {
    SectionCard(title = "Ingredients") {
        uiState.ingredientError?.let { ErrorText(it) }
        if (uiState.ingredients.isEmpty()) Text("No ingredients added yet. Add ingredients now, or save the recipe and fill them in later.", style = MaterialTheme.typography.bodyMedium)
        uiState.ingredients.forEachIndexed { index, ingredient ->
            RecipeIngredientRow(index + 1, ingredient, onRemoveIngredient, onIngredientQueryChange, onIngredientSelected, onQuantityChange, onUnitChange, onPrepNoteChange, onSectionChange, onOptionalChange)
        }
        OutlinedButton(onClick = onAddIngredient, modifier = Modifier.fillMaxWidth()) { Text("Add Ingredient") }
    }
}

@Composable
private fun RecipeIngredientRow(
    number: Int,
    ingredient: IngredientInputState,
    onRemoveIngredient: (String) -> Unit,
    onIngredientQueryChange: (String, String) -> Unit,
    onIngredientSelected: (String, IngredientEntity) -> Unit,
    onQuantityChange: (String, String) -> Unit,
    onUnitChange: (String, String) -> Unit,
    onPrepNoteChange: (String, String) -> Unit,
    onSectionChange: (String, String) -> Unit,
    onOptionalChange: (String, Boolean) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Ingredient $number", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                IconButton(onClick = { onRemoveIngredient(ingredient.localId) }, modifier = Modifier.semantics { contentDescription = "Remove ingredient" }) { Icon(Icons.Default.Delete, contentDescription = null) }
            }
            IngredientSearchField(ingredient, onIngredientQueryChange, onIngredientSelected)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = ingredient.quantityText, onValueChange = { onQuantityChange(ingredient.localId, it) }, label = { Text("Quantity") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = ingredient.unit, onValueChange = { onUnitChange(ingredient.localId, it) }, label = { Text("Unit") }, modifier = Modifier.weight(1f))
            }
            OutlinedTextField(value = ingredient.prepNote, onValueChange = { onPrepNoteChange(ingredient.localId, it) }, label = { Text("Prep note") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = ingredient.section, onValueChange = { onSectionChange(ingredient.localId, it) }, label = { Text("Section") }, modifier = Modifier.fillMaxWidth())
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Text("Optional")
                Switch(checked = ingredient.optional, onCheckedChange = { onOptionalChange(ingredient.localId, it) })
            }
        }
    }
}

@Composable
private fun IngredientSearchField(
    ingredient: IngredientInputState,
    onIngredientQueryChange: (String, String) -> Unit,
    onIngredientSelected: (String, IngredientEntity) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(value = ingredient.query, onValueChange = { onIngredientQueryChange(ingredient.localId, it) }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        if (ingredient.isSearching) Text("Searching…", style = MaterialTheme.typography.bodySmall)
        ingredient.suggestions.take(5).forEach { suggestion ->
            TextButton(onClick = { onIngredientSelected(ingredient.localId, suggestion) }, modifier = Modifier.fillMaxWidth()) { Text(suggestion.displayName, modifier = Modifier.fillMaxWidth()) }
        }
    }
}


@Composable
private fun RecipeStepEditorSection(
    uiState: AddRecipeUiState,
    onAddStep: () -> Unit,
    onRemoveStep: (String) -> Unit,
    onMoveStepUp: (String) -> Unit,
    onMoveStepDown: (String) -> Unit,
    onStepInstructionChange: (String, String) -> Unit,
    onStepTimerMinutesChange: (String, String) -> Unit,
    onStepTimerSecondsChange: (String, String) -> Unit,
    onStepWarningChange: (String, String) -> Unit,
    onStepEquipmentChange: (String, String) -> Unit,
    onStepWhileTimerRunsChange: (String, String) -> Unit,
    onStepCheckpointChange: (String, Boolean) -> Unit,
    onToggleStepIngredientLink: (String, String) -> Unit,
) {
    SectionCard(title = "Steps") {
        uiState.stepError?.let { ErrorText(it) }
        if (uiState.steps.isEmpty()) {
            Text("No steps added yet. Add steps now, or save the recipe and fill them in later.", style = MaterialTheme.typography.bodyMedium)
        }
        val linkableIngredients = uiState.ingredients.filter { it.query.isNotBlank() }
        uiState.steps.forEachIndexed { index, step ->
            RecipeStepRow(
                number = index + 1,
                step = step,
                isFirst = index == 0,
                isLast = index == uiState.steps.lastIndex,
                linkableIngredients = linkableIngredients,
                onRemoveStep = onRemoveStep,
                onMoveStepUp = onMoveStepUp,
                onMoveStepDown = onMoveStepDown,
                onStepInstructionChange = onStepInstructionChange,
                onStepTimerMinutesChange = onStepTimerMinutesChange,
                onStepTimerSecondsChange = onStepTimerSecondsChange,
                onStepWarningChange = onStepWarningChange,
                onStepEquipmentChange = onStepEquipmentChange,
                onStepWhileTimerRunsChange = onStepWhileTimerRunsChange,
                onStepCheckpointChange = onStepCheckpointChange,
                onToggleStepIngredientLink = onToggleStepIngredientLink,
            )
        }
        OutlinedButton(onClick = onAddStep, modifier = Modifier.fillMaxWidth()) { Text("Add Step") }
    }
}

@Composable
private fun RecipeStepRow(
    number: Int,
    step: StepInputState,
    isFirst: Boolean,
    isLast: Boolean,
    linkableIngredients: List<IngredientInputState>,
    onRemoveStep: (String) -> Unit,
    onMoveStepUp: (String) -> Unit,
    onMoveStepDown: (String) -> Unit,
    onStepInstructionChange: (String, String) -> Unit,
    onStepTimerMinutesChange: (String, String) -> Unit,
    onStepTimerSecondsChange: (String, String) -> Unit,
    onStepWarningChange: (String, String) -> Unit,
    onStepEquipmentChange: (String, String) -> Unit,
    onStepWhileTimerRunsChange: (String, String) -> Unit,
    onStepCheckpointChange: (String, Boolean) -> Unit,
    onToggleStepIngredientLink: (String, String) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Step $number", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                TextButton(onClick = { onMoveStepUp(step.localId) }, enabled = !isFirst) { Text("Move up") }
                TextButton(onClick = { onMoveStepDown(step.localId) }, enabled = !isLast) { Text("Move down") }
                IconButton(onClick = { onRemoveStep(step.localId) }, modifier = Modifier.semantics { contentDescription = "Remove step" }) { Icon(Icons.Default.Delete, contentDescription = null) }
            }
            OutlinedTextField(value = step.instruction, onValueChange = { onStepInstructionChange(step.localId, it) }, label = { Text("Instruction") }, minLines = 2, modifier = Modifier.fillMaxWidth())
            Text("Optional timer for this step.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = step.timerMinutes, onValueChange = { onStepTimerMinutesChange(step.localId, it) }, label = { Text("Min") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.weight(1f))
                OutlinedTextField(value = step.timerSeconds, onValueChange = { onStepTimerSecondsChange(step.localId, it) }, label = { Text("Sec") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.weight(1f))
            }
            OutlinedTextField(value = step.warning, onValueChange = { onStepWarningChange(step.localId, it) }, label = { Text("Warning") }, supportingText = { Text("Anything the cook should be careful about.") }, minLines = 2, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = step.equipment, onValueChange = { onStepEquipmentChange(step.localId, it) }, label = { Text("Equipment") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = step.whileTimerRuns, onValueChange = { onStepWhileTimerRunsChange(step.localId, it) }, label = { Text("Meanwhile") }, supportingText = { Text("What can the cook do while this timer is running?") }, minLines = 2, modifier = Modifier.fillMaxWidth())
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Checkbox(checked = step.checkpoint, onCheckedChange = { onStepCheckpointChange(step.localId, it) })
                Column {
                    Text("Checkpoint")
                    Text("Mark this as a pause or doneness check.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text("Linked ingredients", style = MaterialTheme.typography.titleSmall)
            if (linkableIngredients.isEmpty()) {
                Text("Add ingredients above to link them to steps.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    linkableIngredients.forEach { ingredient ->
                        FilterChip(
                            selected = ingredient.localId in step.linkedIngredientLocalIds,
                            onClick = { onToggleStepIngredientLink(step.localId, ingredient.localId) },
                            label = { Text(ingredient.query.trim()) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecipeNotesSection(notes: String, onNotesChange: (String) -> Unit) = SectionCard(title = "Private notes") {
    OutlinedTextField(
        value = notes,
        onValueChange = onNotesChange,
        label = { Text("Private notes") },
        supportingText = { Text("Notes for yourself — changes, family tips, or things to remember next time.") },
        minLines = 4,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun NumberField(label: String, value: String, onValueChange: (String) -> Unit, error: String?, modifier: Modifier = Modifier) {
    OutlinedTextField(value = value, onValueChange = onValueChange, label = { Text(label) }, isError = error != null, supportingText = { error?.let { Text(it) } }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = modifier)
}

@Composable
private fun ErrorText(message: String) = Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)

private fun RecipeType.displayName(): String = when (this) {
    RecipeType.FULL_DISH -> "Full dish"
    RecipeType.COMPONENT -> "Component"
    RecipeType.SAUCE -> "Sauce"
    RecipeType.SPICE_BLEND -> "Spice blend"
    RecipeType.DOUGH -> "Dough"
    RecipeType.MARINADE -> "Marinade"
    RecipeType.DRINK -> "Drink"
    RecipeType.DESSERT -> "Dessert"
    RecipeType.SNACK -> "Snack"
    RecipeType.OTHER -> "Other"
}

@Preview(showBackground = true)
@Composable
private fun AddRecipeScreenEmptyPreview() = MaterialTheme { AddRecipeScreenPreview(AddRecipeUiState()) }

@Preview(showBackground = true)
@Composable
private fun AddRecipeScreenIngredientPreview() = MaterialTheme {
    AddRecipeScreenPreview(AddRecipeUiState(title = "Pasta night", ingredients = listOf(IngredientInputState(localId = "1", query = "Garlic", quantityText = "2", unit = "cloves", prepNote = "minced"))))
}

@Preview(showBackground = true)
@Composable
private fun IngredientRowSuggestionsPreview() = MaterialTheme {
    RecipeIngredientRow(
        number = 1,
        ingredient = IngredientInputState(localId = "1", query = "gar", suggestions = listOf(previewIngredient("garlic", "Garlic"), previewIngredient("garlic-powder", "Garlic powder"))),
        onRemoveIngredient = {}, onIngredientQueryChange = { _, _ -> }, onIngredientSelected = { _, _ -> }, onQuantityChange = { _, _ -> }, onUnitChange = { _, _ -> }, onPrepNoteChange = { _, _ -> }, onSectionChange = { _, _ -> }, onOptionalChange = { _, _ -> },
    )
}

@Composable
private fun AddRecipeScreenPreview(state: AddRecipeUiState) = AddRecipeScreen(
    uiState = state,
    onBackClick = {}, onSaveClick = {}, onSaveComplete = {}, onTitleChange = {}, onDescriptionChange = {}, onServingsChange = {}, onPrepTimeChange = {}, onCookTimeChange = {}, onRecipeTypeChange = {}, onNotesChange = {}, onAddIngredient = {}, onRemoveIngredient = {}, onIngredientQueryChange = { _, _ -> }, onIngredientSelected = { _, _ -> }, onQuantityChange = { _, _ -> }, onUnitChange = { _, _ -> }, onPrepNoteChange = { _, _ -> }, onSectionChange = { _, _ -> }, onOptionalChange = { _, _ -> }, onAddStep = {}, onRemoveStep = {}, onMoveStepUp = {}, onMoveStepDown = {}, onStepInstructionChange = { _, _ -> }, onStepTimerMinutesChange = { _, _ -> }, onStepTimerSecondsChange = { _, _ -> }, onStepWarningChange = { _, _ -> }, onStepEquipmentChange = { _, _ -> }, onStepWhileTimerRunsChange = { _, _ -> }, onStepCheckpointChange = { _, _ -> }, onToggleStepIngredientLink = { _, _ -> },
)

private fun previewIngredient(id: String, name: String) = IngredientEntity(id = id, displayName = name, canonicalName = name.lowercase(), category = "vegetable", defaultUnit = null, commonUnitsJson = null, source = IngredientSource.CURATED.name, sourceId = id, isUserCreated = false, createdAt = 0L, updatedAt = 0L)


private fun RecipeEditorTab.label(): String = when (this) {
    RecipeEditorTab.BASIC_INFO -> "Basic Info"
    RecipeEditorTab.INGREDIENTS -> "Ingredients"
    RecipeEditorTab.STEPS -> "Steps"
    RecipeEditorTab.NOTES -> "Notes"
}

private fun StepInputState.isBlankStepForUi(): Boolean = instruction.isBlank() && timerMinutes.isBlank() && timerSeconds.isBlank() && warning.isBlank() && equipment.isBlank() && whileTimerRuns.isBlank() && !checkpoint && linkedIngredientLocalIds.isEmpty()
