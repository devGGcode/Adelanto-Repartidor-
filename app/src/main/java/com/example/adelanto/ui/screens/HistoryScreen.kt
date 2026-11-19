package com.example.adelanto.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.adelanto.data.repository.OperationRepository
import com.example.adelanto.domain.WeekUtils
import com.example.adelanto.ui.theme.appBg
import com.example.adelanto.ui.theme.appBorder
import com.example.adelanto.ui.theme.appCard
import com.example.adelanto.ui.theme.appMuted
import com.example.adelanto.ui.theme.appPrimary
import com.example.adelanto.ui.theme.appText
import com.example.adelanto.ui.viewmodel.HistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(repo: OperationRepository, nav: NavController) {
    // Corregido: Especificamos explícitamente el tipo <HistoryViewModel> para resolver la ambigüedad
    val vm: HistoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = HistoryViewModel.factory(repo))
    val state by vm.uiState.collectAsState()

    Scaffold(
        containerColor = appBg(),
        topBar = {
            TopAppBar(
                title = { Text("Historial por día", color = appText()) },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(containerColor = appBg()),
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
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(40.dp, 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Filled.Inbox, contentDescription = null, tint = appMuted(), modifier = Modifier.size(40.dp))
                        Text("Sin historial aún", fontSize = 14.sp, color = appMuted())
                    }
                }
            } else {
                var lastWeek: Long? = null

                // Corregido: Usamos un bucle clásico indexado para inyectar los elementos estructuradamente en el LazyColumn
                for (i in daysList.indices) {
                    val d = daysList[i]

                    if (lastWeek != d.weekStart) {
                        lastWeek = d.weekStart
                        val weekDays = daysList.filter { it.weekStart == d.weekStart }
                        val weekEf = weekDays.sumOf { it.efectivo }
                        val weekTr = weekDays.sumOf { it.transfer }
                        val weekTotal = weekEf + weekTr

                        item(key = "week_${d.weekStart}_$i") {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .clickable { nav.navigate("weekSummary/${d.weekStart}") },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = appCard()),
                                border = BorderStroke(1.dp, appBorder()),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            "Semana: ${WeekUtils.formatWeekRange(d.weekStart)}",
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = appPrimary(),
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            WeekUtils.formatMoney(weekTotal),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = appText()
                                        )
                                        Icon(
                                            Icons.Filled.ChevronRight,
                                            contentDescription = "Ver resumen semanal",
                                            tint = appMuted(),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Text("Efectivo ${WeekUtils.formatMoney(weekEf)}", fontSize = 11.5.sp, color = appMuted())
                                        Text("Transfer. ${WeekUtils.formatMoney(weekTr)}", fontSize = 11.5.sp, color = appMuted())
                                    }
                                }
                            }
                        }
                    }

                    item(key = "day_${d.dayStart}_$i") {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { nav.navigate("dayDetail/${d.dayStart}") },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = appCard()),
                            border = BorderStroke(1.dp, appBorder()),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(d.label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = appText())
                                        Text(
                                            if (d.count == 1) "1 operación" else "${d.count} operaciones",
                                            fontSize = 12.sp,
                                            color = appMuted()
                                        )
                                    }
                                    Text(
                                        WeekUtils.formatMoney(d.total),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = appPrimary()
                                    )
                                }

                                // desglose del día por método de pago
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Filled.Payments, contentDescription = null, tint = appMuted(), modifier = Modifier.size(13.dp))
                                        Text("Efectivo", fontSize = 11.5.sp, color = appMuted())
                                        Text(WeekUtils.formatMoney(d.efectivo), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = appText())
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Filled.SwapHoriz, contentDescription = null, tint = appMuted(), modifier = Modifier.size(13.dp))
                                        Text("Transfer.", fontSize = 11.5.sp, color = appMuted())
                                        Text(WeekUtils.formatMoney(d.transfer), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = appText())
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
