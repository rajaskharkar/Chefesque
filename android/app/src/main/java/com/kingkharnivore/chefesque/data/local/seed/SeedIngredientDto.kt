package com.kingkharnivore.chefesque.data.local.seed

data class SeedIngredientDto(
    val id: String,
    val displayName: String,
    val canonicalName: String,
    val category: String?,
    val defaultUnit: String?,
    val commonUnits: List<String>,
    val aliases: List<String>,
)
