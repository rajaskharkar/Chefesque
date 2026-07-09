package com.kingkharnivore.chefesque.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cooking_log_photos",
    foreignKeys = [ForeignKey(entity = CookingLogEntity::class, parentColumns = ["id"], childColumns = ["cookingLogId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("cookingLogId"), Index("photoType"), Index("sortOrder"), Index("createdAt"), Index(value = ["cookingLogId", "sortOrder"])],
)
data class CookingLogPhotoEntity(
    @PrimaryKey val id: String,
    val cookingLogId: String,
    val imageUri: String,
    val caption: String?,
    val photoType: String?,
    val sortOrder: Int,
    val createdAt: Long,
)
