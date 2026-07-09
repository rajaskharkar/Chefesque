package com.kingkharnivore.chefesque.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ingredients",
    indices = [
        Index("displayName"),
        Index("canonicalName"),
        Index("category"),
        Index("source"),
        Index("isUserCreated"),
    ],
)
data class IngredientEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val canonicalName: String,
    val category: String?,
    val defaultUnit: String?,
    val commonUnitsJson: String?,
    val source: String,
    val sourceId: String?,
    val isUserCreated: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)
