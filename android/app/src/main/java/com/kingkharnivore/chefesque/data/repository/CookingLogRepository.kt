package com.kingkharnivore.chefesque.data.repository

import androidx.room.withTransaction
import com.kingkharnivore.chefesque.data.local.db.ChefesqueDatabase
import com.kingkharnivore.chefesque.data.local.entity.CookingLogEntity
import com.kingkharnivore.chefesque.data.local.entity.CookingLogPhotoEntity
import kotlinx.coroutines.flow.Flow

class CookingLogRepository(private val database: ChefesqueDatabase) {
    private val logDao = database.cookingLogDao()
    private val photoDao = database.cookingLogPhotoDao()

    fun observeAllLogs(): Flow<List<CookingLogEntity>> = logDao.observeAllLogs()
    fun observeLogsForRecipe(recipeId: String): Flow<List<CookingLogEntity>> = logDao.observeLogsForRecipe(recipeId)
    fun observeLog(id: String): Flow<CookingLogEntity?> = logDao.observeLog(id)
    suspend fun getLog(id: String): CookingLogEntity? = logDao.getLog(id)
    suspend fun getLogForCookSession(cookSessionId: String): CookingLogEntity? = logDao.getLogForCookSession(cookSessionId)
    suspend fun upsertLog(log: CookingLogEntity) = logDao.upsertLog(log)
    suspend fun deleteLog(log: CookingLogEntity) = logDao.deleteLog(log)
    suspend fun updateFavorite(id: String, isFavorite: Boolean, updatedAt: Long) = logDao.updateFavorite(id, isFavorite, updatedAt)

    fun observePhotosForLog(logId: String): Flow<List<CookingLogPhotoEntity>> = photoDao.observePhotosForLog(logId)
    suspend fun getPhotosForLog(logId: String): List<CookingLogPhotoEntity> = photoDao.getPhotosForLog(logId)
    suspend fun getPhotosForRecipeLogs(recipeId: String): List<CookingLogPhotoEntity> = photoDao.getPhotosForRecipeLogs(recipeId)
    suspend fun upsertPhoto(photo: CookingLogPhotoEntity) = photoDao.upsertPhoto(photo)
    suspend fun upsertPhotos(photos: List<CookingLogPhotoEntity>) = photoDao.upsertPhotos(photos)

    suspend fun saveLogWithPhotos(log: CookingLogEntity, photos: List<CookingLogPhotoEntity>) = database.withTransaction {
        logDao.upsertLog(log)
        photoDao.deletePhotosForLog(log.id)
        photoDao.upsertPhotos(photos)
    }
}
