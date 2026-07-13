package com.kingkharnivore.chefesque.ui.screen.addrecipe

data class RecipePublishValidation(
    val missingTitle: Boolean,
    val missingIngredients: Boolean,
    val missingSteps: Boolean,
) {
    val isValid: Boolean get() = !missingTitle && !missingIngredients && !missingSteps
    val firstMissingTab: RecipeEditorTab? get() = when {
        missingTitle -> RecipeEditorTab.BASIC_INFO
        missingIngredients -> RecipeEditorTab.INGREDIENTS
        missingSteps -> RecipeEditorTab.STEPS
        else -> null
    }

    val stepErrorMessage: String? get() = if (missingSteps) "Add at least one step before publishing." else null
}

fun validateRecipeForPublish(
    title: String,
    ingredients: List<IngredientInputState>,
    steps: List<StepInputState>,
): RecipePublishValidation {
    val missingTitle = !title.hasVisibleContent()
    val missingIngredients = ingredients.none { it.query.hasVisibleContent() }
    val missingSteps = steps.none { it.isPublishableStep() }
    return RecipePublishValidation(
        missingTitle = missingTitle,
        missingIngredients = missingIngredients,
        missingSteps = missingSteps,
    )
}

fun StepInputState.isPublishableStep(): Boolean = title.hasVisibleContent() || instruction.hasVisibleContent()

fun StepInputState.hasAnyContent(): Boolean = title.hasVisibleContent() ||
    instruction.hasVisibleContent() ||
    timerMinutes.hasVisibleContent() ||
    timerSeconds.hasVisibleContent() ||
    warning.hasVisibleContent() ||
    equipment.hasVisibleContent() ||
    meanwhile.hasVisibleContent() ||
    checkpoint.hasVisibleContent() ||
    linkedIngredientLocalIds.isNotEmpty()

fun String.hasVisibleContent(): Boolean = any { !it.isWhitespace() && it != '\u00A0' }
