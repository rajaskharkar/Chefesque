package com.kingkharnivore.chefesque.data.local.seed

import com.kingkharnivore.chefesque.data.local.entity.IngredientAliasEntity
import com.kingkharnivore.chefesque.data.local.entity.IngredientEntity
import com.kingkharnivore.chefesque.domain.model.IngredientSource

internal object IngredientSeedMapper {
    fun SeedIngredientDto.toEntity(now: Long): IngredientEntity = IngredientEntity(
        id = id,
        displayName = displayName.trim(),
        canonicalName = IngredientNormalizer.normalize(canonicalName),
        category = category,
        defaultUnit = defaultUnit,
        commonUnitsJson = IngredientSeedDataSource.commonUnitsToJson(commonUnits),
        source = IngredientSource.CURATED.name,
        sourceId = id,
        isUserCreated = false,
        createdAt = now,
        updatedAt = now,
    )

    fun SeedIngredientDto.toAliases(): List<IngredientAliasEntity> = aliases
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinctBy { IngredientNormalizer.normalize(it) }
        .map { alias ->
            val normalized = IngredientNormalizer.normalize(alias)
            IngredientAliasEntity(
                id = "${id}_alias_${normalized.replace(' ', '_')}",
                ingredientId = id,
                alias = alias,
                normalizedAlias = normalized,
                language = "en",
            )
        }
}
