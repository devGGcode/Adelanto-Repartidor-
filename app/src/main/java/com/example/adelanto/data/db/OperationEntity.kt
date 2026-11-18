package com.example.adelanto.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class OpType { PAYMENT, COLLECTION, FULL }

enum class PayMethod { EFECTIVO, TRANSFERENCIA }

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
    val weekStart: Long,        // martes 00:00 de la semana (epoch millis)
    val method: PayMethod = PayMethod.EFECTIVO,  // método del pago (o del cobro si es cobro solo)
    val collectionMethod: PayMethod = PayMethod.EFECTIVO, // método del cobro (para FULL puede ser distinto)
    val conversionDone: Boolean = false, // ya se movió plata del cobro al método del pago
    val conversionTotal: Boolean = false // true = se movió el cobro completo (con adelanto)
)
