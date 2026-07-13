package com.kingkharnivore.chefesque.ui.screen.recipedetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kingkharnivore.chefesque.data.local.entity.RecipeEntity
import com.kingkharnivore.chefesque.data.local.entity.RecipeIngredientEntity
import com.kingkharnivore.chefesque.data.local.entity.RecipeStepEntity
import com.kingkharnivore.chefesque.ui.theme.ChefesqueTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    uiState: RecipeDetailUiState,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onCookAlongClick: () -> Unit,
    onCookingLogClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(uiState.recipe?.title?.takeIf { it.isNotBlank() } ?: "Recipe") },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                actions = { TextButton(onClick = onEditClick, enabled = uiState.recipe != null) { Text("Edit") } },
            )
        },
    ) { paddingValues ->
        Box(Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                uiState.notFound || uiState.recipe == null -> RecipeNotFound(onBackClick, Modifier.align(Alignment.Center).padding(24.dp))
                else -> RecipeDetailContent(uiState, onCookAlongClick, onCookingLogClick)
            }
        }
    }
}

@Composable
private fun RecipeDetailContent(uiState: RecipeDetailUiState, onCookAlongClick: () -> Unit, onCookingLogClick: (String) -> Unit) {
    val recipe = uiState.recipe ?: return
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        RecipePhotoPlaceholder(recipe.coverImageUri)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(recipe.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            recipe.description?.trim()?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodyLarge) }
            val metadata = recipeMetadataLabels(recipe.servings, recipe.prepTimeMinutes, recipe.cookTimeMinutes)
            if (metadata.isNotEmpty()) Text(metadata.joinToString(" · "), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            recipeTypeDisplayName(recipe.recipeType)?.let { AssistChip(onClick = {}, label = { Text(it) }) }
        }
        Button(onClick = onCookAlongClick, modifier = Modifier.fillMaxWidth()) { Text("Start Cook Along") }
        DetailSectionCard("Cooking history") {
            CookingHistoryContent(
                recentLogs = uiState.recentLogs,
                totalLogCount = uiState.totalLogCount,
                lastCookedText = uiState.lastCookedText,
                onCookingLogClick = onCookingLogClick,
            )
        }
        DetailSectionCard("Ingredients") { IngredientsContent(uiState.ingredients) }
        DetailSectionCard("Steps") { StepsContent(uiState.steps) }
        recipe.notes?.trim()?.takeIf { it.isNotBlank() }?.let { notes -> DetailSectionCard("Private notes") { Text(notes, style = MaterialTheme.typography.bodyMedium) } }
    }
}

@Composable
private fun CookingHistoryContent(
    recentLogs: List<RecipeCookingLogUiModel>,
    totalLogCount: Int,
    lastCookedText: String?,
    onCookingLogClick: (String) -> Unit,
) {
    if (recentLogs.isEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("No cooks logged yet.", style = MaterialTheme.typography.bodyMedium)
            Text("Finish a Cook Along to start tracking this recipe.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            lastCookedText?.let { Text("Last cooked $it", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold) }
            Text("$totalLogCount ${if (totalLogCount == 1) "cook" else "cooks"} logged", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (totalLogCount > recentLogs.size) {
                Text("Showing latest ${recentLogs.size} of $totalLogCount cooks.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        recentLogs.forEach { log ->
            RecipeCookingLogRow(log = log, onClick = { onCookingLogClick(log.id) })
        }
    }
}

@Composable
private fun RecipeCookingLogRow(log: RecipeCookingLogUiModel, onClick: () -> Unit) {
    OutlinedCard(onClick = onClick, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(listOfNotNull(log.cookedDateText, log.durationText).joinToString(" • "), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                if (log.isFavorite) Text("★", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
            val resultLine = listOfNotNull(log.resultText, log.wouldMakeAgainText).joinToString(" • ")
            if (resultLine.isNotBlank()) Text(resultLine, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            log.notesPreview?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            if (log.createdFromCookAlong) AssistChip(onClick = {}, label = { Text("Cook Along") }, enabled = false)
        }
    }
}

@Composable
private fun RecipePhotoPlaceholder(coverImageUri: String?) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Box(
            modifier = Modifier.fillMaxWidth().height(180.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)).padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(if (coverImageUri.isNullOrBlank()) "No recipe photo yet" else "Recipe photo saved", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(if (coverImageUri.isNullOrBlank()) "Photos come in a later pass." else "Image display comes in a later pass.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun IngredientsContent(ingredients: List<RecipeIngredientEntity>) {
    if (ingredients.isEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("No ingredients added yet.", style = MaterialTheme.typography.bodyMedium)
            Text("You can add them when recipe editing is implemented.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    val groups = ingredientSectionGroups(ingredients)
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        groups.forEach { group ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (groups.size > 1 || group.title != "Ingredients") Text(group.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                group.ingredients.forEach { IngredientDisplayRow(it) }
            }
        }
    }
}

@Composable
private fun IngredientDisplayRow(ingredient: RecipeIngredientEntity) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("•", style = MaterialTheme.typography.bodyLarge)
        Text(formatIngredientLine(ingredient), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        if (ingredient.optional) AssistChip(onClick = {}, label = { Text("Optional") })
    }
}


@Composable
private fun StepsContent(steps: List<RecipeStepEntity>) {
    if (steps.isEmpty()) {
        Text("No steps added yet.", style = MaterialTheme.typography.bodyMedium)
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        steps.forEachIndexed { index, step -> StepDisplayRow(index + 1, step) }
    }
}

@Composable
private fun StepDisplayRow(number: Int, step: RecipeStepEntity) {
    val title = step.title?.trim()?.takeIf { it.isNotBlank() }
    val instruction = step.instruction.trim().takeIf { it.isNotBlank() }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        when {
            title != null && instruction != null -> {
                Text("$number. $title", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(instruction, style = MaterialTheme.typography.bodyMedium)
            }
            title != null -> Text("$number. $title", style = MaterialTheme.typography.bodyLarge)
            instruction != null -> Text("$number. $instruction", style = MaterialTheme.typography.bodyLarge)
        }
        formatStepTimer(step.timerSeconds)?.let { DetailLabel("Timer", it) }
        step.warning?.trim()?.takeIf { it.isNotBlank() }?.let { DetailLabel("Warning", it) }
        step.equipment?.trim()?.takeIf { it.isNotBlank() }?.let { DetailLabel("Equipment", it) }
        (step.meanwhile ?: step.whileTimerRuns)?.trim()?.takeIf { it.isNotBlank() }?.let { DetailLabel("Meanwhile", it) }
        step.checkpoint?.trim()?.takeIf { it.isNotBlank() }?.let { Text("Checkpoint", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold) }
    }
}

@Composable
private fun DetailLabel(label: String, value: String) {
    Text("$label: $value", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun DetailSectionCard(title: String, content: @Composable () -> Unit) {
    OutlinedCard(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

@Composable
private fun RecipeNotFound(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Recipe not found", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Text("This recipe may have been deleted or archived.", style = MaterialTheme.typography.bodyLarge)
        Button(onClick = onBackClick) { Text("Back to recipes") }
    }
}

@Preview(showBackground = true)
@Composable
private fun RecipeDetailScreenPreview() {
    ChefesqueTheme {
        RecipeDetailScreen(
            uiState = RecipeDetailUiState(isLoading = false, recipe = RecipeEntity("1", "Sunday Sauce", "A cozy all-day sauce.", 6, 20, 180, null, null, null, "FULL_DISH", "Use the big pot.", 0, 0, null), ingredients = listOf(RecipeIngredientEntity("i1", "1", null, "Garlic", null, "2", "cloves", "minced", "Sauce", false, 0)), steps = listOf(RecipeStepEntity("s1", "1", "Chop onion and garlic.", 300, null, null, null, "Do not let garlic burn.", "Large skillet", "Start pasta water.", 0))),
            onBackClick = {}, onEditClick = {}, onCookAlongClick = {}, onCookingLogClick = {},
        )
    }
}
