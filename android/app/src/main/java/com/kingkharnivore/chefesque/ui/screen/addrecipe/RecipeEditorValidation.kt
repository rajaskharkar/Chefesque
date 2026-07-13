package com.kingkharnivore.chefesque.ui.screen.addrecipe

data class RecipePublishValidation(
    val missingTitle: Boolean,
    val missingIngredients: Boolean,
    val missingSteps: Boolean,
    val hasStepContent: Boolean,
) {
    val isValid: Boolean get() = !missingTitle && !missingIngredients && !missingSteps
    val firstMissingTab: RecipeEditorTab? get() = when {
        missingTitle -> RecipeEditorTab.BASIC_INFO
        missingIngredients -> RecipeEditorTab.INGREDIENTS
        missingSteps -> RecipeEditorTab.STEPS
        else -> null
    }

    val stepErrorMessage: String? get() = when {
        !missingSteps -> null
        hasStepContent -> "Add an instruction to at least one step before publishing."
        else -> "Add at least one step before publishing."
    }
}

fun validateRecipeForPublish(
    title: String,
    ingredients: List<IngredientInputState>,
    steps: List<StepInputState>,
): RecipePublishValidation {
    val missingTitle = !title.hasVisibleContent()
    val missingIngredients = ingredients.none { it.query.hasVisibleContent() }
    val hasStepContent = steps.any { it.hasAnyContent() }
    val missingSteps = steps.none { it.hasInstruction() }
    return RecipePublishValidation(
        missingTitle = missingTitle,
        missingIngredients = missingIngredients,
        missingSteps = missingSteps,
        hasStepContent = hasStepContent,
    )
}

fun StepInputState.hasInstruction(): Boolean = instruction.hasVisibleContent()

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
