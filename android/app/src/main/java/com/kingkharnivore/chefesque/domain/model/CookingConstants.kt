package com.kingkharnivore.chefesque.domain.model

enum class CookSessionStatus { ACTIVE, COMPLETED, ABANDONED }

enum class ComponentCookStatus { MAKE_NOW, ALREADY_MADE, SUBSTITUTE, SKIPPED }

enum class CookingResult { GREAT, GOOD, OKAY, NEEDS_WORK, FAILED }

enum class WouldMakeAgain { YES, MAYBE, NO }

enum class CookingLogPhotoType { FINAL_DISH, IN_PROGRESS, PLATING, INGREDIENT_PREP, COMPONENT, MISTAKE, OTHER }

enum class RecipeType { FULL_DISH, COMPONENT, SAUCE, SPICE_BLEND, DOUGH, MARINADE, DRINK, DESSERT, SNACK, OTHER }


enum class IngredientSource { CURATED, USER }
