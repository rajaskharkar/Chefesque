package com.kingkharnivore.chefesque.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cooking_logs",
    foreignKeys = [
        ForeignKey(entity = RecipeEntity::class, parentColumns = ["id"], childColumns = ["recipeId"], onDelete = ForeignKey.SET_NULL),
        ForeignKey(entity = CookSessionEntity::class, parentColumns = ["id"], childColumns = ["cookSessionId"], onDelete = ForeignKey.SET_NULL),
    ],
    indices = [Index("recipeId"), Index("cookSessionId"), Index("cookedAt"), Index("result"), Index("isFavorite")],
)
data class CookingLogEntity(
    @PrimaryKey val id: String,
    val recipeId: String?,
    val cookSessionId: String?,
    val titleSnapshot: String,
    val cookedAt: Long,
    val actualDurationSeconds: Int?,
    val result: String?,
    val wouldMakeAgain: String?,
    val whatWentWell: String?,
    val changesMade: String?,
    val notesForNextTime: String?,
    val isFavorite: Boolean,
    val createdFromCookAlong: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)
