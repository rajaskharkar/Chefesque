package com.kingkharnivore.chefesque.ui.screen.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kingkharnivore.chefesque.ui.screen.cookinglog.CookingLogScreen
import com.kingkharnivore.chefesque.ui.screen.cookinglog.CookingLogUiState
import com.kingkharnivore.chefesque.ui.screen.recipes.MyRecipesScreen
import com.kingkharnivore.chefesque.ui.screen.recipes.RecipesUiState
import com.kingkharnivore.chefesque.ui.theme.ChefesqueTheme

private enum class MainTab(val label: String) { Recipes("My Recipes"), Logs("Cooking Log") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChefesqueMainScreen(
    recipesUiState: RecipesUiState,
    cookingLogUiState: CookingLogUiState,
    onAddRecipeClick: () -> Unit,
    onAddLogClick: () -> Unit,
    onRecipeClick: (String) -> Unit,
    onLogClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val selectedTab = MainTab.entries[selectedTabIndex]
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Chefesque", fontWeight = FontWeight.SemiBold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                )
                TabRow(selectedTabIndex = selectedTabIndex) {
                    MainTab.entries.forEachIndexed { index, tab ->
                        Tab(selected = selectedTabIndex == index, onClick = { selectedTabIndex = index }, text = { Text(tab.label) })
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { if (selectedTab == MainTab.Recipes) onAddRecipeClick() else onAddLogClick() },
                icon = { Text("+") },
                text = { Text(if (selectedTab == MainTab.Recipes) "Add Recipe" else "Add Log") },
            )
        },
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)
        when (selectedTab) {
            MainTab.Recipes -> MyRecipesScreen(recipesUiState, onAddRecipeClick, onRecipeClick, contentModifier)
            MainTab.Logs -> CookingLogScreen(cookingLogUiState, onAddLogClick, onLogClick, contentModifier)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceholderScreen(title: String, body: String, onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text(title) }, navigationIcon = { TextButton(onClick = onBackClick) { Text("Back") } }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(title, style = MaterialTheme.typography.headlineMedium)
            Text(body, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 12.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChefesqueMainScreenPreview() {
    ChefesqueTheme { ChefesqueMainScreen(RecipesUiState(isLoading = false), CookingLogUiState(isLoading = false), {}, {}, {}, {}) }
}
