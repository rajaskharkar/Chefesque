package com.kingkharnivore.chefesque.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cook_sessions",
    foreignKeys = [ForeignKey(entity = RecipeEntity::class, parentColumns = ["id"], childColumns = ["recipeId"], onDelete = ForeignKey.SET_NULL)],
    indices = [Index("recipeId"), Index("status"), Index("startedAt"), Index("completedAt")],
)
data class CookSessionEntity(
    @PrimaryKey val id: String,
    val recipeId: String?,
    val titleSnapshot: String,
    val startedAt: Long,
    val completedAt: Long?,
    val status: String,
    val currentStepIndex: Int,
    val actualDurationSeconds: Int?,
    val createdFromRecipe: Boolean,
    val timerOriginalSeconds: Int?,
    val timerRemainingSeconds: Int?,
    val timerStatus: String?,
    val updatedAt: Long,
)
