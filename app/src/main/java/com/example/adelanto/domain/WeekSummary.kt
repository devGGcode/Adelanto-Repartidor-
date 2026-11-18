package com.example.adelanto.domain

import com.example.adelanto.data.db.OperationEntity

/**
 * Resumen de una semana:
 * - caja inicial (lo que había en efectivo y transferencia al empezar)
 * - movimientos de las operaciones de la semana
 * - adelanto total por método
 */
data class WeekSummary(
    val weekStart: Long,
    val label: String,
    val cajaEfectivo: Long,
    val cajaTransferencia: Long,
    val movimientosEfectivo: Long,
    val movimientosTransferencia: Long,
    val count: Int
) {
    val efectivoTotal: Long get() = cajaEfectivo + movimientosEfectivo
    val transferenciaTotal: Long get() = cajaTransferencia + movimientosTransferencia
    val total: Long get() = efectivoTotal + transferenciaTotal
}

object WeekSummaryUtils {
    fun build(
        weekStart: Long,
        ops: List<OperationEntity>,
        cajaEfectivo: Long,
        cajaTransferencia: Long
    ): WeekSummary = WeekSummary(
        weekStart = weekStart,
        label = WeekUtils.formatWeekRange(weekStart),
        cajaEfectivo = cajaEfectivo,
        cajaTransferencia = cajaTransferencia,
        movimientosEfectivo = ops.sumOf { OperationUtils.efectivo(it) },
        movimientosTransferencia = ops.sumOf { OperationUtils.transferencia(it) },
        count = ops.size
    )
}
