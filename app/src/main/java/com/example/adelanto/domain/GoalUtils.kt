package com.example.adelanto.domain

import com.example.adelanto.data.db.GoalEntity
import com.example.adelanto.data.db.GoalFrequency
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.math.ceil

object GoalUtils {
    fun remaining(goal: GoalEntity): Long =
        (goal.targetAmount - goal.savedAmount).coerceAtLeast(0L)

    fun progress(goal: GoalEntity): Float =
        if (goal.targetAmount <= 0) 0f
        else (goal.savedAmount.toFloat() / goal.targetAmount).coerceIn(0f, 1f)

    fun targetReached(goal: GoalEntity): Boolean = goal.savedAmount >= goal.targetAmount

    /** cuántas semanas o meses faltan para la fecha objetivo */
    fun periodsLeft(goal: GoalEntity, now: Long = System.currentTimeMillis()): Int? {
        val date = goal.targetDate ?: return null
        val today = Instant.ofEpochMilli(now).atZone(WeekUtils.zone).toLocalDate()
        val target = Instant.ofEpochMilli(date).atZone(WeekUtils.zone).toLocalDate()
        if (!target.isAfter(today)) return 1

        val periods = when (goal.frequency) {
            GoalFrequency.SEMANAL ->
                ceil(ChronoUnit.DAYS.between(today, target).toDouble() / 7.0).toInt()
            GoalFrequency.MENSUAL ->
                ChronoUnit.MONTHS.between(today.withDayOfMonth(1), target.withDayOfMonth(1)).toInt()
        }
        return periods.coerceAtLeast(1)
    }

    /** cuánto hay que juntar por semana o por mes para llegar en la fecha */
    fun perPeriod(goal: GoalEntity, now: Long = System.currentTimeMillis()): Long? {
        val periods = periodsLeft(goal, now) ?: return null
        val rest = remaining(goal)
        if (rest <= 0) return 0L
        return (rest + periods - 1) / periods
    }

    fun frequencyName(goal: GoalEntity): String =
        if (goal.frequency == GoalFrequency.SEMANAL) "semana" else "mes"

    fun frequencyPlural(goal: GoalEntity): String =
        if (goal.frequency == GoalFrequency.SEMANAL) "semanas" else "meses"
}
