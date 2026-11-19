package com.example.adelanto.data.repository

import androidx.room.withTransaction
import com.example.adelanto.data.backup.BackupData
import com.example.adelanto.data.db.AppDatabase

class BackupRepository(private val db: AppDatabase) {
    suspend fun allOperations() = db.operationDao().getAll()
    suspend fun allDebts() = db.debtDao().getAll()
    suspend fun allGoals() = db.goalDao().getAll()

    /** reemplaza todos los datos por los de la copia, en una sola transacción */
    suspend fun restore(data: BackupData) {
        db.withTransaction {
            db.operationDao().deleteAll()
            db.debtDao().deleteAll()
            if (data.goalsIncluded) {
                db.goalDao().deleteAll()
            }
            if (data.operations.isNotEmpty()) db.operationDao().insertAll(data.operations)
            if (data.debts.isNotEmpty()) db.debtDao().insertAll(data.debts)
            if (data.goalsIncluded && data.goals.isNotEmpty()) db.goalDao().insertAll(data.goals)
        }
    }
}
