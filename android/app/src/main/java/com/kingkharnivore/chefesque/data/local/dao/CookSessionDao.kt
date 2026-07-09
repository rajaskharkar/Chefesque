package com.kingkharnivore.chefesque.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kingkharnivore.chefesque.data.local.entity.CookSessionComponentStatusEntity
import com.kingkharnivore.chefesque.data.local.entity.CookSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CookSessionDao {
    @Query("SELECT * FROM cook_sessions WHERE status = :status ORDER BY startedAt DESC")
    fun observeSessionsByStatus(status: String): Flow<List<CookSessionEntity>>

    @Query("SELECT * FROM cook_sessions WHERE recipeId = :recipeId AND status = :status ORDER BY startedAt DESC LIMIT 1")
    fun observeLatestSessionForRecipeByStatus(recipeId: String, status: String): Flow<CookSessionEntity?>

    @Query("SELECT * FROM cook_sessions WHERE id = :id")
    suspend fun getCookSession(id: String): CookSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCookSession(session: CookSessionEntity)

    @Query("UPDATE cook_sessions SET currentStepIndex = :stepIndex WHERE id = :sessionId")
    suspend fun updateCurrentStep(sessionId: String, stepIndex: Int)

    @Query("""
        UPDATE cook_sessions
        SET status = :status, completedAt = :completedAt, actualDurationSeconds = :actualDurationSeconds
        WHERE id = :sessionId
    """)
    suspend fun completeSession(sessionId: String, status: String, completedAt: Long, actualDurationSeconds: Int)

    @Query("SELECT * FROM cook_session_component_statuses WHERE cookSessionId = :cookSessionId")
    suspend fun getComponentStatusesForSession(cookSessionId: String): List<CookSessionComponentStatusEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertComponentStatuses(statuses: List<CookSessionComponentStatusEntity>)

    @Query("DELETE FROM cook_session_component_statuses WHERE cookSessionId = :cookSessionId")
    suspend fun deleteComponentStatusesForSession(cookSessionId: String)
}
