package com.example.adelanto.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.adelanto.data.db.DebtEntity
import com.example.adelanto.data.db.DebtPriority
import com.example.adelanto.data.db.DebtType
import com.example.adelanto.data.repository.DebtRepository
import com.example.adelanto.domain.WeekUtils
import com.example.adelanto.ui.theme.appBg
import com.example.adelanto.ui.theme.appDanger
import com.example.adelanto.ui.theme.appDangerBg
import com.example.adelanto.ui.theme.appMuted
import com.example.adelanto.ui.theme.appPrimary
import com.example.adelanto.ui.theme.appPrimaryLight
import com.example.adelanto.ui.theme.appText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDebtScreen(debtRepo: DebtRepository, nav: NavController) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var installments by remember { mutableStateOf("1") }
    var isFixedMonthly by remember { mutableStateOf(false) }
    var dueDate by remember { mutableStateOf<Long?>(null) }
    var priority by remember { mutableStateOf(DebtPriority.NORMAL) }
    var showDatePicker by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val amountValue = amount.toLongOrNull() ?: 0L
    val cuotas = installments.toIntOrNull() ?: 1

    Scaffold(
        containerColor = appBg(),
        topBar = {
            TopAppBar(
                title = { Text("Nueva deuda por pagar", color = appText()) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("¿A quién le debes?") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { c -> c.isDigit() } },
                label = { Text("Valor de cada cuota") },
                leadingIcon = { Text("$") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Deuda fija todos los meses",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = appText()
                    )
                    Text(
                        "Arriendo, luz, etc. Se repite cada mes, sin fin de cuotas.",
                        fontSize = 11.5.sp,
                        color = appMuted()
                    )
                }
                Switch(checked = isFixedMonthly, onCheckedChange = { isFixedMonthly = it })
            }
            Spacer(Modifier.height(8.dp))

            if (!isFixedMonthly) {
                OutlinedTextField(
                    value = installments,
                    onValueChange = { installments = it.filter { c -> c.isDigit() }.take(2) },
                    label = { Text("Cantidad de cuotas") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                if (amountValue > 0 && cuotas > 1) {
                    Text(
                        "${cuotas} cuotas de ${WeekUtils.formatMoney(amountValue)} · Total ${WeekUtils.formatMoney(amountValue * cuotas)}",
                        fontSize = 12.sp,
                        color = appMuted(),
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }
            } else {
                Text(
                    "Se paga una vez al mes y vuelve a aparecer el mes siguiente.",
                    fontSize = 12.sp,
                    color = appMuted(),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(4.dp)
            ) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = null)
                Spacer(Modifier.width(10.dp))
                Text(
                    dueDate?.let { "Vence: ${WeekUtils.formatDueDate(it)}" }
                        ?: "Fecha de vencimiento",
                    color = if (dueDate == null) appMuted() else appText()
                )
            }
            Spacer(Modifier.height(16.dp))

            Text("Estado", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = appText())
            Spacer(Modifier.height(8.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = priority == DebtPriority.NORMAL,
                    onClick = { priority = DebtPriority.NORMAL },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = appPrimaryLight(),
                        activeContentColor = appPrimary(),
                        inactiveContentColor = appText()
                    )
                ) { Text("Normal") }
                SegmentedButton(
                    selected = priority == DebtPriority.CRITICO,
                    onClick = { priority = DebtPriority.CRITICO },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = appDangerBg(),
                        activeContentColor = appDanger(),
                        inactiveContentColor = appText()
                    )
                ) { Text("Crítico") }
            }
            if (priority == DebtPriority.CRITICO) {
                Text(
                    "Las deudas críticas aparecen primero y tienen prioridad para pagar.",
                    fontSize = 12.sp,
                    color = appDanger(),
                    modifier = Modifier.padding(start = 4.dp, top = 6.dp)
                )
            }
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Nota (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )

            if (error != null) {
                Spacer(Modifier.height(12.dp))
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    val a = amount.toLongOrNull()
                    val c = if (isFixedMonthly) 1 else (installments.toIntOrNull() ?: 1)
                    if (name.isBlank()) { error = "Escribe el nombre"; return@Button }
                    if (a == null || a <= 0) { error = "Ingresa un monto válido"; return@Button }
                    if (!isFixedMonthly && c < 1) { error = "Las cuotas deben ser 1 o más"; return@Button }
                    scope.launch {
                        val total = if (!isFixedMonthly && c > 1) a * c else a
                        debtRepo.insert(
                            DebtEntity(
                                type = DebtType.PAGAR,
                                name = name.trim(),
                                amount = total,
                                note = note.ifBlank { null },
                                createdAt = System.currentTimeMillis(),
                                status = "pendiente",
                                dueDate = dueDate,
                                installments = c,
                                installmentAmount = a,
                                isFixedMonthly = isFixedMonthly,
                                priority = priority
                            )
                        )
                        nav.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar") }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { dueDate = WeekUtils.pickerDateToLocalStart(it) }
                    showDatePicker = false
                }) { Text("Aceptar", color = appPrimary(), fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar", color = appMuted()) }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}
