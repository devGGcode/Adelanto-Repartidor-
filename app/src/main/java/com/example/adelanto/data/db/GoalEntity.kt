package com.example.adelanto.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class GoalFrequency { SEMANAL, MENSUAL }

/**
 * Meta de ahorro para comprar algo en el futuro (ej. PS5).
 * targetAmount = precio; savedAmount = lo que llevas ahorrado.
 */
@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetAmount: Long,
    val savedAmount: Long = 0,
    val frequency: GoalFrequency = GoalFrequency.SEMANAL,
    val targetDate: Long? = null,
    val photoPath: String? = null,
    val note: String? = null,
    val createdAt: Long,
    val completed: Boolean = false,
    val completedAt: Long? = null
)
