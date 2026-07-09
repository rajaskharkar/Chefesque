package com.kingkharnivore.chefesque.ui.screen.addrecipe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import com.kingkharnivore.chefesque.ui.theme.ChefesqueTheme

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
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(uiState.savedRecipeId) {
        if (uiState.savedRecipeId != null) onSaveComplete()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Add Recipe") },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                actions = { TextButton(onClick = onSaveClick, enabled = !uiState.isSaving) { Text(if (uiState.isSaving) "Saving" else "Save") } },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            RecipeBasicInfoSection(uiState, onTitleChange, onDescriptionChange, onServingsChange, onPrepTimeChange, onCookTimeChange, onRecipeTypeChange)
            RecipeIngredientEditorSection(uiState, onAddIngredient, onRemoveIngredient, onIngredientQueryChange, onIngredientSelected, onQuantityChange, onUnitChange, onPrepNoteChange, onSectionChange, onOptionalChange)
            RecipeNotesSection(notes = uiState.notes, onNotesChange = onNotesChange)
            uiState.saveError?.let { ErrorText(it) }
            Button(onClick = onSaveClick, enabled = !uiState.isSaving, modifier = Modifier.fillMaxWidth()) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.height(18.dp).width(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Text(if (uiState.isSaving) "Saving recipe…" else "Save Recipe")
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
private fun SectionCard(title: String, content: @Composable Column.() -> Unit) {
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
private fun AddRecipeScreenEmptyPreview() = ChefesqueTheme { AddRecipeScreenPreview(AddRecipeUiState()) }

@Preview(showBackground = true)
@Composable
private fun AddRecipeScreenIngredientPreview() = ChefesqueTheme {
    AddRecipeScreenPreview(AddRecipeUiState(title = "Pasta night", ingredients = listOf(IngredientInputState(localId = "1", query = "Garlic", quantityText = "2", unit = "cloves", prepNote = "minced"))))
}

@Preview(showBackground = true)
@Composable
private fun IngredientRowSuggestionsPreview() = ChefesqueTheme {
    RecipeIngredientRow(
        number = 1,
        ingredient = IngredientInputState(localId = "1", query = "gar", suggestions = listOf(previewIngredient("garlic", "Garlic"), previewIngredient("garlic-powder", "Garlic powder"))),
        onRemoveIngredient = {}, onIngredientQueryChange = { _, _ -> }, onIngredientSelected = { _, _ -> }, onQuantityChange = { _, _ -> }, onUnitChange = { _, _ -> }, onPrepNoteChange = { _, _ -> }, onSectionChange = { _, _ -> }, onOptionalChange = { _, _ -> },
    )
}

@Composable
private fun AddRecipeScreenPreview(state: AddRecipeUiState) = AddRecipeScreen(
    uiState = state,
    onBackClick = {}, onSaveClick = {}, onSaveComplete = {}, onTitleChange = {}, onDescriptionChange = {}, onServingsChange = {}, onPrepTimeChange = {}, onCookTimeChange = {}, onRecipeTypeChange = {}, onNotesChange = {}, onAddIngredient = {}, onRemoveIngredient = {}, onIngredientQueryChange = { _, _ -> }, onIngredientSelected = { _, _ -> }, onQuantityChange = { _, _ -> }, onUnitChange = { _, _ -> }, onPrepNoteChange = { _, _ -> }, onSectionChange = { _, _ -> }, onOptionalChange = { _, _ -> },
)

private fun previewIngredient(id: String, name: String) = IngredientEntity(id = id, displayName = name, canonicalName = name.lowercase(), category = "vegetable", defaultUnit = null, commonUnitsJson = null, source = IngredientSource.CURATED.name, sourceId = id, isUserCreated = false, createdAt = 0L, updatedAt = 0L)
