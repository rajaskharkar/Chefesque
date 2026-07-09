package com.kingkharnivore.chefesque.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recipe_steps",
    foreignKeys = [ForeignKey(entity = RecipeEntity::class, parentColumns = ["id"], childColumns = ["recipeId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("recipeId"), Index("sortOrder")],
)
data class RecipeStepEntity(
    @PrimaryKey val id: String,
    val recipeId: String,
    val instruction: String,
    val timerSeconds: Int?,
    val temperatureValue: Double?,
    val temperatureUnit: String?,
    val checkpoint: String?,
    val warning: String?,
    val equipment: String?,
    val whileTimerRuns: String?,
    val sortOrder: Int,
)
