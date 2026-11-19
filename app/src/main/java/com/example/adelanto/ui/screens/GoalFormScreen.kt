package com.example.adelanto.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.adelanto.data.db.GoalEntity
import com.example.adelanto.data.db.GoalFrequency
import com.example.adelanto.data.repository.GoalRepository
import com.example.adelanto.domain.GoalUtils
import com.example.adelanto.domain.WeekUtils
import com.example.adelanto.ui.components.GoalPhoto
import com.example.adelanto.ui.theme.appBg
import com.example.adelanto.ui.theme.appDanger
import com.example.adelanto.ui.theme.appMuted
import com.example.adelanto.ui.theme.appPrimary
import com.example.adelanto.ui.theme.appPrimaryLight
import com.example.adelanto.ui.theme.appText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalFormScreen(goalRepo: GoalRepository, goalId: Long?, nav: NavController) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var original by remember { mutableStateOf<GoalEntity?>(null) }
    var name by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf(GoalFrequency.SEMANAL) }
    var dueDate by remember { mutableStateOf<Long?>(null) }
    var photoPath by remember { mutableStateOf<String?>(null) }
    var note by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    LaunchedEffect(goalId) {
        if (goalId != null) {
            val g = goalRepo.getById(goalId)
            if (g != null) {
                original = g
                name = g.name
                target = g.targetAmount.toString()
                saved = g.savedAmount.toString()
                frequency = g.frequency
                dueDate = g.targetDate
                photoPath = g.photoPath
                note = g.note ?: ""
            }
        }
    }

    val photoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val path = withContext(Dispatchers.IO) { copyPhoto(ctx, uri) }
                if (path != null) photoPath = path
            }
        }
    }

    val targetValue = target.toLongOrNull() ?: 0L
    val savedValue = (saved.toLongOrNull() ?: 0L).coerceAtLeast(0L)
    val previewGoal = GoalEntity(
        name = name,
        targetAmount = targetValue,
        savedAmount = savedValue,
        frequency = frequency,
        targetDate = dueDate,
        createdAt = 0L
    )
    val perPeriod = GoalUtils.perPeriod(previewGoal)
    val periods = GoalUtils.periodsLeft(previewGoal)

    Scaffold(
        containerColor = appBg(),
        topBar = {
            TopAppBar(
                title = { Text(if (goalId == null) "Nueva meta" else "Editar meta", color = appText()) },
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
            GoalPhoto(
                path = photoPath,
                modifier = Modifier.fillMaxWidth().height(180.dp),
                iconSize = 48.dp
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { photoLauncher.launch("image/*") },
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(Icons.Filled.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (photoPath == null) "Agregar foto" else "Cambiar foto")
                }
                if (photoPath != null) {
                    TextButton(onClick = { photoPath = null }) {
                        Text("Quitar", color = appMuted())
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("¿Qué quieres comprar?") },
                placeholder = { Text("Ej. PlayStation 5") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = target,
                onValueChange = { target = it.filter { c -> c.isDigit() } },
                label = { Text("Precio / monto objetivo") },
                leadingIcon = { Text("$") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = saved,
                onValueChange = { saved = it.filter { c -> c.isDigit() } },
                label = { Text("Ya tengo ahorrado (opcional)") },
                leadingIcon = { Text("$") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            Text("Frecuencia de ahorro", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = appText())
            Spacer(Modifier.height(8.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = frequency == GoalFrequency.SEMANAL,
                    onClick = { frequency = GoalFrequency.SEMANAL },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = appPrimaryLight(),
                        activeContentColor = appPrimary(),
                        inactiveContentColor = appText()
                    )
                ) { Text("Semanal") }
                SegmentedButton(
                    selected = frequency == GoalFrequency.MENSUAL,
                    onClick = { frequency = GoalFrequency.MENSUAL },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = appPrimaryLight(),
                        activeContentColor = appPrimary(),
                        inactiveContentColor = appText()
                    )
                ) { Text("Mensual") }
            }
            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(4.dp)
            ) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = null)
                Spacer(Modifier.width(10.dp))
                Text(
                    dueDate?.let { "Fecha objetivo: ${WeekUtils.formatDueDate(it)}" } ?: "Fecha objetivo (opcional)",
                    color = if (dueDate == null) appMuted() else appText()
                )
            }
            if (dueDate != null) {
                TextButton(onClick = { dueDate = null }) { Text("Quitar fecha", color = appMuted()) }
            }

            if (targetValue > 0) {
                val freqName = if (frequency == GoalFrequency.SEMANAL) "semana" else "mes"
                val plural = if (frequency == GoalFrequency.SEMANAL) "semanas" else "meses"
                Text(
                    if (perPeriod != null && periods != null) {
                        if (perPeriod == 0L) {
                            "Meta completada con lo ahorrado"
                        } else {
                            val falta = if (periods == 1) "falta 1 $freqName" else "faltan $periods $plural"
                            "Ahorra ${WeekUtils.formatMoney(perPeriod)} por $freqName · $falta"
                        }
                    } else {
                        "Elige una fecha objetivo para calcular cuánto ahorrar por $freqName."
                    },
                    fontSize = 12.sp,
                    color = appPrimary(),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Spacer(Modifier.height(12.dp))

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

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    if (goalId != null && original == null) {
                        error = "Espera un momento mientras se carga la meta"
                        return@Button
                    }
                    val price = target.toLongOrNull()
                    val savedAmount = (saved.toLongOrNull() ?: 0L).coerceAtLeast(0L)
                    if (name.isBlank()) { error = "Escribe qué quieres comprar"; return@Button }
                    if (price == null || price <= 0) { error = "Ingresa un precio válido"; return@Button }
                    if (savedAmount > price) { error = "Lo ahorrado no puede ser mayor al precio"; return@Button }

                    val finalPhoto = photoPath
                    val oldPhoto = original?.photoPath
                    scope.launch {
                        if (oldPhoto != null && oldPhoto != finalPhoto) deletePhoto(oldPhoto)
                        val current = original
                        if (current == null) {
                            goalRepo.insert(
                                GoalEntity(
                                    name = name.trim(),
                                    targetAmount = price,
                                    savedAmount = savedAmount,
                                    frequency = frequency,
                                    targetDate = dueDate,
                                    photoPath = finalPhoto,
                                    note = note.ifBlank { null },
                                    createdAt = System.currentTimeMillis()
                                )
                            )
                        } else {
                            val stillCompleted = current.completed && savedAmount >= price
                            goalRepo.update(
                                current.copy(
                                    name = name.trim(),
                                    targetAmount = price,
                                    savedAmount = savedAmount,
                                    frequency = frequency,
                                    targetDate = dueDate,
                                    photoPath = finalPhoto,
                                    note = note.ifBlank { null },
                                    completed = stillCompleted,
                                    completedAt = if (stillCompleted) current.completedAt else null
                                )
                            )
                        }
                        nav.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50)
            ) { Text("Guardar meta") }

            if (goalId != null && original != null) {
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = { showDelete = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Eliminar meta", color = appDanger()) }
            }

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

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Eliminar meta", fontWeight = FontWeight.Bold, color = appText()) },
            text = { Text("¿Seguro que quieres eliminar esta meta? No se puede deshacer.", color = appText()) },
            confirmButton = {
                TextButton(onClick = {
                    val current = original
                    showDelete = false
                    if (current != null) {
                        scope.launch {
                            current.photoPath?.let { deletePhoto(it) }
                            goalRepo.delete(current)
                            nav.popBackStack()
                        }
                    }
                }) { Text("Eliminar", color = appDanger(), fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) { Text("Cancelar", color = appMuted()) }
            },
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
        )
    }
}

private fun copyPhoto(context: Context, uri: Uri): String? {
    return try {
        val dir = File(context.filesDir, "goals").apply { mkdirs() }
        val file = File(dir, "goal_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        } ?: return null
        file.absolutePath
    } catch (e: Exception) {
        null
    }
}

private fun deletePhoto(path: String) {
    try { File(path).delete() } catch (e: Exception) { }
}
