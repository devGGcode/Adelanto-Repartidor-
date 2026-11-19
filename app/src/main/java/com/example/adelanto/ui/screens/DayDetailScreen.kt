package com.example.adelanto.ui.screens

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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.adelanto.data.repository.OperationRepository
import com.example.adelanto.domain.OperationUtils
import com.example.adelanto.domain.WeekUtils
import com.example.adelanto.ui.theme.appBg
import com.example.adelanto.ui.theme.appBorder
import com.example.adelanto.ui.theme.appCard
import com.example.adelanto.ui.theme.appMuted
import com.example.adelanto.ui.theme.appText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailScreen(repo: OperationRepository, dayStart: Long, nav: NavController) {
    val flow = androidx.compose.runtime.remember { repo.observeAll() }
    val all by flow.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val ops = all.filter { WeekUtils.dayStartMillis(it.timestamp) == dayStart }.sortedByDescending { it.timestamp }

    // desglose del día por método de pago
    val efectivoTotal = ops.sumOf { OperationUtils.efectivo(it) }
    val transferTotal = ops.sumOf { OperationUtils.transferencia(it) }
    val total = efectivoTotal + transferTotal

    Scaffold(
        containerColor = appBg(),
        topBar = {
            TopAppBar(
                title = { Text(WeekUtils.formatDay(dayStart), color = appText()) },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(containerColor = appBg()),
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = appCard()),
                border = androidx.compose.foundation.BorderStroke(1.dp, appBorder()),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total adelanto del día", fontSize = 12.sp, color = appMuted(), fontWeight = FontWeight.Medium)
                    Text(
                        WeekUtils.formatMoney(total),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = appText(),
                        letterSpacing = (-0.3).sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                Icon(Icons.Filled.Payments, contentDescription = null, tint = appMuted(), modifier = Modifier.size(13.dp))
                                Text("Efectivo", fontSize = 11.5.sp, color = appMuted())
                            }
                            Text(WeekUtils.formatMoney(efectivoTotal), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = appText())
                        }
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .size(width = 1.dp, height = 30.dp)
                                .background(appBorder())
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                Icon(Icons.Filled.SwapHoriz, contentDescription = null, tint = appMuted(), modifier = Modifier.size(13.dp))
                                Text("Transferencia", fontSize = 11.5.sp, color = appMuted())
                            }
                            Text(WeekUtils.formatMoney(transferTotal), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = appText())
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ops, key = { it.id }) { op ->
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
    }
}
