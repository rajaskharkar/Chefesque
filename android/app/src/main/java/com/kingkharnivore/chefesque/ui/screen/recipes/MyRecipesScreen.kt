package com.kingkharnivore.chefesque.ui.screen.recipes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kingkharnivore.chefesque.ui.component.ChefesqueEmptyState
import com.kingkharnivore.chefesque.ui.component.RecipeSummaryCard
import com.kingkharnivore.chefesque.ui.theme.ChefesqueTheme

@Composable
fun MyRecipesScreen(uiState: RecipesUiState, onAddRecipeClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            uiState.recipes.isEmpty() -> ChefesqueEmptyState(
                title = "No recipes yet",
                body = "Save your first recipe and Chefesque will turn it into something you can cook step by step.",
                actionLabel = "Add Recipe",
                onActionClick = onAddRecipeClick,
                modifier = Modifier.align(Alignment.Center),
            )
            else -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(uiState.recipes, key = { it.id }) { RecipeSummaryCard(it) }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MyRecipesScreenEmptyPreview() {
    ChefesqueTheme { MyRecipesScreen(RecipesUiState(isLoading = false), {}) }
}
