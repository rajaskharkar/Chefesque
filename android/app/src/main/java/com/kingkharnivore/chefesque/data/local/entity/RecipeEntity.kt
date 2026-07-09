package com.kingkharnivore.chefesque.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recipes",
    indices = [
        Index("title"),
        Index("createdAt"),
        Index("updatedAt"),
        Index("archivedAt"),
    ],
)
data class RecipeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val servings: Int?,
    val prepTimeMinutes: Int?,
    val cookTimeMinutes: Int?,
    val coverImageUri: String?,
    val cuisine: String?,
    val difficulty: String?,
    val recipeType: String?,
    val notes: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val archivedAt: Long?,
)
