package com.kingkharnivore.chefesque.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kingkharnivore.chefesque.data.local.entity.CookingLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CookingLogDao {
    @Query("SELECT * FROM cooking_logs ORDER BY cookedAt DESC")
    fun observeAllLogs(): Flow<List<CookingLogEntity>>

    @Query("SELECT * FROM cooking_logs WHERE recipeId = :recipeId ORDER BY cookedAt DESC")
    fun observeLogsForRecipe(recipeId: String): Flow<List<CookingLogEntity>>

    @Query("SELECT * FROM cooking_logs WHERE id = :id")
    fun observeLog(id: String): Flow<CookingLogEntity?>

    @Query("SELECT * FROM cooking_logs WHERE id = :id")
    suspend fun getLog(id: String): CookingLogEntity?

    @Query("SELECT * FROM cooking_logs WHERE cookSessionId = :cookSessionId LIMIT 1")
    suspend fun getLogForCookSession(cookSessionId: String): CookingLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLog(log: CookingLogEntity)

    @Delete
    suspend fun deleteLog(log: CookingLogEntity)

    @Query("UPDATE cooking_logs SET isFavorite = :isFavorite, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean, updatedAt: Long)
}
