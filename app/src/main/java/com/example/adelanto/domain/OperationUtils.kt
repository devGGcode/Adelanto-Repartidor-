package com.example.adelanto.domain

import com.example.adelanto.data.db.OpType
import com.example.adelanto.data.db.OperationEntity
import com.example.adelanto.data.db.PayMethod

/**
 * Cuánto suma cada operación al efectivo y a la transferencia.
 *
 * En un Pago + Cobro el pago puede ser en efectivo y el cobro en transferencia:
 * el pago se descuenta del efectivo y el cobro se suma a la transferencia.
 * La suma de ambos lados siempre da la ganancia (profit) de la operación.
 */
object OperationUtils {

    fun efectivo(op: OperationEntity): Long = when (op.type) {
        OpType.PAYMENT -> paymentEffect(op, PayMethod.EFECTIVO)
        OpType.COLLECTION -> collectionEffect(op, PayMethod.EFECTIVO)
        OpType.FULL -> collectionEffect(op, PayMethod.EFECTIVO) - paymentEffect(op, PayMethod.EFECTIVO) +
            conversionAdjust(op, PayMethod.EFECTIVO)
    }

    fun transferencia(op: OperationEntity): Long = when (op.type) {
        OpType.PAYMENT -> paymentEffect(op, PayMethod.TRANSFERENCIA)
        OpType.COLLECTION -> collectionEffect(op, PayMethod.TRANSFERENCIA)
        OpType.FULL -> collectionEffect(op, PayMethod.TRANSFERENCIA) - paymentEffect(op, PayMethod.TRANSFERENCIA) +
            conversionAdjust(op, PayMethod.TRANSFERENCIA)
    }

    /**
     * Cuando el usuario ya movió la plata del método del cobro al método del pago,
     * se compensa el saldo: entra al método del pago y sale del método del cobro.
     * - Cambio parcial: solo el monto pagado (el adelanto queda donde se cobró).
     * - Cambio total: el cobro completo (el adelanto también se mueve).
     * El adelanto total no cambia.
     */
    private fun conversionAdjust(op: OperationEntity, method: PayMethod): Long {
        if (op.type != OpType.FULL || !op.conversionDone || op.method == op.collectionMethod) return 0L
        val amount = if (op.conversionTotal) (op.collectionAmount ?: 0L) else (op.paymentAmount ?: 0L)
        var effect = 0L
        if (op.method == method) effect += amount
        if (op.collectionMethod == method) effect -= amount
        return effect
    }

    /** un pago pendiente todavía no afecta el adelanto ni el desglose */
    private fun paymentEffect(op: OperationEntity, method: PayMethod): Long {
        if (op.status == "pending") return 0L
        return if (op.method == method) (op.paymentAmount ?: 0L) else 0L
    }

    private fun collectionEffect(op: OperationEntity, method: PayMethod): Long =
        if (op.collectionMethod == method) (op.collectionAmount ?: 0L) else 0L

    fun methodLabel(method: PayMethod): String =
        if (method == PayMethod.TRANSFERENCIA) "Transfer." else "Efectivo"

    fun methodName(method: PayMethod): String =
        if (method == PayMethod.TRANSFERENCIA) "transferencia" else "efectivo"

    /** Texto del aviso cuando el pago y el cobro usan métodos distintos, o null si son iguales */
    fun moveAdvice(op: OperationEntity): String? {
        if (op.type != OpType.FULL || op.method == op.collectionMethod || op.conversionDone) return null
        val amount = op.paymentAmount ?: 0L
        if (amount <= 0) return null
        return "Cambia ${WeekUtils.formatMoney(amount)} de ${methodName(op.collectionMethod)} a ${methodName(op.method)}"
    }

    /** "Efectivo → Transfer." cuando el pago y el cobro usan métodos distintos */
    fun methodSummary(op: OperationEntity): String = when (op.type) {
        OpType.PAYMENT -> methodLabel(op.method)
        OpType.COLLECTION -> methodLabel(op.collectionMethod)
        OpType.FULL ->
            if (op.method == op.collectionMethod) methodLabel(op.method)
            else methodLabel(op.method) + " → " + methodLabel(op.collectionMethod)
    }
}
