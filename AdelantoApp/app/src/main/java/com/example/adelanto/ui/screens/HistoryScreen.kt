package com.example.adelanto.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.adelanto.data.repository.OperationRepository
import com.example.adelanto.domain.WeekUtils
import com.example.adelanto.ui.viewmodel.DaySummary
import com.example.adelanto.ui.viewmodel.HistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(repo: OperationRepository, nav: NavController) {
    // Corregido: Especificamos explícitamente el tipo <HistoryViewModel> para resolver la ambigüedad
    val vm: HistoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = HistoryViewModel.factory(repo))
    val state by vm.uiState.collectAsState()

    Scaffold(
        containerColor = com.example.adelanto.ui.theme.appBg(),
        topBar = {
            TopAppBar(
                title = { Text("Historial por día", color = com.example.adelanto.ui.theme.appText()) },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(containerColor = com.example.adelanto.ui.theme.appBg()),
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        val daysList = state.days

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (daysList.isEmpty()) {
                item {
                    Text("Sin historial aún", modifier = Modifier.padding(16.dp))
                }
            } else {
                var lastWeek: Long? = null
                
                // Corregido: Usamos un bucle clásico indexado para inyectar los elementos estructuradamente en el LazyColumn
                for (i in daysList.indices) {
                    val d = daysList[i]
                    
                    if (lastWeek != d.weekStart) {
                        lastWeek = d.weekStart
                        item(key = "week_${d.weekStart}_$i") {
                            Text(
                                "Semana: ${WeekUtils.formatWeekRange(d.weekStart)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                              )
                        }
                    }
                    
                    item(key = "day_${d.dayStart}_$i") {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { nav.navigate("dayDetail/${d.dayStart}") }
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(d.label, fontWeight = FontWeight.SemiBold)
                                    Text("${d.count} operaciones", style = MaterialTheme.typography.bodySmall)
                                }
                                Text(
                                    WeekUtils.formatMoney(d.total),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
