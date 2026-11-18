package com.example.adelanto.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DebtType { COBRAR, PAGAR }

/** NORMAL = sin apuro · CRITICO = prioridad para pagar */
enum class DebtPriority { NORMAL, CRITICO }

/**
 * COBRAR = alguien me debe plata (por recolectar)
 * PAGAR  = yo debo plata (por pagar)
 *
 * isGoal = marcada como objetivo de pago (se muestra en Inicio y se descuenta del adelanto)
 */
@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: DebtType = DebtType.PAGAR,
    val name: String,        // persona o descripción
    val amount: Long,
    val note: String?,
    val createdAt: Long,
    val status: String,      // pendiente / saldada
    val settledAt: Long? = null,
    val dueDate: Long? = null,   // fecha de vencimiento (inicio del día, hora local)
    val installments: Int = 1,   // cantidad total de cuotas
    val paidInstallments: Int = 0, // cuotas ya pagadas
    val isFixedMonthly: Boolean = false, // se repite todos los meses (no tiene fin de cuotas)
    val priority: DebtPriority = DebtPriority.NORMAL,
    val isGoal: Boolean = false
)
