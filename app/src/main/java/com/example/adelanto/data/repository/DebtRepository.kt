package com.example.adelanto.data.repository

import com.example.adelanto.data.db.DebtDao
import com.example.adelanto.data.db.DebtEntity
import com.example.adelanto.domain.DebtUtils
import com.example.adelanto.domain.WeekUtils
import kotlinx.coroutines.flow.Flow

class DebtRepository(private val dao: DebtDao) {
    fun observeAll(): Flow<List<DebtEntity>> = dao.observeAll()
    suspend fun insert(debt: DebtEntity): Long = dao.insert(debt)
    suspend fun update(debt: DebtEntity) = dao.update(debt)
    suspend fun delete(debt: DebtEntity) = dao.delete(debt)

    /**
     * Paga una cuota.
     * - Si todavía quedan cuotas: avanza el vencimiento un mes y sigue pendiente.
     * - Si era la última: queda saldada y deja de ser objetivo de pago.
     */
    suspend fun payInstallment(debt: DebtEntity) {
        // deuda fija: se paga este mes y vuelve el mes siguiente (nunca termina)
        if (debt.isFixedMonthly) {
            dao.update(debt.copy(dueDate = WeekUtils.nextDueDate(debt.dueDate)))
            return
        }

        val total = debt.installments.coerceAtLeast(1)
        val nextPaid = DebtUtils.paidCount(debt) + 1
        if (nextPaid >= total) {
            dao.update(
                debt.copy(
                    paidInstallments = total,
                    status = "saldada",
                    settledAt = System.currentTimeMillis(),
                    isGoal = false
                )
            )
        } else {
            dao.update(
                debt.copy(
                    paidInstallments = nextPaid,
                    dueDate = WeekUtils.nextDueDate(debt.dueDate)
                )
            )
        }
    }

    /** reabre una deuda saldada dejando la última cuota como pendiente */
    suspend fun reopen(debt: DebtEntity) {
        val total = debt.installments.coerceAtLeast(1)
        val paid = if (debt.paidInstallments >= total) (total - 1).coerceAtLeast(0) else debt.paidInstallments
        dao.update(debt.copy(status = "pendiente", settledAt = null, paidInstallments = paid))
    }

    /** se pueden marcar varias deudas como objetivo de pago */
    suspend fun setGoal(debt: DebtEntity) = dao.markGoal(debt.id)

    suspend fun clearGoal(debt: DebtEntity) = dao.unmarkGoal(debt.id)
}
