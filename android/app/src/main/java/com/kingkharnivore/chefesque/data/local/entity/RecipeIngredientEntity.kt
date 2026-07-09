package com.kingkharnivore.chefesque.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recipe_ingredients",
    foreignKeys = [
        ForeignKey(entity = RecipeEntity::class, parentColumns = ["id"], childColumns = ["recipeId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = IngredientEntity::class, parentColumns = ["id"], childColumns = ["ingredientId"], onDelete = ForeignKey.SET_NULL),
    ],
    indices = [Index("recipeId"), Index("ingredientId"), Index("section"), Index("sortOrder"), Index(value = ["recipeId", "sortOrder"])],
)
data class RecipeIngredientEntity(
    @PrimaryKey val id: String,
    val recipeId: String,
    val ingredientId: String?,
    val nameSnapshot: String,
    val quantity: Double?,
    val quantityText: String?,
    val unit: String?,
    val prepNote: String?,
    val section: String?,
    val optional: Boolean,
    val sortOrder: Int,
)
