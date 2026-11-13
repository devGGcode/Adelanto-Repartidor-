package com.example.adelanto.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.adelanto.data.db.OperationEntity
import com.example.adelanto.data.repository.OperationRepository
import com.example.adelanto.domain.WeekUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

data class DaySummary(
    val dayStart: Long,
    val weekStart: Long,
    val label: String,
    val total: Long,
    val count: Int
)

data class HistoryUiState(
    val days: List<DaySummary> = emptyList()
)

class HistoryViewModel(private val repo: OperationRepository) : ViewModel() {
    val uiState: StateFlow<HistoryUiState> = repo.observeAll()
        .flatMapLatest { all ->
            flow {
                val days = all.groupBy { WeekUtils.dayStartMillis(it.timestamp) }
                    .map { (ds, ops) ->
                        DaySummary(
                            dayStart = ds,
                            weekStart = ops.first().weekStart,
                            label = WeekUtils.formatDay(ds),
                            total = ops.sumOf { it.profit },
                            count = ops.size
                        )
                    }
                    .sortedByDescending { it.dayStart }
                emit(HistoryUiState(days))
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HistoryUiState())

    companion object {
        fun factory(repo: OperationRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = HistoryViewModel(repo) as T
        }
    }
}
