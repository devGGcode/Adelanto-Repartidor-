package com.example.adelanto.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.adelanto.data.db.OpType
import com.example.adelanto.data.db.OperationEntity
import com.example.adelanto.data.db.PayMethod
import com.example.adelanto.data.repository.OperationRepository
import com.example.adelanto.domain.WeekUtils
import com.example.adelanto.ui.theme.appCard
import com.example.adelanto.ui.theme.appMuted
import com.example.adelanto.ui.theme.appPrimary
import com.example.adelanto.ui.theme.appPrimaryLight
import com.example.adelanto.ui.theme.appText
import kotlinx.coroutines.launch

/**
 * Formulario compacto en ventana flotante (se abre desde el tile).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddDialog(repo: OperationRepository, onDismiss: () -> Unit) {
    var opType by remember { mutableStateOf(OpType.FULL) }
    var method by remember { mutableStateOf(PayMethod.EFECTIVO) }
    var collectionMethod by remember { mutableStateOf(PayMethod.EFECTIVO) }
    var payment by remember { mutableStateOf("") }
    var collection by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var pending by remember { mutableStateOf<List<OperationEntity>>(emptyList()) }
    var linkedId by remember { mutableStateOf<Long?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(opType) {
        if (opType == OpType.COLLECTION) {
            pending = repo.getPendingPayments(WeekUtils.weekStartMillis())
            if (linkedId != null && pending.none { it.id == linkedId }) linkedId = null
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(18.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth().heightIn(max = 640.dp),
                shape = RoundedCornerShape(22.dp),
                color = appCard(),
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Nueva operación",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = appText(),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = appMuted())
                        }
                    }

                    val segColors = SegmentedButtonDefaults.colors(
                        activeContainerColor = appPrimaryLight(),
                        activeContentColor = appPrimary(),
                        inactiveContainerColor = Color.Transparent,
                        inactiveContentColor = appText()
                    )

                    Text("Tipo", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = appText())
                    Spacer(Modifier.height(6.dp))
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = opType == OpType.PAYMENT,
                            onClick = { opType = OpType.PAYMENT },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                            colors = segColors
                        ) { Text("Pago") }
                        SegmentedButton(
                            selected = opType == OpType.COLLECTION,
                            onClick = { opType = OpType.COLLECTION },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                            colors = segColors
                        ) { Text("Cobro") }
                        SegmentedButton(
                            selected = opType == OpType.FULL,
                            onClick = { opType = OpType.FULL },
                            shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                            colors = segColors
                        ) { Text("Pago+Cobro") }
                    }
                    Spacer(Modifier.height(12.dp))

                    Text(
                        if (opType == OpType.COLLECTION) "Método del cobro" else "Método del pago",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = appText()
                    )
                    Spacer(Modifier.height(6.dp))
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = method == PayMethod.EFECTIVO,
                            onClick = { method = PayMethod.EFECTIVO },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            colors = segColors
                        ) { Text("Efectivo") }
                        SegmentedButton(
                            selected = method == PayMethod.TRANSFERENCIA,
                            onClick = { method = PayMethod.TRANSFERENCIA },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            colors = segColors
                        ) { Text("Transfer.") }
                    }

                    if (opType == OpType.FULL) {
                        Spacer(Modifier.height(12.dp))
                        Text("Método del cobro", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = appText())
                        Spacer(Modifier.height(6.dp))
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            SegmentedButton(
                                selected = collectionMethod == PayMethod.EFECTIVO,
                                onClick = { collectionMethod = PayMethod.EFECTIVO },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                                colors = segColors
                            ) { Text("Efectivo") }
                            SegmentedButton(
                                selected = collectionMethod == PayMethod.TRANSFERENCIA,
                                onClick = { collectionMethod = PayMethod.TRANSFERENCIA },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                                colors = segColors
                            ) { Text("Transfer.") }
                        }
                    }

                    if (opType == OpType.COLLECTION) {
                        Spacer(Modifier.height(12.dp))
                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                            OutlinedTextField(
                                value = linkedId?.let { id ->
                                    pending.find { it.id == id }?.let {
                                        "Pago " + WeekUtils.formatMoney(it.paymentAmount ?: 0L)
                                    }
                                } ?: "Cobro independiente",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Vincular a pago pendiente") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                DropdownMenuItem(
                                    text = { Text("Cobro independiente") },
                                    onClick = { linkedId = null; expanded = false }
                                )
                                if (pending.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("No hay pagos pendientes") },
                                        onClick = {},
                                        enabled = false
                                    )
                                }
                                pending.forEach { p ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "Pago " + WeekUtils.formatMoney(p.paymentAmount ?: 0L) +
                                                    " · " + WeekUtils.formatDate(p.timestamp)
                                            )
                                        },
                                        onClick = { linkedId = p.id; expanded = false }
                                    )
                                }
                            }
                        }
                    }

                    if (opType == OpType.PAYMENT || opType == OpType.FULL) {
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = payment,
                            onValueChange = { payment = it.filter { c -> c.isDigit() } },
                            label = { Text("Monto pagado en el local") },
                            leadingIcon = { Text("$") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (opType == OpType.COLLECTION || opType == OpType.FULL) {
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = collection,
                            onValueChange = { collection = it.filter { c -> c.isDigit() } },
                            label = { Text("Monto cobrado al cliente") },
                            leadingIcon = { Text("$") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Nota (opcional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    val pay = payment.toLongOrNull() ?: 0L
                    val col = collection.toLongOrNull() ?: 0L
                    val linkedPay = linkedId?.let { id -> pending.find { it.id == id }?.paymentAmount } ?: 0L
                    val profit = when (opType) {
                        OpType.PAYMENT -> 0L
                        OpType.COLLECTION -> if (linkedId != null) col - linkedPay else col
                        OpType.FULL -> col - pay
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Ganancia (adelanto): ${WeekUtils.formatMoney(profit)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = appText()
                    )

                    if (error != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(error!!, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    }

                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancelar", color = appMuted())
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                when (opType) {
                                    OpType.PAYMENT -> {
                                        val p = payment.toLongOrNull()
                                        if (p == null || p <= 0) { error = "Ingresa monto pagado"; return@Button }
                                        scope.launch {
                                            repo.insert(
                                                OperationEntity(
                                                    type = OpType.PAYMENT,
                                                    paymentAmount = p,
                                                    collectionAmount = null,
                                                    profit = 0,
                                                    status = "pending",
                                                    note = note.ifBlank { null },
                                                    timestamp = System.currentTimeMillis(),
                                                    weekStart = WeekUtils.weekStartMillis(),
                                                    method = method,
                                                    collectionMethod = method
                                                )
                                            )
                                            onDismiss()
                                        }
                                    }
                                    OpType.COLLECTION -> {
                                        val c = collection.toLongOrNull()
                                        if (c == null || c <= 0) { error = "Ingresa monto cobrado"; return@Button }
                                        scope.launch {
                                            val linked = linkedId?.let { id -> pending.find { it.id == id } }
                                            if (linked != null) {
                                                repo.update(
                                                    linked.copy(
                                                        type = OpType.FULL,
                                                        collectionAmount = c,
                                                        profit = c - (linked.paymentAmount ?: 0L),
                                                        status = "resolved",
                                                        note = if (note.isNotBlank()) (linked.note?.let { "$it · " } ?: "") + note else linked.note,
                                                        collectionMethod = method
                                                    )
                                                )
                                            } else {
                                                repo.insert(
                                                    OperationEntity(
                                                        type = OpType.COLLECTION,
                                                        paymentAmount = null,
                                                        collectionAmount = c,
                                                        profit = c,
                                                        status = "standalone",
                                                        note = note.ifBlank { null },
                                                        timestamp = System.currentTimeMillis(),
                                                        weekStart = WeekUtils.weekStartMillis(),
                                                        method = method,
                                                        collectionMethod = method
                                                    )
                                                )
                                            }
                                            onDismiss()
                                        }
                                    }
                                    OpType.FULL -> {
                                        val p = payment.toLongOrNull()
                                        val c = collection.toLongOrNull()
                                        if (p == null || p <= 0) { error = "Ingresa monto pagado"; return@Button }
                                        if (c == null || c <= 0) { error = "Ingresa monto cobrado"; return@Button }
                                        scope.launch {
                                            repo.insert(
                                                OperationEntity(
                                                    type = OpType.FULL,
                                                    paymentAmount = p,
                                                    collectionAmount = c,
                                                    profit = c - p,
                                                    status = "resolved",
                                                    note = note.ifBlank { null },
                                                    timestamp = System.currentTimeMillis(),
                                                    weekStart = WeekUtils.weekStartMillis(),
                                                    method = method,
                                                    collectionMethod = collectionMethod
                                                )
                                            )
                                            onDismiss()
                                        }
                                    }
                                }
                            },
                            shape = RoundedCornerShape(50)
                        ) { Text("Guardar") }
                    }

                    Spacer(Modifier.height(6.dp))
                }
            }
        }
    }
}
