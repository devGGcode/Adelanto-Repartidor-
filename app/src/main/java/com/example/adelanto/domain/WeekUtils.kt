package com.example.adelanto.domain

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object WeekUtils {
    val zone: ZoneId = ZoneId.systemDefault()

    fun weekStartMillis(now: Long = System.currentTimeMillis()): Long =
        weekStartMillis(Instant.ofEpochMilli(now).atZone(zone).toLocalDate())

    fun weekStartMillis(date: LocalDate): Long {
        val daysBack = (date.dayOfWeek.value - DayOfWeek.TUESDAY.value + 7) % 7
        val tuesday = date.minusDays(daysBack.toLong())
        return tuesday.atStartOfDay(zone).toInstant().toEpochMilli()
    }

    fun weekEndMillis(weekStart: Long): Long {
        // Monday 23:59:59.999 of same week (weekStart Tuesday + 6 days)
        val start = Instant.ofEpochMilli(weekStart).atZone(zone).toLocalDate()
        val monday = start.plusDays(6)
        return monday.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
    }

    fun formatWeekRange(weekStart: Long): String {
        val start = Instant.ofEpochMilli(weekStart).atZone(zone).toLocalDate()
        val end = start.plusDays(6)
        val fmt = DateTimeFormatter.ofPattern("dd MMM", Locale("es", "ES"))
        return "${start.format(fmt)} - ${end.format(fmt)}"
    }

    fun formatMoney(amount: Long): String {
        // pesos with thousands separator (negativos: -$1.000)
        val sign = if (amount < 0) "-" else ""
        val abs = kotlin.math.abs(amount)
        return sign + "$" + String.format(Locale("es", "CL"), "%,d", abs).replace(',', '.')
    }

    fun formatDate(millis: Long): String {
        val d = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
        val fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale("es", "ES"))
        return Instant.ofEpochMilli(millis).atZone(zone).format(fmt)
    }

    fun formatDateShort(millis: Long): String {
        val d = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
        val fmt = DateTimeFormatter.ofPattern("dd MMM", Locale("es", "ES"))
        return d.format(fmt)
    }

    /** el DatePicker entrega milisegundos en UTC; lo pasamos al inicio del día local */
    fun pickerDateToLocalStart(utcMillis: Long): Long {
        val date = Instant.ofEpochMilli(utcMillis).atZone(java.time.ZoneOffset.UTC).toLocalDate()
        return date.atStartOfDay(zone).toInstant().toEpochMilli()
    }

    fun formatDueDate(millis: Long): String {
        val d = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
        val fmt = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("es", "ES"))
        return d.format(fmt)
    }

    fun isOverdue(dueDate: Long?, now: Long = System.currentTimeMillis()): Boolean =
        dueDate != null && dueDate < dayStartMillis(now)

    fun addMonths(millis: Long, months: Long): Long {
        val d = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate().plusMonths(months)
        return d.atStartOfDay(zone).toInstant().toEpochMilli()
    }

    fun addWeeks(millis: Long, weeks: Long): Long {
        val d = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate().plusWeeks(weeks)
        return d.atStartOfDay(zone).toInstant().toEpochMilli()
    }

    /** próximo vencimiento al pagar una cuota: exactamente un mes después */
    fun nextDueDate(currentDue: Long?, now: Long = System.currentTimeMillis()): Long {
        return addMonths(currentDue ?: dayStartMillis(now), 1)
    }

    /** milisegundos que faltan para el próximo martes 00:00 (inicio de semana) */
    fun millisUntilNextWeek(now: Long = System.currentTimeMillis()): Long {
        val ws = weekStartMillis(now)
        val next = Instant.ofEpochMilli(ws).atZone(zone).toLocalDate()
            .plusDays(7).atStartOfDay(zone).toInstant().toEpochMilli()
        return (next - now).coerceAtLeast(1000L)
    }

    fun formatTime(millis: Long): String {
        val fmt = DateTimeFormatter.ofPattern("HH:mm", Locale("es", "ES"))
        return Instant.ofEpochMilli(millis).atZone(zone).format(fmt)
    }

    fun dayStartMillis(millis: Long): Long {
        val d = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
        return d.atStartOfDay(zone).toInstant().toEpochMilli()
    }

    fun formatDay(millis: Long): String {
        val d = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
        val fmt = DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy", Locale("es", "ES"))
        return d.format(fmt).replaceFirstChar { it.uppercase() }
    }
}
