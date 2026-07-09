package com.kingkharnivore.chefesque.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kingkharnivore.chefesque.data.local.entity.CookingLogPhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CookingLogPhotoDao {
    @Query("SELECT * FROM cooking_log_photos WHERE cookingLogId = :logId ORDER BY sortOrder ASC")
    fun observePhotosForLog(logId: String): Flow<List<CookingLogPhotoEntity>>

    @Query("SELECT * FROM cooking_log_photos WHERE cookingLogId = :logId ORDER BY sortOrder ASC")
    suspend fun getPhotosForLog(logId: String): List<CookingLogPhotoEntity>

    @Query("""
        SELECT cooking_log_photos.* FROM cooking_log_photos
        INNER JOIN cooking_logs ON cooking_logs.id = cooking_log_photos.cookingLogId
        WHERE cooking_logs.recipeId = :recipeId
        ORDER BY cooking_logs.cookedAt DESC, cooking_log_photos.sortOrder ASC
    """)
    suspend fun getPhotosForRecipeLogs(recipeId: String): List<CookingLogPhotoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPhoto(photo: CookingLogPhotoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPhotos(photos: List<CookingLogPhotoEntity>)

    @Delete
    suspend fun deletePhoto(photo: CookingLogPhotoEntity)

    @Query("DELETE FROM cooking_log_photos WHERE cookingLogId = :logId")
    suspend fun deletePhotosForLog(logId: String)
}
