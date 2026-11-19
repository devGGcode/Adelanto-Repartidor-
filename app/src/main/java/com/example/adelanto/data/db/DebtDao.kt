package com.example.adelanto.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtDao {
    @Insert
    suspend fun insert(debt: DebtEntity): Long

    @Update
    suspend fun update(debt: DebtEntity)

    @Delete
    suspend fun delete(debt: DebtEntity)

    @Query("SELECT * FROM debts ORDER BY status ASC, createdAt DESC")
    fun observeAll(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE id = :id")
    suspend fun getById(id: Long): DebtEntity?

    @Query("UPDATE debts SET isGoal = 1 WHERE id = :id")
    suspend fun markGoal(id: Long)

    @Query("UPDATE debts SET isGoal = 0 WHERE id = :id")
    suspend fun unmarkGoal(id: Long)
}
