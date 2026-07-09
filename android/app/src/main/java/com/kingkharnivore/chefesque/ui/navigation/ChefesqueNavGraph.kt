package com.kingkharnivore.chefesque.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kingkharnivore.chefesque.data.AppContainer
import com.kingkharnivore.chefesque.ui.screen.addlog.AddLogPlaceholderScreen
import com.kingkharnivore.chefesque.ui.screen.addrecipe.AddRecipeScreen
import com.kingkharnivore.chefesque.ui.screen.addrecipe.AddRecipeViewModel
import com.kingkharnivore.chefesque.ui.screen.addrecipe.AddRecipeViewModelFactory
import com.kingkharnivore.chefesque.ui.screen.cookinglog.CookingLogViewModel
import com.kingkharnivore.chefesque.ui.screen.cookinglog.CookingLogViewModelFactory
import com.kingkharnivore.chefesque.ui.screen.main.ChefesqueMainScreen
import com.kingkharnivore.chefesque.ui.screen.main.PlaceholderScreen
import com.kingkharnivore.chefesque.ui.screen.recipes.RecipesViewModel
import com.kingkharnivore.chefesque.ui.screen.recipes.RecipesViewModelFactory
import com.kingkharnivore.chefesque.ui.screen.recipedetail.RecipeDetailScreen
import com.kingkharnivore.chefesque.ui.screen.recipedetail.RecipeDetailViewModel
import com.kingkharnivore.chefesque.ui.screen.recipedetail.RecipeDetailViewModelFactory

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
                onRecipeClick = { recipeId -> navController.navigate(ChefesqueDestination.RecipeDetail.createRoute(recipeId)) },
            )
        }
        composable(ChefesqueDestination.AddRecipe.route) {
            val addRecipeViewModel: AddRecipeViewModel = viewModel(
                factory = AddRecipeViewModelFactory(
                    recipeRepository = appContainer.recipeRepository,
                    ingredientRepository = appContainer.ingredientRepository,
                ),
            )
            AddRecipeScreen(
                uiState = addRecipeViewModel.uiState.collectAsStateWithLifecycle().value,
                onBackClick = { navController.popBackStack() },
                onSaveClick = addRecipeViewModel::saveRecipe,
                onSaveComplete = { navController.popBackStack(ChefesqueDestination.Main.route, inclusive = false) },
                onTitleChange = addRecipeViewModel::updateTitle,
                onDescriptionChange = addRecipeViewModel::updateDescription,
                onServingsChange = addRecipeViewModel::updateServings,
                onPrepTimeChange = addRecipeViewModel::updatePrepTimeMinutes,
                onCookTimeChange = addRecipeViewModel::updateCookTimeMinutes,
                onRecipeTypeChange = addRecipeViewModel::updateRecipeType,
                onNotesChange = addRecipeViewModel::updateNotes,
                onAddIngredient = addRecipeViewModel::addIngredientRow,
                onRemoveIngredient = addRecipeViewModel::removeIngredientRow,
                onIngredientQueryChange = addRecipeViewModel::updateIngredientQuery,
                onIngredientSelected = addRecipeViewModel::selectIngredient,
                onQuantityChange = addRecipeViewModel::updateQuantity,
                onUnitChange = addRecipeViewModel::updateUnit,
                onPrepNoteChange = addRecipeViewModel::updatePrepNote,
                onSectionChange = addRecipeViewModel::updateSection,
                onOptionalChange = addRecipeViewModel::updateOptional,
                onAddStep = addRecipeViewModel::addStep,
                onRemoveStep = addRecipeViewModel::removeStep,
                onMoveStepUp = addRecipeViewModel::moveStepUp,
                onMoveStepDown = addRecipeViewModel::moveStepDown,
                onStepInstructionChange = addRecipeViewModel::updateStepInstruction,
                onStepTimerMinutesChange = addRecipeViewModel::updateStepTimerMinutes,
                onStepTimerSecondsChange = addRecipeViewModel::updateStepTimerSeconds,
                onStepWarningChange = addRecipeViewModel::updateStepWarning,
                onStepEquipmentChange = addRecipeViewModel::updateStepEquipment,
                onStepWhileTimerRunsChange = addRecipeViewModel::updateStepWhileTimerRuns,
                onStepCheckpointChange = addRecipeViewModel::updateStepCheckpoint,
                onToggleStepIngredientLink = addRecipeViewModel::toggleStepIngredientLink,
            )
        }
        composable(ChefesqueDestination.AddLog.route) { AddLogPlaceholderScreen(onBackClick = { navController.popBackStack() }) }
        composable(
            route = ChefesqueDestination.RecipeDetail.route,
            arguments = listOf(navArgument("recipeId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId").orEmpty()
            val recipeDetailViewModel: RecipeDetailViewModel = viewModel(
                factory = RecipeDetailViewModelFactory(recipeId, appContainer.recipeRepository),
            )
            RecipeDetailScreen(
                uiState = recipeDetailViewModel.uiState.collectAsStateWithLifecycle().value,
                onBackClick = { navController.popBackStack(ChefesqueDestination.Main.route, inclusive = false) },
                onEditClick = { navController.navigate(ChefesqueDestination.EditRecipe.createRoute(recipeId)) },
                onCookAlongClick = { navController.navigate(ChefesqueDestination.CookAlong.route) },
            )
        }
        composable(
            route = ChefesqueDestination.EditRecipe.route,
            arguments = listOf(navArgument("recipeId") { type = NavType.StringType }),
        ) {
            PlaceholderScreen(
                title = "Edit Recipe",
                body = "Recipe editing starts in a later pass.",
                onBackClick = { navController.popBackStack() },
            )
        }
        composable(ChefesqueDestination.CookAlong.route) {
            PlaceholderScreen(
                title = "Cook Along",
                body = "Cook Along starts in a later pass.",
                onBackClick = { navController.popBackStack() },
            )
        }
    }
}
