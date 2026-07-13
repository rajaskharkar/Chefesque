package com.kingkharnivore.chefesque.ui.screen.recipes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kingkharnivore.chefesque.data.local.entity.RecipeEntity
import com.kingkharnivore.chefesque.ui.component.ChefesqueEmptyState
import com.kingkharnivore.chefesque.ui.component.RecipeSummaryCard
import com.kingkharnivore.chefesque.ui.theme.ChefesqueTheme

@Composable
fun MyRecipesScreen(uiState: RecipesUiState, onAddRecipeClick: () -> Unit, onRecipeClick: (String) -> Unit, modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableIntStateOf(0) }
    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Published") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Drafts") })
        }
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                selectedTab == 0 -> PublishedRecipesTab(uiState, onAddRecipeClick, onRecipeClick, onViewDrafts = { selectedTab = 1 })
                else -> DraftRecipesTab(uiState, onAddRecipeClick, onRecipeClick)
            }
        }
    }
}

@Composable
private fun PublishedRecipesTab(uiState: RecipesUiState, onAddRecipeClick: () -> Unit, onRecipeClick: (String) -> Unit, onViewDrafts: () -> Unit) {
    if (uiState.publishedRecipes.isEmpty()) {
        ChefesqueEmptyState(
            title = "Your recipe collection starts here",
            body = "Publish recipes you love and use Cook Along to make them step by step.",
            actionLabel = "Add Recipe",
            onActionClick = onAddRecipeClick,
            modifier = Modifier.fillMaxSize(),
        )
        if (uiState.draftRecipes.isNotEmpty()) Box(Modifier.fillMaxSize()) { TextButton(onClick = onViewDrafts, modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp)) { Text("View Drafts") } }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(uiState.publishedRecipes, key = { it.id }) { recipe -> RecipeSummaryCard(recipe = recipe, onClick = { onRecipeClick(recipe.id) }) }
        }
    }
}

@Composable
private fun DraftRecipesTab(uiState: RecipesUiState, onAddRecipeClick: () -> Unit, onRecipeClick: (String) -> Unit) {
    if (uiState.draftRecipes.isEmpty()) {
        ChefesqueEmptyState(
            title = "No unfinished recipes",
            body = "Recipes you save for later will appear here.",
            actionLabel = "Create a Recipe",
            onActionClick = onAddRecipeClick,
            modifier = Modifier.fillMaxSize(),
        )
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(uiState.draftRecipes, key = { it.id }) { recipe -> DraftRecipeCard(recipe = recipe, onContinue = { onRecipeClick(recipe.id) }) }
        }
    }
}

@Composable
private fun DraftRecipeCard(recipe: RecipeEntity, onContinue: () -> Unit) {
    Card(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(recipe.title.ifBlank { "Untitled Recipe" }, style = MaterialTheme.typography.titleMedium)
            Text(draftProgressText(recipe), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Last edited recently", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = onContinue, modifier = Modifier.align(Alignment.End)) { Text("Continue Editing") }
        }
    }
}

fun draftProgressText(recipe: RecipeEntity): String = when {
    recipe.title.isNotBlank() && recipe.title != "Untitled Recipe" -> "Basic information added"
    else -> "Add ingredients to continue"
}

@Preview(showBackground = true)
@Composable
private fun MyRecipesScreenEmptyPreview() {
    ChefesqueTheme { MyRecipesScreen(RecipesUiState(isLoading = false), {}, {}) }
}
