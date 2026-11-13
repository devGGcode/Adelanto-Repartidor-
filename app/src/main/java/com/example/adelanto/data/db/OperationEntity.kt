package com.example.adelanto.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class OpType { PAYMENT, COLLECTION, FULL }

@Entity(tableName = "operations")
data class OperationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: OpType,
    val paymentAmount: Long?,   // pago
    val collectionAmount: Long?, // cobro
    val profit: Long,           // adelanto aportado
    val status: String,         // pending / resolved / standalone
    val note: String?,
    val timestamp: Long,
    val weekStart: Long         // martes 00:00 de la semana (epoch millis)
)
