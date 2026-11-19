package com.example.adelanto.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.adelanto.data.db.DebtEntity
import com.example.adelanto.data.db.DebtPriority
import com.example.adelanto.data.repository.DebtRepository
import com.example.adelanto.domain.DebtUtils
import com.example.adelanto.domain.WeekUtils
import com.example.adelanto.ui.theme.appBg
import com.example.adelanto.ui.theme.appBorder
import com.example.adelanto.ui.theme.appCard
import com.example.adelanto.ui.theme.appDanger
import com.example.adelanto.ui.theme.appDangerBg
import com.example.adelanto.ui.theme.appMuted
import com.example.adelanto.ui.theme.appPrimary
import com.example.adelanto.ui.theme.appPrimaryLight
import com.example.adelanto.ui.theme.appSuccess
import com.example.adelanto.ui.theme.appText
import com.example.adelanto.ui.viewmodel.DebtsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtsScreen(debtRepo: DebtRepository, nav: NavController) {
    val vm: DebtsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = DebtsViewModel.factory(debtRepo))
    val state by vm.uiState.collectAsState()

    Scaffold(
        containerColor = appBg(),
        topBar = {
            TopAppBar(
                title = { Text("Deudas por pagar", color = appText()) },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(containerColor = appBg()),
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { nav.navigate("addDebt") },
                containerColor = appPrimary(),
                contentColor = Color.White,
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar deuda")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = appCard()),
                    border = BorderStroke(1.dp, appBorder()),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total de cuotas por pagar", fontSize = 12.sp, color = appMuted(), fontWeight = FontWeight.Medium)
                        Text(
                            WeekUtils.formatMoney(state.total),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = appDanger(),
                            letterSpacing = (-0.3).sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            (if (state.pending.size == 1) "1 pendiente" else "${state.pending.size} pendientes") +
                                " · " +
                                (if (state.settled.size == 1) "1 pagada" else "${state.settled.size} pagadas"),
                            fontSize = 12.sp,
                            color = appMuted()
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(appBg())
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(appPrimaryLight()),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Receipt,
                                    contentDescription = null,
                                    tint = appPrimary(),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                                Text(
                                    "Total absoluto",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = appText()
                                )
                                Text(
                                    "Todas las cuotas restantes",
                                    fontSize = 10.5.sp,
                                    color = appMuted()
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    WeekUtils.formatMoney(state.pending.sumOf { DebtUtils.remainingAmount(it) }),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = appText()
                                )
                                Text(
                                    "REFERENCIA",
                                    fontSize = 9.sp,
                                    color = appMuted(),
                                    letterSpacing = 0.4.sp
                                )
                            }
                        }
                        if (state.goals.isNotEmpty()) {
                            Spacer(Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Filled.Flag, contentDescription = null, tint = appPrimary(), modifier = Modifier.size(14.dp))
                                Text(
                                    (if (state.goals.size == 1) "1 objetivo de pago" else "${state.goals.size} objetivos de pago") +
                                        " · " + WeekUtils.formatMoney(state.goalTotal),
                                    fontSize = 12.sp,
                                    color = appPrimary(),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            if (state.pending.isNotEmpty()) {
                item { SectionHeader("Pendientes", appDanger()) }
                items(state.pending, key = { "p_${it.id}" }) { d ->
                    DebtCard(
                        debt = d,
                        onToggleGoal = { vm.toggleGoal(d) },
                        onEdit = { nav.navigate("editDebt/${d.id}") },
                        onAction = { vm.payInstallment(d) },
                        onDelete = { vm.delete(d) }
                    )
                }
            }

            if (state.pending.isEmpty() && state.settled.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(40.dp, 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Filled.Inbox, contentDescription = null, tint = appMuted(), modifier = Modifier.size(40.dp))
                        Text("No tienes deudas por pagar", fontSize = 14.sp, color = appMuted())
                        Text("Toca + para agregar una", fontSize = 12.sp, color = appMuted())
                    }
                }
            }

            if (state.settled.isNotEmpty()) {
                item { SectionHeader("Pagadas", appMuted()) }
                items(state.settled, key = { "s_${it.id}" }) { d ->
                    DebtCard(
                        debt = d,
                        settled = true,
                        onToggleGoal = {},
                        onAction = { vm.reopen(d) },
                        onDelete = { vm.delete(d) }
                    )
                }
            }

            item { Spacer(Modifier.height(72.dp)) }
        }
    }
}

@Composable
private fun SectionHeader(text: String, color: Color) {
    Text(
        text,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    )
}

@Composable
private fun Badge(text: String, color: Color, bg: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(text, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Composable
private fun DebtCard(
    debt: DebtEntity,
    settled: Boolean = false,
    onToggleGoal: () -> Unit,
    onAction: () -> Unit,
    onDelete: () -> Unit,
    onEdit: (() -> Unit)? = null
) {
    val overdue = !settled && WeekUtils.isOverdue(debt.dueDate)
    val totalCuotas = debt.installments.coerceAtLeast(1)
    val shownAmount = if (settled) debt.amount else DebtUtils.nextPayment(debt)
    val saldo = DebtUtils.remainingAmount(debt)
    val nextCuota = DebtUtils.nextInstallment(debt)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onEdit != null) Modifier.clickable { onEdit?.invoke() } else Modifier),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = appCard()),
        border = BorderStroke(1.dp, if (debt.isGoal && !settled) appPrimary() else appBorder()),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    if (!settled && debt.priority == DebtPriority.CRITICO) {
                        Badge("CRÍTICO", appDanger(), appDangerBg())
                    }
                    if (!settled && debt.isGoal) {
                        Badge("OBJETIVO", appPrimary(), appPrimaryLight())
                    }
                    if (!settled && debt.isFixedMonthly) {
                        Badge("MENSUAL", appPrimary(), appPrimaryLight())
                    }
                    Text(WeekUtils.formatDateShort(debt.createdAt), fontSize = 11.sp, color = appMuted())
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    debt.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = appText(),
                    textDecoration = if (settled) TextDecoration.LineThrough else null,
                    maxLines = 1
                )
                val meta = buildList {
                    if (debt.isFixedMonthly) {
                        add("Fija mensual")
                    } else if (totalCuotas > 1) {
                        if (settled) add("$totalCuotas de $totalCuotas cuotas pagadas")
                        else add("Cuota ${nextCuota ?: totalCuotas} de $totalCuotas")
                    }
                    if (!settled) {
                        debt.dueDate?.let {
                            add(if (overdue) "Venció ${WeekUtils.formatDueDate(it)}" else "Vence ${WeekUtils.formatDueDate(it)}")
                        }
                    }
                }.joinToString(" · ")
                if (meta.isNotEmpty()) {
                    Text(
                        meta,
                        fontSize = 11.5.sp,
                        color = if (overdue) appDanger() else appMuted(),
                        fontWeight = if (overdue) FontWeight.Medium else FontWeight.Normal,
                        maxLines = 1
                    )
                }
                if (totalCuotas > 1 && !settled) {
                    Text(
                        "Saldo ${WeekUtils.formatMoney(saldo)}",
                        fontSize = 11.5.sp,
                        color = appMuted(),
                        maxLines = 1
                    )
                }
                if (!debt.note.isNullOrBlank()) {
                    Text(debt.note, fontSize = 11.5.sp, color = appMuted(), maxLines = 1)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    WeekUtils.formatMoney(shownAmount),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (settled) appMuted() else appDanger(),
                    textDecoration = if (settled) TextDecoration.LineThrough else null
                )
                Row {
                    if (!settled) {
                        IconButton(onClick = onToggleGoal, modifier = Modifier.size(32.dp)) {
                            Icon(
                                Icons.Filled.Flag,
                                contentDescription = if (debt.isGoal) "Quitar objetivo de pago" else "Marcar como objetivo de pago",
                                tint = if (debt.isGoal) appPrimary() else appMuted(),
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                    IconButton(onClick = onAction, modifier = Modifier.size(32.dp)) {
                        Icon(
                            if (settled) Icons.Filled.Refresh else Icons.Filled.Check,
                            contentDescription = when {
                                settled -> "Reabrir"
                                debt.isFixedMonthly -> "Pagar mes"
                                totalCuotas > 1 -> "Pagar cuota"
                                else -> "Marcar como pagada"
                            },
                            tint = if (settled) appMuted() else appSuccess(),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = appMuted(), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
