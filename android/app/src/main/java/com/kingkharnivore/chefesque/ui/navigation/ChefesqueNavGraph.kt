package com.kingkharnivore.chefesque.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kingkharnivore.chefesque.data.AppContainer
import com.kingkharnivore.chefesque.ui.screen.addlog.AddLogPlaceholderScreen
import com.kingkharnivore.chefesque.ui.screen.addrecipe.AddRecipePlaceholderScreen
import com.kingkharnivore.chefesque.ui.screen.cookinglog.CookingLogViewModel
import com.kingkharnivore.chefesque.ui.screen.cookinglog.CookingLogViewModelFactory
import com.kingkharnivore.chefesque.ui.screen.main.ChefesqueMainScreen
import com.kingkharnivore.chefesque.ui.screen.recipes.RecipesViewModel
import com.kingkharnivore.chefesque.ui.screen.recipes.RecipesViewModelFactory

@Composable
fun ChefesqueApp(appContainer: AppContainer) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = ChefesqueDestination.Main.route) {
        composable(ChefesqueDestination.Main.route) {
            val recipesViewModel: RecipesViewModel = viewModel(factory = RecipesViewModelFactory(appContainer.recipeRepository))
            val cookingLogViewModel: CookingLogViewModel = viewModel(factory = CookingLogViewModelFactory(appContainer.cookingLogRepository))
            ChefesqueMainScreen(
                recipesUiState = recipesViewModel.uiState.collectAsStateWithLifecycle().value,
                cookingLogUiState = cookingLogViewModel.uiState.collectAsStateWithLifecycle().value,
                onAddRecipeClick = { navController.navigate(ChefesqueDestination.AddRecipe.route) },
                onAddLogClick = { navController.navigate(ChefesqueDestination.AddLog.route) },
            )
        }
        composable(ChefesqueDestination.AddRecipe.route) { AddRecipePlaceholderScreen(onBackClick = { navController.popBackStack() }) }
        composable(ChefesqueDestination.AddLog.route) { AddLogPlaceholderScreen(onBackClick = { navController.popBackStack() }) }
    }
}
