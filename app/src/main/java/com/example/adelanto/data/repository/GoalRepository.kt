package com.example.adelanto.data.repository

import com.example.adelanto.data.db.GoalDao
import com.example.adelanto.data.db.GoalEntity
import kotlinx.coroutines.flow.Flow

class GoalRepository(private val dao: GoalDao) {
    fun observeAll(): Flow<List<GoalEntity>> = dao.observeAll()
    suspend fun insert(goal: GoalEntity): Long = dao.insert(goal)
    suspend fun update(goal: GoalEntity) = dao.update(goal)
    suspend fun delete(goal: GoalEntity) = dao.delete(goal)
    suspend fun getById(id: Long): GoalEntity? = dao.getById(id)

    suspend fun addSaving(goal: GoalEntity, amount: Long) =
        dao.update(
            goal.copy(
                savedAmount = (goal.savedAmount + amount).coerceIn(0L, goal.targetAmount)
            )
        )

    suspend fun markPurchased(goal: GoalEntity) =
        dao.update(goal.copy(completed = true, completedAt = System.currentTimeMillis()))

    suspend fun reopen(goal: GoalEntity) =
        dao.update(goal.copy(completed = false, completedAt = null))
}
