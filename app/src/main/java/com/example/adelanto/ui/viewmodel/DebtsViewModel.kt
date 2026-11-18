package com.example.adelanto.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.adelanto.data.db.DebtEntity
import com.example.adelanto.data.db.DebtPriority
import com.example.adelanto.data.db.DebtType
import com.example.adelanto.data.repository.DebtRepository
import com.example.adelanto.domain.DebtUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DebtsUiState(
    val pending: List<DebtEntity> = emptyList(),
    val settled: List<DebtEntity> = emptyList(),
    val total: Long = 0L,
    val goals: List<DebtEntity> = emptyList(),
    val goalTotal: Long = 0L
)

class DebtsViewModel(private val repo: DebtRepository) : ViewModel() {

    /** críticas primero, luego por vencimiento más próximo, luego las más nuevas */
    private val order = compareBy<DebtEntity> { if (it.priority == DebtPriority.CRITICO) 0 else 1 }
        .thenBy { it.dueDate ?: Long.MAX_VALUE }
        .thenByDescending { it.createdAt }

    val uiState: StateFlow<DebtsUiState> = repo.observeAll()
        .map { all ->
            val pagar = all.filter { it.type == DebtType.PAGAR }
            val pending = pagar.filter { it.status == "pendiente" }.sortedWith(order)
            val goals = pending.filter { it.isGoal }
            DebtsUiState(
                pending = pending,
                settled = pagar.filter { it.status != "pendiente" },
                total = pending.sumOf { DebtUtils.nextPayment(it) },
                goals = goals,
                goalTotal = goals.sumOf { DebtUtils.nextPayment(it) }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DebtsUiState())

    fun payInstallment(debt: DebtEntity) = viewModelScope.launch { repo.payInstallment(debt) }
    fun reopen(debt: DebtEntity) = viewModelScope.launch { repo.reopen(debt) }
    fun delete(debt: DebtEntity) = viewModelScope.launch { repo.delete(debt) }

    fun toggleGoal(debt: DebtEntity) = viewModelScope.launch {
        if (debt.isGoal) repo.clearGoal(debt) else repo.setGoal(debt)
    }

    companion object {
        fun factory(repo: DebtRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = DebtsViewModel(repo) as T
        }
    }
}
