package com.example.adelanto.domain

import com.example.adelanto.data.db.DebtEntity

/** Cálculos de cuotas de una deuda por pagar */
object DebtUtils {
    /** valor de cada cuota (la última absorbe el redondeo) */
    fun cuotaAmount(debt: DebtEntity): Long =
        if (debt.installments <= 1) debt.amount else debt.amount / debt.installments

    /** monto que todavía falta pagar (0 si ya está saldada) */
    fun remainingAmount(debt: DebtEntity): Long {
        if (debt.status != "pendiente") return 0L
        val total = debt.installments.coerceAtLeast(1)
        val paid = debt.paidInstallments.coerceIn(0, total)
        return (debt.amount - paid * cuotaAmount(debt)).coerceAtLeast(0L)
    }

    /** cuota que toca pagar ahora (la última absorbe el redondeo) */
    fun nextPayment(debt: DebtEntity): Long {
        if (debt.status != "pendiente") return 0L
        val total = debt.installments.coerceAtLeast(1)
        val paid = debt.paidInstallments.coerceIn(0, total)
        if (paid >= total) return 0L
        return if (paid == total - 1) remainingAmount(debt) else cuotaAmount(debt)
    }

    /** cuotas pagadas, acotado al total */
    fun paidCount(debt: DebtEntity): Int =
        debt.paidInstallments.coerceIn(0, debt.installments.coerceAtLeast(1))

    /** cuota que toca pagar (1-based); null si ya no quedan */
    fun nextInstallment(debt: DebtEntity): Int? {
        val total = debt.installments.coerceAtLeast(1)
        val next = paidCount(debt) + 1
        return if (next <= total) next else null
    }
}
