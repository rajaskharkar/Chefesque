package com.kingkharnivore.chefesque.ui.navigation

/**
 * Route names reserved for future cooking app destinations.
 *
 * These declarations intentionally do not wire Navigation Compose yet; they
 * provide stable names for upcoming My Recipes and Cooking Log flows without
 * changing the starter app behavior in Pass 0.
 */
sealed class ChefesqueDestination(val route: String) {
    data object Main : ChefesqueDestination("main")
    data object MyRecipes : ChefesqueDestination("my_recipes")
    data object CookingLog : ChefesqueDestination("cooking_log")
    data object AddRecipe : ChefesqueDestination("add_recipe")
    data object AddLog : ChefesqueDestination("add_log")

    data object CookingLogDetail : ChefesqueDestination("cooking_log/{logId}") {
        fun createRoute(logId: String): String = "cooking_log/$logId"
    }
    data object CookAlong : ChefesqueDestination("cook_along/{recipeId}") {
        fun createRoute(recipeId: String): String = "cook_along/$recipeId"
    }

    data object CookAlongCompletion : ChefesqueDestination("cook_along_completion/{sessionId}") {
        fun createRoute(sessionId: String): String = "cook_along_completion/$sessionId"
    }

    data object ImportRecipe : ChefesqueDestination("import_recipe")

    data object RecipeDetail : ChefesqueDestination("recipe_detail/{recipeId}") {
        fun createRoute(recipeId: String): String = "recipe_detail/$recipeId"
    }

    data object EditRecipe : ChefesqueDestination("edit_recipe/{recipeId}") {
        fun createRoute(recipeId: String): String = "edit_recipe/$recipeId"
    }

    data object EditLog : ChefesqueDestination("edit_log/{logId}") {
        fun createRoute(logId: String): String = "edit_log/$logId"
    }
}
