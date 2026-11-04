package com.example.adelanto.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.adelanto.data.db.OpType
import com.example.adelanto.data.db.OperationEntity
import com.example.adelanto.data.repository.OperationRepository
import com.example.adelanto.domain.WeekUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOperationScreen(repo: OperationRepository, nav: NavController) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    var opType by remember { mutableStateOf(OpType.FULL) }
    var payment by remember { mutableStateOf("") }
    var collection by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var pending by remember { mutableStateOf<List<OperationEntity>>(emptyList()) }
    var linkedId by remember { mutableStateOf<Long?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // load pending payments when COLLECTION
    androidx.compose.runtime.LaunchedEffect(opType) {
        if (opType == OpType.COLLECTION) {
            pending = repo.getPendingPayments(WeekUtils.weekStartMillis())
        }
    }

    Scaffold(
        containerColor = com.example.adelanto.ui.theme.appBg(),
        topBar = { TopAppBar(title = { Text("Nueva operación", color = com.example.adelanto.ui.theme.appText()) }, colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(containerColor = com.example.adelanto.ui.theme.appBg())) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Tipo de operación", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = opType == OpType.PAYMENT,
                    onClick = { opType = OpType.PAYMENT },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                ) { Text("Pago") }
                SegmentedButton(
                    selected = opType == OpType.COLLECTION,
                    onClick = { opType = OpType.COLLECTION },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                ) { Text("Cobro") }
                SegmentedButton(
                    selected = opType == OpType.FULL,
                    onClick = { opType = OpType.FULL },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                ) { Text("Pago+Cobro") }
            }
            Spacer(Modifier.height(16.dp))

            if (opType == OpType.COLLECTION) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = linkedId?.let { id -> pending.find { it.id == id }?.let { "Pago $" + it.paymentAmount } } ?: "Cobro independiente",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Vincular a pago pendiente") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Cobro independiente (sin vincular)") },
                            onClick = { linkedId = null; expanded = false }
                        )
                        pending.forEach { p ->
                            DropdownMenuItem(
                                text = { Text("Pago $" + (p.paymentAmount ?: 0) + " - " + WeekUtils.formatDate(p.timestamp)) },
                                onClick = { linkedId = p.id; expanded = false }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            if (opType == OpType.PAYMENT || opType == OpType.FULL) {
                OutlinedTextField(
                    value = payment,
                    onValueChange = { payment = it.filter { c -> c.isDigit() } },
                    label = { Text("Monto pagado en el local") },
                    prefix = { Text("$") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = opType != OpType.COLLECTION || linkedId == null
                )
                Spacer(Modifier.height(12.dp))
            }

            if (opType == OpType.COLLECTION || opType == OpType.FULL) {
                OutlinedTextField(
                    value = collection,
                    onValueChange = { collection = it.filter { c -> c.isDigit() } },
                    label = { Text("Monto cobrado al cliente") },
                    prefix = { Text("$") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Nota (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            val pay = payment.toLongOrNull() ?: 0L
            val col = collection.toLongOrNull() ?: 0L
            val linkedPay = linkedId?.let { id -> pending.find { it.id == id }?.paymentAmount } ?: 0L
            val profit = when (opType) {
                OpType.PAYMENT -> 0L
                OpType.COLLECTION -> if (linkedId != null) col - linkedPay else col
                OpType.FULL -> col - pay
            }
            Text("Ganancia (adelanto): ${WeekUtils.formatMoney(profit)}", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)

            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    when (opType) {
                        OpType.PAYMENT -> {
                            val p = payment.toLongOrNull()
                            if (p == null || p < 0) { error = "Ingresa monto pagado"; return@Button }
                            scope.launch {
                                repo.insert(OperationEntity(
                                    type = OpType.PAYMENT,
                                    paymentAmount = p,
                                    collectionAmount = null,
                                    profit = 0,
                                    status = "pending",
                                    note = note.ifBlank { null },
                                    timestamp = System.currentTimeMillis(),
                                    weekStart = WeekUtils.weekStartMillis()
                                ))
                                nav.popBackStack()
                            }
                        }
                        OpType.COLLECTION -> {
                            val c = collection.toLongOrNull()
                            if (c == null || c <= 0) { error = "Ingresa monto cobrado"; return@Button }
                            scope.launch {
                                val linked = linkedId?.let { id -> pending.find { it.id == id } }
                                if (linked != null) {
                                    linked.type = OpType.FULL
                                    linked.collectionAmount = c
                                    linked.profit = c - (linked.paymentAmount ?: 0L)
                                    linked.status = "resolved"
                                    if (note.isNotBlank()) linked.note = (linked.note?.let { "$it · " } ?: "") + note
                                    repo.update(linked)
                                } else {
                                    repo.insert(OperationEntity(
                                        type = OpType.COLLECTION,
                                        paymentAmount = null,
                                        collectionAmount = c,
                                        profit = c,
                                        status = "standalone",
                                        note = note.ifBlank { null },
                                        timestamp = System.currentTimeMillis(),
                                        weekStart = WeekUtils.weekStartMillis()
                                    ))
                                }
                                nav.popBackStack()
                            }
                        }
                        OpType.FULL -> {
                            val p = payment.toLongOrNull()
                            val c = collection.toLongOrNull()
                            if (p == null || p < 0) { error = "Ingresa monto pagado"; return@Button }
                            if (c == null || c <= 0) { error = "Ingresa monto cobrado"; return@Button }
                            scope.launch {
                                repo.insert(OperationEntity(
                                    type = OpType.FULL,
                                    paymentAmount = p,
                                    collectionAmount = c,
                                    profit = c - p,
                                    status = "resolved",
                                    note = note.ifBlank { null },
                                    timestamp = System.currentTimeMillis(),
                                    weekStart = WeekUtils.weekStartMillis()
                                ))
                                nav.popBackStack()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar") }
        }
    }
}
