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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.adelanto.data.db.GoalEntity
import com.example.adelanto.data.repository.GoalRepository
import com.example.adelanto.domain.GoalUtils
import com.example.adelanto.domain.WeekUtils
import com.example.adelanto.ui.components.GoalPhoto
import com.example.adelanto.ui.theme.appBg
import com.example.adelanto.ui.theme.appBorder
import com.example.adelanto.ui.theme.appCard
import com.example.adelanto.ui.theme.appMuted
import com.example.adelanto.ui.theme.appPrimary
import com.example.adelanto.ui.theme.appPrimaryLight
import com.example.adelanto.ui.theme.appSuccess
import com.example.adelanto.ui.theme.appText
import com.example.adelanto.ui.viewmodel.GoalsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(goalRepo: GoalRepository, nav: NavController) {
    val vm: GoalsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = GoalsViewModel.factory(goalRepo))
    val state by vm.uiState.collectAsState()

    var savingGoal by remember { mutableStateOf<GoalEntity?>(null) }
    var savingInput by remember { mutableStateOf("") }
    var savingError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = appBg(),
        topBar = {
            TopAppBar(
                title = { Text("Metas de ahorro", color = appText()) },
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
                onClick = { nav.navigate("addGoal") },
                containerColor = appPrimary(),
                contentColor = Color.White,
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Nueva meta")
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
            item { GoalsSummary(state.totalSaved, state.totalTarget, state.pending.size) }

            if (state.pending.isNotEmpty()) {
                item { SectionTitle("En camino", appPrimary()) }
                items(state.pending, key = { "p_${it.id}" }) { goal ->
                    GoalCard(
                        goal = goal,
                        onEdit = { nav.navigate("editGoal/${goal.id}") },
                        onAddSaving = {
                            savingGoal = goal
                            savingInput = ""
                            savingError = null
                        },
                        onPurchased = { vm.markPurchased(goal) }
                    )
                }
            }

            if (state.pending.isEmpty() && state.completed.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(40.dp, 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Filled.Inbox, contentDescription = null, tint = appMuted(), modifier = Modifier.size(40.dp))
                        Text("Aún no tienes metas", fontSize = 14.sp, color = appMuted())
                        Text("Toca + para crear una", fontSize = 12.sp, color = appMuted())
                    }
                }
            }

            if (state.completed.isNotEmpty()) {
                item { SectionTitle("Compradas", appMuted()) }
                items(state.completed, key = { "c_${it.id}" }) { goal ->
                    GoalCard(
                        goal = goal,
                        onEdit = { nav.navigate("editGoal/${goal.id}") },
                        onAddSaving = null,
                        onPurchased = null,
                        onReopen = { vm.reopen(goal) }
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    savingGoal?.let { goal ->
        AlertDialog(
            onDismissRequest = { savingGoal = null },
            title = { Text("Abonar a ${goal.name}", fontWeight = FontWeight.Bold, color = appText()) },
            text = {
                Column {
                    Text(
                        "Ahorrado ${WeekUtils.formatMoney(goal.savedAmount)} de ${WeekUtils.formatMoney(goal.targetAmount)}",
                        fontSize = 12.sp,
                        color = appMuted()
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = savingInput,
                        onValueChange = { savingInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Monto a abonar") },
                        leadingIcon = { Text("$") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        )
                    )
                    if (savingError != null) {
                        Spacer(Modifier.height(6.dp))
                        Text(savingError!!, color = androidx.compose.ui.graphics.Color.Red, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val amount = savingInput.toLongOrNull()
                    if (amount == null || amount <= 0) {
                        savingError = "Ingresa un monto válido"
                    } else {
                        vm.addSaving(goal, amount)
                        savingGoal = null
                    }
                }) { Text("Abonar", color = appPrimary(), fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { savingGoal = null }) { Text("Cancelar", color = appMuted()) }
            },
            containerColor = appCard()
        )
    }
}

@Composable
private fun GoalsSummary(saved: Long, target: Long, count: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = appCard()),
        border = BorderStroke(1.dp, appBorder()),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(appPrimaryLight()),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Savings, contentDescription = null, tint = appPrimary(), modifier = Modifier.size(20.dp))
                }
                Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                    Text("Ahorro para metas", fontSize = 12.sp, color = appMuted(), fontWeight = FontWeight.Medium)
                    Text(
                        "${WeekUtils.formatMoney(saved)} de ${WeekUtils.formatMoney(target)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = appText(),
                        letterSpacing = (-0.3).sp
                    )
                }
            }
            if (target > 0) {
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = (saved.toFloat() / target).coerceIn(0f, 1f),
                    modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(4.dp)),
                    color = appPrimary(),
                    trackColor = appBorder()
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                if (count == 1) "1 meta en camino" else "$count metas en camino",
                fontSize = 11.5.sp,
                color = appMuted()
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String, color: Color) {
    Text(text, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color, modifier = Modifier.padding(top = 8.dp, bottom = 2.dp))
}

@Composable
private fun GoalCard(
    goal: GoalEntity,
    onEdit: () -> Unit,
    onAddSaving: (() -> Unit)?,
    onPurchased: (() -> Unit)?,
    onReopen: (() -> Unit)? = null
) {
    val reached = GoalUtils.targetReached(goal)
    val perPeriod = GoalUtils.perPeriod(goal)
    val periods = GoalUtils.periodsLeft(goal)
    val freq = GoalUtils.frequencyName(goal)

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEdit() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = appCard()),
        border = BorderStroke(1.dp, if (goal.completed) appBorder() else appPrimary()),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GoalPhoto(path = goal.photoPath, modifier = Modifier.size(64.dp))
                Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            goal.name,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (goal.completed) appMuted() else appText(),
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        if (goal.completed) {
                            Badge("COMPRADA", appMuted(), appBorder().copy(alpha = 0.35f))
                        } else if (reached) {
                            Badge("LISTA", appSuccess(), appSuccess().copy(alpha = 0.15f))
                        }
                    }
                    Text(
                        "Ahorrado ${WeekUtils.formatMoney(goal.savedAmount)} de ${WeekUtils.formatMoney(goal.targetAmount)}",
                        fontSize = 11.5.sp,
                        color = appMuted(),
                        maxLines = 1
                    )
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = GoalUtils.progress(goal),
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = if (goal.completed) appMuted() else if (reached) appSuccess() else appPrimary(),
                        trackColor = appBorder()
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        when {
                            goal.completed -> "Comprada"
                            reached -> "Ya puedes comprarla 🎉"
                            perPeriod != null && periods != null -> {
                                val falta = if (periods == 1) {
                                    "falta 1 $freq"
                                } else {
                                    "faltan $periods ${GoalUtils.frequencyPlural(goal)}"
                                }
                                "Ahorra ${WeekUtils.formatMoney(perPeriod)} por $freq · $falta"
                            }
                            else -> "Sin fecha límite: elige una fecha para calcular el ahorro"
                        },
                        fontSize = 11.sp,
                        color = if (reached && !goal.completed) appSuccess() else appMuted(),
                        maxLines = 2
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    if (!goal.completed && !reached && onAddSaving != null) {
                        IconButton(onClick = onAddSaving, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.Add, contentDescription = "Abonar", tint = appPrimary(), modifier = Modifier.size(18.dp))
                        }
                    }
                    if (!goal.completed && reached && onPurchased != null) {
                        IconButton(onClick = onPurchased, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.Check, contentDescription = "Marcar como comprada", tint = appSuccess(), modifier = Modifier.size(18.dp))
                        }
                    }
                    if (goal.completed && onReopen != null) {
                        IconButton(onClick = onReopen, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Volver a activar", tint = appMuted(), modifier = Modifier.size(17.dp))
                        }
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar meta", tint = appMuted(), modifier = Modifier.size(16.dp))
                    }
                }
            }
            if (!goal.note.isNullOrBlank()) {
                Text(
                    goal.note,
                    fontSize = 11.5.sp,
                    color = appMuted(),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun Badge(text: String, color: Color, bg: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(text, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}
