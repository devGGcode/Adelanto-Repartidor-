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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.SwapHoriz
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.adelanto.data.db.OpType
import com.example.adelanto.data.db.DebtType
import com.example.adelanto.data.repository.OperationRepository
import com.example.adelanto.data.repository.DebtRepository
import com.example.adelanto.domain.DebtUtils
import com.example.adelanto.domain.OperationUtils
import com.example.adelanto.domain.WeekSummary
import com.example.adelanto.domain.WeekSummaryUtils
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
fun HomeScreen(
    repo: OperationRepository,
    debtRepo: DebtRepository,
    nav: NavController,
    onToggleTheme: () -> Unit
) {
    // Corregido: Especificamos explícitamente el tipo <HomeViewModel> para solucionar la ambigüedad
    val vm: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = HomeViewModel.factory(repo))
    val state by vm.uiState.collectAsState()
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val prefs = ctx.getSharedPreferences("adelanto", android.content.Context.MODE_PRIVATE)
    var cajaValue by remember { mutableStateOf(prefs.getString("caja_${state.weekStart}", "") ?: "") }
    var cajaTransferValue by remember { mutableStateOf(prefs.getString("caja_transfer_${state.weekStart}", "") ?: "") }
    var showCajaDialog by remember { mutableStateOf(false) }
    var cajaInput by remember { mutableStateOf("") }
    var cajaTransferInput by remember { mutableStateOf("") }
    var weeklySummary by remember { mutableStateOf<WeekSummary?>(null) }
    LaunchedEffect(state.weekStart) {
        cajaValue = prefs.getString("caja_${state.weekStart}", "") ?: ""
        cajaTransferValue = prefs.getString("caja_transfer_${state.weekStart}", "") ?: ""
    }

    // al pasar de lunes a martes 00:00 se cambia de semana: la caja vuelve a cero
    LaunchedEffect(Unit) {
        vm.refreshWeek()
        while (true) {
            kotlinx.coroutines.delay(WeekUtils.millisUntilNextWeek())
            vm.refreshWeek()
        }
    }

    // al abrir en una semana nueva, muestra el resumen de la semana cerrada (una sola vez)
    LaunchedEffect(state.weekStart) {
        val currentWs = state.weekStart
        if (currentWs <= 0L) return@LaunchedEffect
        val seen = prefs.getLong("week_summary_seen", 0L)
        if (seen >= currentWs) return@LaunchedEffect
        prefs.edit().putLong("week_summary_seen", currentWs).apply()

        val lastWs = WeekUtils.addWeeks(currentWs, -1)
        val lastOps = repo.getByWeek(lastWs)
        val cajaEf = prefs.getString("caja_$lastWs", "")?.toLongOrNull() ?: 0L
        val cajaTr = prefs.getString("caja_transfer_$lastWs", "")?.toLongOrNull() ?: 0L
        if (lastOps.isNotEmpty() || cajaEf != 0L || cajaTr != 0L) {
            weeklySummary = WeekSummaryUtils.build(lastWs, lastOps, cajaEf, cajaTr)
        }
    }

    // Desglose por método: el efectivo incluye la caja en efectivo y la transferencia la caja transferida.
    // En un Pago + Cobro, el pago se descuenta de su método y el cobro se suma al suyo.
    val caja = cajaValue.toLongOrNull() ?: 0L
    val cajaTransfer = cajaTransferValue.toLongOrNull() ?: 0L
    val efectivoOps = state.operations.sumOf { OperationUtils.efectivo(it) }
    val transferOps = state.operations.sumOf { OperationUtils.transferencia(it) }
    val efectivoTotal = efectivoOps + caja
    val transferTotal = transferOps + cajaTransfer
    val totalAcum = efectivoTotal + transferTotal

    // deudas pendientes (solo por pagar; no afectan el adelanto)
    val debts by debtRepo.observeAll().collectAsState(initial = emptyList())
    val deudasPendientes = debts.filter { it.status == "pendiente" && it.type == DebtType.PAGAR }
    val porPagar = deudasPendientes.sumOf { DebtUtils.nextPayment(it) }

    // deudas marcadas como objetivo de pago: se pagan con el adelanto
    val objetivos = deudasPendientes.filter { it.isGoal }
    val totalObjetivos = objetivos.sumOf { DebtUtils.nextPayment(it) }
    val disponible = totalAcum - totalObjetivos

    Scaffold(
        containerColor = appBg(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(appPrimaryLight())
                                .clickable { onToggleTheme() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.AccountBalanceWallet,
                                contentDescription = "Cambiar modo oscuro",
                                tint = appPrimary(),
                                modifier = Modifier.size(20.dp)
                            )
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 84.dp)
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(WeekUtils.formatMoney(totalAcum), color = androidx.compose.ui.graphics.Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp)
                            if (totalObjetivos > 0) {
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "−",
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(Modifier.width(6.dp))
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color.White.copy(alpha = 0.22f))
                                        .padding(horizontal = 9.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Flag,
                                        contentDescription = "Objetivos de pago",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        WeekUtils.formatMoney(totalObjetivos),
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                        if (totalObjetivos > 0) {
                            Spacer(Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    if (disponible < 0) "Falta = " else "Queda = ",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    WeekUtils.formatMoney(if (disponible < 0) -disponible else disponible),
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Icon(Icons.Filled.Payments, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f), modifier = Modifier.size(13.dp))
                                    Text("Efectivo", color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f), fontSize = 11.5.sp)
                                }
                                Text(WeekUtils.formatMoney(efectivoTotal), color = androidx.compose.ui.graphics.Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .size(width = 1.dp, height = 30.dp)
                                    .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.25f))
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Icon(Icons.Filled.SwapHoriz, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f), modifier = Modifier.size(13.dp))
                                    Text("Transferencia", color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f), fontSize = 11.5.sp)
                                }
                                Text(WeekUtils.formatMoney(transferTotal), color = androidx.compose.ui.graphics.Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            // Objetivos de pago (deudas marcadas desde Deudas)
            if (objetivos.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { nav.navigate("debts") },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = appPrimaryLight()),
                        border = androidx.compose.foundation.BorderStroke(1.dp, appPrimary()),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(appPrimary()),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Flag, contentDescription = null, tint = Color.White, modifier = Modifier.size(19.dp))
                                }
                                Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                                    Text(
                                        if (objetivos.size == 1) "OBJETIVO DE PAGO" else "OBJETIVOS DE PAGO",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = appPrimary(),
                                        letterSpacing = 0.6.sp
                                    )
                                    Text(
                                        (if (objetivos.size == 1) "1 deuda" else "${objetivos.size} deudas") +
                                            " · " + WeekUtils.formatMoney(totalObjetivos),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = appText()
                                    )
                                }
                                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = appPrimary())
                            }

                            objetivos.forEach { goal ->
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        goal.name,
                                        fontSize = 12.5.sp,
                                        color = appText(),
                                        maxLines = 1,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        WeekUtils.formatMoney(DebtUtils.nextPayment(goal)),
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = appDanger()
                                    )
                                }
                                val metaParts = buildList {
                                    if (goal.isFixedMonthly) {
                                        add("Fija mensual")
                                    } else if (goal.installments > 1) {
                                        add("Cuota ${DebtUtils.nextInstallment(goal) ?: goal.installments} de ${goal.installments}")
                                    }
                                    goal.dueDate?.let { add("Vence ${WeekUtils.formatDueDate(it)}") }
                                }
                                if (metaParts.isNotEmpty()) {
                                    Text(
                                        metaParts.joinToString(" · "),
                                        fontSize = 11.sp,
                                        color = if (WeekUtils.isOverdue(goal.dueDate)) appDanger() else appMuted()
                                    )
                                }
                            }
                        }
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
                                Text("Dinero en Caja", fontSize = 12.sp, color = appMuted(), fontWeight = FontWeight.Medium)
                                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                    Column {
                                        Text("Efectivo", fontSize = 10.5.sp, color = appMuted())
                                        Text(WeekUtils.formatMoney(caja), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = appText())
                                    }
                                    Column {
                                        Text("Transferencia", fontSize = 10.5.sp, color = appMuted())
                                        Text(WeekUtils.formatMoney(cajaTransfer), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = appText())
                                    }
                                }
                            }
                        }
                        TextButton(onClick = {
                            cajaInput = cajaValue
                            cajaTransferInput = cajaTransferValue
                            showCajaDialog = true
                        }) {
                            Text("Editar", color = appText(), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Deudas card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { nav.navigate("debts") },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = appCard()),
                    border = androidx.compose.foundation.BorderStroke(1.dp, appBorder()),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp, 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(appDangerBg()),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.AccountBalance, contentDescription = null, tint = appDanger(), modifier = Modifier.size(20.dp))
                        }
                        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                            Text("Deudas por pagar", fontSize = 12.sp, color = appMuted(), fontWeight = FontWeight.Medium)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(WeekUtils.formatMoney(porPagar), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = appText())
                                Text(
                                    if (deudasPendientes.size == 1) "1 pendiente" else "${deudasPendientes.size} pendientes",
                                    fontSize = 11.5.sp,
                                    color = appMuted()
                                )
                            }
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = appMuted())
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
                    OpCard(
                        op = op,
                        onDelete = { vm.delete(op) },
                        onConversionDone = { total -> vm.markConversionDone(op, total) }
                    )
                }
            }
        }

        if (showCajaDialog) {
            AlertDialog(
                onDismissRequest = { showCajaDialog = false },
                title = { Text("Dinero en Caja", fontWeight = FontWeight.Bold, color = appText()) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = cajaInput,
                            onValueChange = { cajaInput = it.filter { c -> c.isDigit() } },
                            label = { Text("Efectivo") },
                            leadingIcon = { Text("$") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                        )
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = cajaTransferInput,
                            onValueChange = { cajaTransferInput = it.filter { c -> c.isDigit() } },
                            label = { Text("Transferencia") },
                            leadingIcon = { Text("$") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        prefs.edit()
                            .putString("caja_${state.weekStart}", cajaInput)
                            .putString("caja_transfer_${state.weekStart}", cajaTransferInput)
                            .apply()
                        cajaValue = cajaInput
                        cajaTransferValue = cajaTransferInput
                        showCajaDialog = false
                    }) { Text("Guardar", color = appPrimary(), fontWeight = FontWeight.SemiBold) }
                },
                dismissButton = {
                    TextButton(onClick = { showCajaDialog = false }) { Text("Cancelar", color = appMuted()) }
                },
                containerColor = appCard()
            )
        }

        weeklySummary?.let { s ->
            AlertDialog(
                onDismissRequest = { weeklySummary = null },
                title = { Text("Semana cerrada", fontWeight = FontWeight.Bold, color = appText()) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(s.label, fontSize = 13.sp, color = appMuted())
                        Text("Adelanto semanal", fontSize = 12.sp, color = appMuted())
                        Text(
                            WeekUtils.formatMoney(s.total),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = appPrimary()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                            Column {
                                Text("Efectivo", fontSize = 11.sp, color = appMuted())
                                Text(WeekUtils.formatMoney(s.efectivoTotal), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = appText())
                            }
                            Column {
                                Text("Transferencia", fontSize = 11.sp, color = appMuted())
                                Text(WeekUtils.formatMoney(s.transferenciaTotal), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = appText())
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("Caja inicial", fontSize = 11.sp, color = appMuted())
                        Text(
                            "Efectivo ${WeekUtils.formatMoney(s.cajaEfectivo)} · Transferencia ${WeekUtils.formatMoney(s.cajaTransferencia)}",
                            fontSize = 12.sp,
                            color = appText()
                        )
                        Text("Movimientos", fontSize = 11.sp, color = appMuted())
                        Text(
                            "Efectivo ${WeekUtils.formatMoney(s.movimientosEfectivo)} · Transferencia ${WeekUtils.formatMoney(s.movimientosTransferencia)}",
                            fontSize = 12.sp,
                            color = appText()
                        )
                        Text(
                            if (s.count == 1) "1 operación" else "${s.count} operaciones",
                            fontSize = 11.5.sp,
                            color = appMuted()
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val ws = s.weekStart
                        weeklySummary = null
                        nav.navigate("weekSummary/$ws")
                    }) { Text("Ver detalle", color = appPrimary(), fontWeight = FontWeight.SemiBold) }
                },
                dismissButton = {
                    TextButton(onClick = { weeklySummary = null }) { Text("Cerrar", color = appMuted()) }
                },
                containerColor = appCard()
            )
        }
    }
}

@Composable
fun OpCard(
    op: com.example.adelanto.data.db.OperationEntity,
    onDelete: () -> Unit,
    onConversionDone: (Boolean) -> Unit = {}
) {
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val methodText = OperationUtils.methodSummary(op)
                    Icon(
                        imageVector = if (methodText.contains("Transfer")) Icons.Filled.SwapHoriz else Icons.Filled.Payments,
                        contentDescription = null,
                        tint = appMuted(),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(methodText, fontSize = 12.sp, color = appMuted(), maxLines = 1)
                    Text("·", fontSize = 12.sp, color = appMuted())
                    Text(WeekUtils.formatTime(op.timestamp), fontSize = 12.sp, color = appMuted())
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(amounts, fontSize = 13.sp, color = appMuted())
                Text(WeekUtils.formatMoney(op.profit), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = profitColor)
            }
            val mixedMethods = op.type == com.example.adelanto.data.db.OpType.FULL &&
                op.method != op.collectionMethod
            if (mixedMethods && op.conversionDone) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(appSuccessBg())
                        .padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = appSuccess(), modifier = Modifier.size(14.dp))
                    Text(
                        if (op.conversionTotal) {
                            "Cambio total · el adelanto quedó en ${OperationUtils.methodName(op.method)}"
                        } else {
                            "Cambio parcial · el adelanto quedó en ${OperationUtils.methodName(op.collectionMethod)}"
                        },
                        fontSize = 11.5.sp,
                        color = appSuccess(),
                        modifier = Modifier.weight(1f)
                    )
                    if (!op.conversionTotal) {
                        TextButton(onClick = { onConversionDone(true) }) {
                            Text(
                                "Cambio total",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = appSuccess()
                            )
                        }
                    }
                }
            } else {
                OperationUtils.moveAdvice(op)?.let { advice ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(appWarningBg())
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Column {
                            Text(advice, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = appWarningText())
                            if (op.profit > 0) {
                                Text(
                                    "El adelanto de ${WeekUtils.formatMoney(op.profit)} queda en ${OperationUtils.methodName(op.collectionMethod)}",
                                    fontSize = 11.sp,
                                    color = appWarningText(),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                TextButton(onClick = { onConversionDone(false) }) {
                                    Text(
                                        "Parcial ${WeekUtils.formatMoney(op.paymentAmount ?: 0L)}",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = appWarningText()
                                    )
                                }
                                TextButton(onClick = { onConversionDone(true) }) {
                                    Text(
                                        "Total ${WeekUtils.formatMoney(op.collectionAmount ?: 0L)}",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = appWarningText()
                                    )
                                }
                            }
                        }
                    }
                }
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
