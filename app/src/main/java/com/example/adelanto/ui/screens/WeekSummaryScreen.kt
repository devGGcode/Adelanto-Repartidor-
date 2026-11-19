package com.example.adelanto.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Inbox
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.adelanto.data.repository.OperationRepository
import com.example.adelanto.domain.OperationUtils
import com.example.adelanto.domain.WeekSummary
import com.example.adelanto.domain.WeekSummaryUtils
import com.example.adelanto.domain.WeekUtils
import com.example.adelanto.ui.theme.appBg
import com.example.adelanto.ui.theme.appBorder
import com.example.adelanto.ui.theme.appCard
import com.example.adelanto.ui.theme.appDanger
import com.example.adelanto.ui.theme.appMuted
import com.example.adelanto.ui.theme.appPrimary
import com.example.adelanto.ui.theme.appText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekSummaryScreen(repo: OperationRepository, weekStart: Long, nav: NavController) {
    val flow = remember(weekStart) { repo.observeByWeek(weekStart) }
    val ops by flow.collectAsState(initial = emptyList())
    val ctx = LocalContext.current
    val prefs = remember { ctx.getSharedPreferences("adelanto", Context.MODE_PRIVATE) }
    val cajaEfectivo = remember(weekStart) { prefs.getString("caja_$weekStart", "")?.toLongOrNull() ?: 0L }
    val cajaTransferencia = remember(weekStart) { prefs.getString("caja_transfer_$weekStart", "")?.toLongOrNull() ?: 0L }
    val summary = WeekSummaryUtils.build(weekStart, ops, cajaEfectivo, cajaTransferencia)
    val scope = rememberCoroutineScope()

    val days = remember(ops) {
        ops.sortedByDescending { it.timestamp }
            .groupBy { WeekUtils.dayStartMillis(it.timestamp) }
            .toList()
            .sortedByDescending { it.first }
    }

    Scaffold(
        containerColor = appBg(),
        topBar = {
            TopAppBar(
                title = { Text("Resumen semanal", color = appText()) },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(containerColor = appBg()),
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = appMuted())
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { SummaryCard(summary) }

            if (ops.isEmpty() && cajaEfectivo == 0L && cajaTransferencia == 0L) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(40.dp, 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Filled.Inbox, contentDescription = null, tint = appMuted(), modifier = Modifier.size(40.dp))
                        Text("Sin movimientos esta semana", fontSize = 14.sp, color = appMuted())
                    }
                }
            }

            if (ops.isNotEmpty()) {
                item {
                    Text(
                        "Detalle de la semana",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = appText(),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                days.forEach { (dayStart, dayOps) ->
                    item(key = "day_$dayStart") {
                        val dayTotal = dayOps.sumOf { OperationUtils.efectivo(it) + OperationUtils.transferencia(it) }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                WeekUtils.formatDay(dayStart),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = appText(),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                WeekUtils.formatMoney(dayTotal),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (dayTotal < 0) appDanger() else appPrimary()
                            )
                        }
                    }
                    items(dayOps, key = { it.id }) { op ->
                        OpCard(
                            op = op,
                            onDelete = {},
                            onConversionDone = { total ->
                                scope.launch { repo.update(op.copy(conversionDone = true, conversionTotal = total)) }
                            },
                            onEdit = { nav.navigate("editOperation/${op.id}") }
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(72.dp)) }
        }
    }
}

@Composable
private fun SummaryCard(summary: WeekSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = appCard()),
        border = androidx.compose.foundation.BorderStroke(1.dp, appBorder()),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Semana", fontSize = 12.sp, color = appMuted(), fontWeight = FontWeight.Medium)
            Text(summary.label, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = appText())
            Spacer(Modifier.height(8.dp))
            Text("Adelanto de la semana", fontSize = 12.sp, color = appMuted(), fontWeight = FontWeight.Medium)
            Text(
                WeekUtils.formatMoney(summary.total),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = appPrimary(),
                letterSpacing = (-0.3).sp
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Efectivo", fontSize = 11.5.sp, color = appMuted())
                    Text(
                        WeekUtils.formatMoney(summary.efectivoTotal),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (summary.efectivoTotal < 0) appDanger() else appText()
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .size(width = 1.dp, height = 30.dp)
                        .background(appBorder())
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text("Transferencia", fontSize = 11.5.sp, color = appMuted())
                    Text(
                        WeekUtils.formatMoney(summary.transferenciaTotal),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (summary.transferenciaTotal < 0) appDanger() else appText()
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(appBorder()))
            Spacer(Modifier.height(10.dp))

            DetailRow(
                title = "Caja inicial (referencia)",
                efectivo = summary.cajaEfectivo,
                transferencia = summary.cajaTransferencia
            )
            Spacer(Modifier.height(8.dp))
            DetailRow(
                title = "Movimientos de la semana",
                efectivo = summary.movimientosEfectivo,
                transferencia = summary.movimientosTransferencia
            )
            Spacer(Modifier.height(8.dp))
            DetailRow(
                title = "Cierre estimado (referencia)",
                efectivo = summary.cierreEfectivo,
                transferencia = summary.cierreTransferencia
            )

            Spacer(Modifier.height(8.dp))
            Text(
                if (summary.count == 1) "1 operación" else "${summary.count} operaciones",
                fontSize = 11.5.sp,
                color = appMuted()
            )
        }
    }
}

@Composable
private fun DetailRow(title: String, efectivo: Long, transferencia: Long) {
    Text(title, fontSize = 11.sp, color = appMuted())
    Text(
        "Efectivo " + WeekUtils.formatMoney(efectivo),
        fontSize = 12.sp,
        color = if (efectivo < 0) appDanger() else appText()
    )
    Text(
        "Transferencia " + WeekUtils.formatMoney(transferencia),
        fontSize = 12.sp,
        color = if (transferencia < 0) appDanger() else appText()
    )
}
