package com.kingkharnivore.chefesque.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recipe_components",
    foreignKeys = [
        ForeignKey(entity = RecipeEntity::class, parentColumns = ["id"], childColumns = ["parentRecipeId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = RecipeEntity::class, parentColumns = ["id"], childColumns = ["childRecipeId"], onDelete = ForeignKey.RESTRICT),
    ],
    indices = [Index("parentRecipeId"), Index("childRecipeId"), Index("sortOrder")],
)
data class RecipeComponentEntity(
    @PrimaryKey val id: String,
    val parentRecipeId: String,
    val childRecipeId: String,
    val quantity: Double?,
    val unit: String?,
    val note: String?,
    val scaleWithParent: Boolean,
    val sortOrder: Int,
)
