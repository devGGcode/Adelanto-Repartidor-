package com.example.adelanto.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.adelanto.data.db.GoalEntity
import com.example.adelanto.data.repository.GoalRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GoalsUiState(
    val pending: List<GoalEntity> = emptyList(),
    val completed: List<GoalEntity> = emptyList(),
    val totalSaved: Long = 0L,
    val totalTarget: Long = 0L
)

class GoalsViewModel(private val repo: GoalRepository) : ViewModel() {
    val uiState: StateFlow<GoalsUiState> = repo.observeAll()
        .map { all ->
            val pending = all.filter { !it.completed }
            val completed = all.filter { it.completed }
            GoalsUiState(
                pending = pending,
                completed = completed,
                totalSaved = pending.sumOf { it.savedAmount },
                totalTarget = pending.sumOf { it.targetAmount }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GoalsUiState())

    fun addSaving(goal: GoalEntity, amount: Long) =
        viewModelScope.launch { repo.addSaving(goal, amount) }

    fun markPurchased(goal: GoalEntity) =
        viewModelScope.launch { repo.markPurchased(goal) }

    fun reopen(goal: GoalEntity) =
        viewModelScope.launch { repo.reopen(goal) }

    fun delete(goal: GoalEntity) =
        viewModelScope.launch { repo.delete(goal) }

    companion object {
        fun factory(repo: GoalRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = GoalsViewModel(repo) as T
        }
    }
}
