package com.kingkharnivore.chefesque.data.repository

import androidx.room.withTransaction
import com.kingkharnivore.chefesque.data.local.db.ChefesqueDatabase
import com.kingkharnivore.chefesque.data.local.entity.CookSessionComponentStatusEntity
import com.kingkharnivore.chefesque.data.local.entity.CookSessionEntity
import com.kingkharnivore.chefesque.domain.model.CookSessionStatus
import kotlinx.coroutines.flow.Flow

class CookSessionRepository(private val database: ChefesqueDatabase) {
    private val cookSessionDao = database.cookSessionDao()

    fun observeActiveSessions(): Flow<List<CookSessionEntity>> = cookSessionDao.observeSessionsByStatus(CookSessionStatus.ACTIVE.name)
    fun observeSessionsByStatus(status: CookSessionStatus): Flow<List<CookSessionEntity>> = cookSessionDao.observeSessionsByStatus(status.name)
    fun observeActiveSessionForRecipe(recipeId: String): Flow<CookSessionEntity?> = cookSessionDao.observeLatestSessionForRecipeByStatus(recipeId, CookSessionStatus.ACTIVE.name)
    suspend fun getCookSession(id: String): CookSessionEntity? = cookSessionDao.getCookSession(id)
    suspend fun upsertSession(session: CookSessionEntity) = cookSessionDao.upsertCookSession(session)
    suspend fun updateCurrentStep(sessionId: String, stepIndex: Int) = cookSessionDao.updateCurrentStep(sessionId, stepIndex)
    suspend fun completeSession(sessionId: String, status: CookSessionStatus, completedAt: Long, actualDurationSeconds: Int) = cookSessionDao.completeSession(sessionId, status.name, completedAt, actualDurationSeconds)
    suspend fun getComponentStatusesForSession(cookSessionId: String): List<CookSessionComponentStatusEntity> = cookSessionDao.getComponentStatusesForSession(cookSessionId)
    suspend fun upsertComponentStatuses(statuses: List<CookSessionComponentStatusEntity>) = cookSessionDao.upsertComponentStatuses(statuses)

    suspend fun saveSessionWithComponentStatuses(session: CookSessionEntity, statuses: List<CookSessionComponentStatusEntity>) = database.withTransaction {
        cookSessionDao.upsertCookSession(session)
        cookSessionDao.deleteComponentStatusesForSession(session.id)
        cookSessionDao.upsertComponentStatuses(statuses)
    }
}
