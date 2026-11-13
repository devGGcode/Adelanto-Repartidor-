package com.example.adelanto.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.adelanto.data.db.OpType
import com.example.adelanto.data.repository.OperationRepository
import com.example.adelanto.domain.WeekUtils
import com.example.adelanto.ui.theme.appBg
import com.example.adelanto.ui.theme.appBorder
import com.example.adelanto.ui.theme.appCard
import com.example.adelanto.ui.theme.appDanger
import com.example.adelanto.ui.theme.appDangerBg
import com.example.adelanto.ui.theme.appHeroGradient
import com.example.adelanto.ui.theme.appMuted
import com.example.adelanto.ui.theme.appPrimary
import com.example.adelanto.ui.theme.appPrimaryLight
import com.example.adelanto.ui.theme.appSuccess
import com.example.adelanto.ui.theme.appSuccessBg
import com.example.adelanto.ui.theme.appText
import com.example.adelanto.ui.theme.appWarningBg
import com.example.adelanto.ui.theme.appWarningText
import com.example.adelanto.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(repo: OperationRepository, nav: NavController) {
    // Corregido: Especificamos explícitamente el tipo <HomeViewModel> para solucionar la ambigüedad
    val vm: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = HomeViewModel.factory(repo))
    val state by vm.uiState.collectAsState()
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val prefs = ctx.getSharedPreferences("adelanto", android.content.Context.MODE_PRIVATE)
    var cajaValue by remember { mutableStateOf(prefs.getString("caja_${state.weekStart}", "") ?: "") }
    var showCajaDialog by remember { mutableStateOf(false) }
    var cajaInput by remember { mutableStateOf("") }
    LaunchedEffect(state.weekStart) {
        cajaValue = prefs.getString("caja_${state.weekStart}", "") ?: ""
    }

    Scaffold(
        containerColor = appBg(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(appPrimaryLight()),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = appPrimary(), modifier = Modifier.size(20.dp))
                        }
                        Text("Adelanto Repartidor", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = appText())
                    }
                },
                actions = {
                    IconButton(onClick = { nav.navigate("history") }) {
                        Icon(Icons.Filled.History, contentDescription = "Historial", tint = appMuted())
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(containerColor = appBg())
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { nav.navigate("add") },
                containerColor = appPrimary(),
                contentColor = androidx.compose.ui.graphics.Color.White,
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Hero card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(appHeroGradient())
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f)).padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(14.dp))
                                    Text(state.weekLabel, color = androidx.compose.ui.graphics.Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Filled.Receipt, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f), modifier = Modifier.size(14.dp))
                                Text("${state.operations.size} op.", color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("Adelanto Acumulado", color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text(WeekUtils.formatMoney(state.total), color = androidx.compose.ui.graphics.Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp)
                    }
                }
            }
            // Cashbox card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = appCard()),
                    border = androidx.compose.foundation.BorderStroke(1.dp, appBorder()),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp, 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(appWarningBg()),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = appWarningText(), modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Text("Dinero en Caja (Efectivo)", fontSize = 12.sp, color = appMuted(), fontWeight = FontWeight.Medium)
                                Text(WeekUtils.formatMoney(cajaValue.toLongOrNull() ?: 0L), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = appText())
                            }
                        }
                        TextButton(onClick = { cajaInput = cajaValue; showCajaDialog = true }) {
                            Text("Editar", color = appText(), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Section header
            item {
                Text("Operaciones de la semana", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = appText(), modifier = Modifier.padding(top = 4.dp))
            }

            // Operations list
            if (state.operations.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(40.dp, 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Filled.Inbox, contentDescription = null, tint = appMuted(), modifier = Modifier.size(40.dp))
                        Text("No hay operaciones registradas", fontSize = 14.sp, color = appMuted())
                    }
                }
            } else {
                items(state.operations.sortedByDescending { it.timestamp }) { op ->
                    OpCard(op, onDelete = { vm.delete(op) })
                }
            }
        }

        if (showCajaDialog) {
            AlertDialog(
                onDismissRequest = { showCajaDialog = false },
                title = { Text("Dinero en Caja", fontWeight = FontWeight.Bold, color = appText()) },
                text = {
                    OutlinedTextField(
                        value = cajaInput,
                        onValueChange = { cajaInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Monto") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        prefs.edit().putString("caja_${state.weekStart}", cajaInput).apply()
                        cajaValue = cajaInput
                        showCajaDialog = false
                    }) { Text("Guardar", color = appPrimary(), fontWeight = FontWeight.SemiBold) }
                },
                dismissButton = {
                    TextButton(onClick = { showCajaDialog = false }) { Text("Cancelar", color = appMuted()) }
                },
                containerColor = appCard()
            )
        }
    }
}

@Composable
fun OpCard(op: com.example.adelanto.data.db.OperationEntity, onDelete: () -> Unit) {
    val badgeColor = when (op.type) {
        com.example.adelanto.data.db.OpType.PAYMENT -> appDangerBg() to appDanger()
        com.example.adelanto.data.db.OpType.COLLECTION -> appSuccessBg() to appSuccess()
        com.example.adelanto.data.db.OpType.FULL -> appPrimaryLight() to appPrimary()
    }
    val label = when (op.type) {
        com.example.adelanto.data.db.OpType.PAYMENT -> "Pago pendiente"
        com.example.adelanto.data.db.OpType.COLLECTION -> "Cobro solo"
        com.example.adelanto.data.db.OpType.FULL -> "Pago + Cobro"
    }
    val amounts = when (op.type) {
        com.example.adelanto.data.db.OpType.PAYMENT -> "Pagado: ${WeekUtils.formatMoney(op.paymentAmount ?: 0L)}"
        com.example.adelanto.data.db.OpType.COLLECTION -> "Cobrado: ${WeekUtils.formatMoney(op.collectionAmount ?: 0L)}"
        com.example.adelanto.data.db.OpType.FULL -> "Pagado: ${WeekUtils.formatMoney(op.paymentAmount ?: 0L)} | Cobrado: ${WeekUtils.formatMoney(op.collectionAmount ?: 0L)}"
    }
    val profitColor = if (op.profit >= 0) appSuccess() else appDanger()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = appCard()),
        border = androidx.compose.foundation.BorderStroke(1.dp, appBorder()),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(badgeColor.first).padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(label.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = badgeColor.second)
                }
                Text(WeekUtils.formatTime(op.timestamp), fontSize = 12.sp, color = appMuted())
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(amounts, fontSize = 13.sp, color = appMuted())
                Text(WeekUtils.formatMoney(op.profit), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = profitColor)
            }
            if (!op.note.isNullOrBlank() || true) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    if (!op.note.isNullOrBlank()) {
                        Text(op.note!!, fontSize = 12.sp, color = appMuted(), fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, maxLines = 1)
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        // Corregido: Cambiado Icons.Filled.Trash2 por Icons.Filled.Delete de Android nativo
                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = appMuted(), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
