package com.example.adelanto.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.adelanto.data.backup.BackupManager
import com.example.adelanto.data.db.AppDatabase
import com.example.adelanto.data.repository.BackupRepository
import com.example.adelanto.ui.theme.appBg
import com.example.adelanto.ui.theme.appBorder
import com.example.adelanto.ui.theme.appCard
import com.example.adelanto.ui.theme.appDanger
import com.example.adelanto.ui.theme.appMuted
import com.example.adelanto.ui.theme.appPrimary
import com.example.adelanto.ui.theme.appText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(nav: NavController) {
    val ctx = LocalContext.current
    val backupRepo = remember { BackupRepository(AppDatabase.getInstance(ctx)) }
    val scope = rememberCoroutineScope()
    var pendingImport by remember { mutableStateOf<Uri?>(null) }

    fun toast(message: String) = Toast.makeText(ctx, message, Toast.LENGTH_LONG).show()

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val json = withContext(Dispatchers.IO) {
                        BackupManager.build(
                            ctx,
                            backupRepo.allOperations(),
                            backupRepo.allDebts(),
                            backupRepo.allGoals()
                        )
                    }
                    withContext(Dispatchers.IO) {
                        ctx.contentResolver.openOutputStream(uri)?.use { out ->
                            out.write(json.toByteArray(Charsets.UTF_8))
                        } ?: error("No se pudo abrir el archivo")
                    }
                    toast("Copia exportada correctamente")
                } catch (e: Exception) {
                    toast("Error al exportar: ${e.message}")
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) pendingImport = uri
    }

    fun doImport(uri: Uri) {
        scope.launch {
            try {
                val data = withContext(Dispatchers.IO) {
                    val text = ctx.contentResolver.openInputStream(uri)
                        ?.bufferedReader()
                        ?.use { it.readText() }
                        ?: error("No se pudo leer el archivo")
                    BackupManager.parse(text)
                }
                val goalsWithPhotos = withContext(Dispatchers.IO) {
                    data.goals.map { goal ->
                        val bytes = data.goalPhotos[goal.id]
                        if (bytes != null) {
                            goal.copy(photoPath = BackupManager.writeGoalPhoto(ctx, goal.id, bytes))
                        } else {
                            goal.copy(photoPath = null)
                        }
                    }
                }
                backupRepo.restore(data.copy(goals = goalsWithPhotos))
                BackupManager.restoreCash(ctx, data.cash)
                toast(
                    "Copia importada: ${data.operations.size} operaciones, " +
                        "${data.debts.size} deudas y ${data.goals.size} metas"
                )
            } catch (e: Exception) {
                toast("Error al importar: ${e.message}")
            } finally {
                pendingImport = null
            }
        }
    }

    Scaffold(
        containerColor = appBg(),
        topBar = {
            TopAppBar(
                title = { Text("Copia de seguridad", color = appText()) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = appCard()),
                border = BorderStroke(1.dp, appBorder()),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Filled.UploadFile, contentDescription = null, tint = appPrimary(), modifier = Modifier.size(26.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Exportar copia", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = appText())
                    Text(
                        "Guarda un archivo con todas tus operaciones, deudas, cuotas y la caja de cada semana.",
                        fontSize = 12.5.sp,
                        color = appMuted()
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val name = "adelanto-" +
                                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) +
                                ".json"
                            exportLauncher.launch(name)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50)
                    ) { Text("Exportar copia de seguridad") }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = appCard()),
                border = BorderStroke(1.dp, appBorder()),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Filled.Download, contentDescription = null, tint = appDanger(), modifier = Modifier.size(26.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Importar copia", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = appText())
                    Text(
                        "Reemplaza todos los datos actuales por los de la copia. Úsalo para recuperar en otro teléfono o después de una instalación.",
                        fontSize = 12.5.sp,
                        color = appMuted()
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50)
                    ) { Text("Importar copia de seguridad", color = appDanger()) }
                }
            }

            Text(
                "Consejo: exporta una copia antes de cambiar de teléfono o de instalar una versión nueva.",
                fontSize = 11.5.sp,
                color = appMuted()
            )
        }
    }

    pendingImport?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingImport = null },
            title = { Text("Importar copia", fontWeight = FontWeight.Bold, color = appText()) },
            text = {
                Text(
                    "Esto reemplazará todas las operaciones, deudas y cajas actuales por las de la copia. ¿Continuar?",
                    color = appText()
                )
            },
            confirmButton = {
                TextButton(onClick = { doImport(uri) }) {
                    Text("Importar", color = appDanger(), fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingImport = null }) {
                    Text("Cancelar", color = appMuted())
                }
            },
            containerColor = appCard()
        )
    }
}
