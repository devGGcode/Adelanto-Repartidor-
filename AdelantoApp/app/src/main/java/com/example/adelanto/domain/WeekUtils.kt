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
        // pesos with thousands separator
        return "$" + String.format(Locale("es", "CL"), "%,d", amount).replace(',', '.')
    }

    fun formatDate(millis: Long): String {
        val d = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
        val fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale("es", "ES"))
        return Instant.ofEpochMilli(millis).atZone(zone).format(fmt)
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
