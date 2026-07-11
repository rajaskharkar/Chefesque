package com.kingkharnivore.chefesque.ui.screen.editrecipe

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kingkharnivore.chefesque.data.local.entity.IngredientEntity
import com.kingkharnivore.chefesque.domain.model.RecipeType
import com.kingkharnivore.chefesque.ui.screen.addrecipe.AddRecipeScreen
import com.kingkharnivore.chefesque.ui.screen.addrecipe.AddRecipeUiState

@Composable
fun EditRecipeScreen(
    uiState: EditRecipeUiState,
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
    modifier: Modifier = Modifier,
) {
    var showDiscardDialog by remember { mutableStateOf(false) }
    fun requestBack() { if (uiState.hasUnsavedChanges && !uiState.isSaving) showDiscardDialog = true else onBackClick() }

    LaunchedEffect(uiState.saved) { if (uiState.saved) onSaveComplete() }
    BackHandler(enabled = uiState.hasUnsavedChanges && !uiState.isSaving) { showDiscardDialog = true }

    when {
        uiState.isLoading -> EditLoadingScreen(onBackClick = ::requestBack, modifier = modifier)
        uiState.notFound -> EditNotFoundScreen(onBackClick = onBackClick, modifier = modifier)
        else -> AddRecipeScreen(
            uiState = uiState.toAddRecipeUiState(),
            onBackClick = ::requestBack,
            onSaveClick = onSaveClick,
            onSaveComplete = {},
            onTitleChange = onTitleChange,
            onDescriptionChange = onDescriptionChange,
            onServingsChange = onServingsChange,
            onPrepTimeChange = onPrepTimeChange,
            onCookTimeChange = onCookTimeChange,
            onRecipeTypeChange = onRecipeTypeChange,
            onNotesChange = onNotesChange,
            onAddIngredient = onAddIngredient,
            onRemoveIngredient = onRemoveIngredient,
            onIngredientQueryChange = onIngredientQueryChange,
            onIngredientSelected = onIngredientSelected,
            onQuantityChange = onQuantityChange,
            onUnitChange = onUnitChange,
            onPrepNoteChange = onPrepNoteChange,
            onSectionChange = onSectionChange,
            onOptionalChange = onOptionalChange,
            onAddStep = onAddStep,
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
            modifier = modifier,
            screenTitle = "Edit Recipe",
            saveActionLabel = "Save",
            saveButtonLabel = "Save Changes",
            savingButtonLabel = "Saving changes…",
        )
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard changes?") },
            text = { Text("Your edits will be lost.") },
            confirmButton = { TextButton(onClick = { showDiscardDialog = false; onBackClick() }) { Text("Discard") } },
            dismissButton = { TextButton(onClick = { showDiscardDialog = false }) { Text("Cancel") } },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditLoadingScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier) = Scaffold(
    modifier = modifier,
    topBar = { TopAppBar(title = { Text("Edit Recipe") }, navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }) },
) { padding -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditNotFoundScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier) = Scaffold(
    modifier = modifier,
    topBar = { TopAppBar(title = { Text("Recipe not found") }, navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }) },
) { padding ->
    Column(Modifier.fillMaxSize().padding(padding).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Recipe not found", style = MaterialTheme.typography.headlineSmall)
        Text("This recipe may have been deleted or archived.", style = MaterialTheme.typography.bodyMedium)
        Button(onClick = onBackClick, modifier = Modifier.padding(top = 16.dp)) { Text("Back to recipe") }
    }
}

private fun EditRecipeUiState.toAddRecipeUiState() = AddRecipeUiState(
    title = title, description = description, servings = servings, prepTimeMinutes = prepTimeMinutes, cookTimeMinutes = cookTimeMinutes, recipeType = recipeType, notes = notes,
    ingredients = ingredients, steps = steps, isSaving = isSaving, titleError = titleError, servingsError = servingsError, prepTimeError = prepTimeError, cookTimeError = cookTimeError, ingredientError = ingredientError, stepError = stepError, saveError = saveError,
)
