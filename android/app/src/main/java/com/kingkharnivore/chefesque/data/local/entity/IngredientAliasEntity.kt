package com.kingkharnivore.chefesque.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ingredient_aliases",
    foreignKeys = [
        ForeignKey(
            entity = IngredientEntity::class,
            parentColumns = ["id"],
            childColumns = ["ingredientId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("ingredientId"),
        Index("alias"),
        Index("normalizedAlias"),
        Index("language"),
    ],
)
data class IngredientAliasEntity(
    @PrimaryKey val id: String,
    val ingredientId: String,
    val alias: String,
    val normalizedAlias: String,
    val language: String,
)
