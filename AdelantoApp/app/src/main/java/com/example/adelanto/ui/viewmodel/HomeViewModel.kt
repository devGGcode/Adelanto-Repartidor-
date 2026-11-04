package com.example.adelanto.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.adelanto.data.db.OperationEntity
import com.example.adelanto.data.repository.OperationRepository
import com.example.adelanto.domain.WeekUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val weekStart: Long = 0L,
    val weekLabel: String = "",
    val operations: List<OperationEntity> = emptyList(),
    val total: Long = 0L
)

class HomeViewModel(private val repo: OperationRepository) : ViewModel() {
    private val weekStart = MutableStateFlow(WeekUtils.weekStartMillis())

    val uiState: StateFlow<HomeUiState> = weekStart.flatMapLatest { ws ->
        kotlinx.coroutines.flow.combine(repo.observeByWeek(ws), repo.observeWeekTotal(ws)) { ops, total ->
            HomeUiState(ws, WeekUtils.formatWeekRange(ws), ops, total)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        HomeUiState(WeekUtils.weekStartMillis(), WeekUtils.formatWeekRange(WeekUtils.weekStartMillis()))
    )

    fun refreshWeek() { weekStart.value = WeekUtils.weekStartMillis() }

    fun delete(op: OperationEntity) = viewModelScope.launch { repo.delete(op) }

    companion object {
        fun factory(repo: OperationRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeViewModel(repo) as T
        }
    }
}
