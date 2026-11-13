package com.example.adelanto.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface OperationDao {
    @Insert
    suspend fun insert(op: OperationEntity): Long

    @Update
    suspend fun update(op: OperationEntity)

    @Delete
    suspend fun delete(op: OperationEntity)

    @Query("SELECT * FROM operations WHERE weekStart = :weekStart ORDER BY timestamp DESC")
    fun observeByWeek(weekStart: Long): Flow<List<OperationEntity>>

    @Query("SELECT * FROM operations ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<OperationEntity>>

    @Query("SELECT COALESCE(SUM(profit),0) FROM operations WHERE weekStart = :weekStart")
    fun observeWeekTotal(weekStart: Long): Flow<Long>

    @Query("SELECT DISTINCT weekStart FROM operations ORDER BY weekStart DESC")
    fun observeWeekStarts(): Flow<List<Long>>

    @Query("SELECT * FROM operations WHERE weekStart = :weekStart ORDER BY timestamp DESC")
    suspend fun getByWeek(weekStart: Long): List<OperationEntity>

    @Query("SELECT * FROM operations WHERE type = 'PAYMENT' AND weekStart = :weekStart AND status = 'pending' ORDER BY timestamp DESC")
    suspend fun getPendingPayments(weekStart: Long): List<OperationEntity>
}
