package com.kingkharnivore.chefesque.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cook_session_component_statuses",
    foreignKeys = [
        ForeignKey(entity = CookSessionEntity::class, parentColumns = ["id"], childColumns = ["cookSessionId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = RecipeComponentEntity::class, parentColumns = ["id"], childColumns = ["recipeComponentId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = RecipeEntity::class, parentColumns = ["id"], childColumns = ["childRecipeId"], onDelete = ForeignKey.RESTRICT),
    ],
    indices = [Index("cookSessionId"), Index("recipeComponentId"), Index("childRecipeId"), Index("status")],
)
data class CookSessionComponentStatusEntity(
    @PrimaryKey val id: String,
    val cookSessionId: String,
    val recipeComponentId: String,
    val childRecipeId: String,
    val status: String,
    val note: String?,
)
