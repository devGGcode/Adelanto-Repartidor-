package com.example.adelanto.data.repository

import com.example.adelanto.data.db.OperationDao
import com.example.adelanto.data.db.OperationEntity
import kotlinx.coroutines.flow.Flow

class OperationRepository(private val dao: OperationDao) {
    fun observeByWeek(ws: Long): Flow<List<OperationEntity>> = dao.observeByWeek(ws)
    fun observeAll(): Flow<List<OperationEntity>> = dao.observeAll()
    suspend fun getById(id: Long): OperationEntity? = dao.getById(id)
    fun observeWeekTotal(ws: Long): Flow<Long> = dao.observeWeekTotal(ws)
    fun observeWeekStarts(): Flow<List<Long>> = dao.observeWeekStarts()
    suspend fun insert(op: OperationEntity): Long = dao.insert(op)
    suspend fun update(op: OperationEntity) = dao.update(op)
    suspend fun delete(op: OperationEntity) = dao.delete(op)
    suspend fun getPendingPayments(ws: Long): List<OperationEntity> = dao.getUnlinkedPendingPayments(ws)
    suspend fun getByWeek(ws: Long): List<OperationEntity> = dao.getByWeek(ws)
}
