package com.example.adelanto.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember // Corregido: Agregada la importación faltante de remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.adelanto.data.repository.OperationRepository
import com.example.adelanto.domain.WeekUtils
import com.example.adelanto.ui.theme.appBg // Importamos el fondo de tu tema

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekDetailScreen(repo: OperationRepository, weekStart: Long, nav: NavController) {
    val flow = remember { repo.observeByWeek(weekStart) }
    val ops by flow.collectAsState(initial = emptyList())

    Scaffold(
        containerColor = appBg(), // Corregido: Agregamos el fondo personalizado
        topBar = {
            TopAppBar(
                title = { Text(WeekUtils.formatWeekRange(weekStart), color = com.example.adelanto.ui.theme.appText()) },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = com.example.adelanto.ui.theme.appMuted())
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(containerColor = appBg())
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                "Total adelanto: ${WeekUtils.formatMoney(ops.sumOf { it.profit })}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = com.example.adelanto.ui.theme.appText()
            )
            Spacer(Modifier.height(16.dp))
            LazyColumn(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                items(ops, key = { it.id }) { op ->
                    // Corregido: Cambiado OperationCard por OpCard que es el componente real de tu app
                    OpCard(op, onDelete = {})
                }
            }
        }
    }
}

