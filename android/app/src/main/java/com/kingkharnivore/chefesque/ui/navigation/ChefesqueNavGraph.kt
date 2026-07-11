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
import com.kingkharnivore.chefesque.ui.screen.cookalong.CookAlongScreen
import com.kingkharnivore.chefesque.ui.screen.cookalong.CookAlongViewModel
import com.kingkharnivore.chefesque.ui.screen.cookalong.CookAlongViewModelFactory
import com.kingkharnivore.chefesque.ui.screen.editrecipe.EditRecipeScreen
import com.kingkharnivore.chefesque.ui.screen.editrecipe.EditRecipeViewModel
import com.kingkharnivore.chefesque.ui.screen.editrecipe.EditRecipeViewModelFactory
import com.kingkharnivore.chefesque.ui.screen.main.ChefesqueMainScreen
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
                onCookAlongClick = { navController.navigate(ChefesqueDestination.CookAlong.createRoute(recipeId)) },
            )
        }
        composable(
            route = ChefesqueDestination.EditRecipe.route,
            arguments = listOf(navArgument("recipeId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId").orEmpty()
            val editRecipeViewModel: EditRecipeViewModel = viewModel(
                factory = EditRecipeViewModelFactory(
                    recipeId = recipeId,
                    recipeRepository = appContainer.recipeRepository,
                    ingredientRepository = appContainer.ingredientRepository,
                ),
            )
            EditRecipeScreen(
                uiState = editRecipeViewModel.uiState.collectAsStateWithLifecycle().value,
                onBackClick = { navController.popBackStack() },
                onSaveClick = editRecipeViewModel::saveRecipe,
                onSaveComplete = { navController.popBackStack(ChefesqueDestination.RecipeDetail.createRoute(recipeId), inclusive = false) },
                onTitleChange = editRecipeViewModel::updateTitle,
                onDescriptionChange = editRecipeViewModel::updateDescription,
                onServingsChange = editRecipeViewModel::updateServings,
                onPrepTimeChange = editRecipeViewModel::updatePrepTimeMinutes,
                onCookTimeChange = editRecipeViewModel::updateCookTimeMinutes,
                onRecipeTypeChange = editRecipeViewModel::updateRecipeType,
                onNotesChange = editRecipeViewModel::updateNotes,
                onAddIngredient = editRecipeViewModel::addIngredientRow,
                onRemoveIngredient = editRecipeViewModel::removeIngredientRow,
                onIngredientQueryChange = editRecipeViewModel::updateIngredientQuery,
                onIngredientSelected = editRecipeViewModel::selectIngredient,
                onQuantityChange = editRecipeViewModel::updateQuantity,
                onUnitChange = editRecipeViewModel::updateUnit,
                onPrepNoteChange = editRecipeViewModel::updatePrepNote,
                onSectionChange = editRecipeViewModel::updateSection,
                onOptionalChange = editRecipeViewModel::updateOptional,
                onAddStep = editRecipeViewModel::addStep,
                onRemoveStep = editRecipeViewModel::removeStep,
                onMoveStepUp = editRecipeViewModel::moveStepUp,
                onMoveStepDown = editRecipeViewModel::moveStepDown,
                onStepInstructionChange = editRecipeViewModel::updateStepInstruction,
                onStepTimerMinutesChange = editRecipeViewModel::updateStepTimerMinutes,
                onStepTimerSecondsChange = editRecipeViewModel::updateStepTimerSeconds,
                onStepWarningChange = editRecipeViewModel::updateStepWarning,
                onStepEquipmentChange = editRecipeViewModel::updateStepEquipment,
                onStepWhileTimerRunsChange = editRecipeViewModel::updateStepWhileTimerRuns,
                onStepCheckpointChange = editRecipeViewModel::updateStepCheckpoint,
                onToggleStepIngredientLink = editRecipeViewModel::toggleStepIngredientLink,
            )
        }
        composable(
            route = ChefesqueDestination.CookAlong.route,
            arguments = listOf(navArgument("recipeId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId").orEmpty()
            val cookAlongViewModel: CookAlongViewModel = viewModel(
                factory = CookAlongViewModelFactory(recipeId, appContainer.recipeRepository),
            )
            val returnToRecipeDetail: () -> Unit = {
                navController.popBackStack(ChefesqueDestination.RecipeDetail.createRoute(recipeId), inclusive = false)
            }
            CookAlongScreen(
                uiState = cookAlongViewModel.uiState.collectAsStateWithLifecycle().value,
                onBackClick = returnToRecipeDetail,
                onPreviousClick = cookAlongViewModel::goToPreviousStep,
                onNextClick = cookAlongViewModel::goToNextStep,
                onFinishClick = returnToRecipeDetail,
                onStartTimerClick = cookAlongViewModel::startTimer,
                onPauseTimerClick = cookAlongViewModel::pauseTimer,
                onResumeTimerClick = cookAlongViewModel::resumeTimer,
                onResetTimerClick = cookAlongViewModel::resetTimer,
                onAddMinuteClick = cookAlongViewModel::addOneMinute,
            )
        }
    }
}
