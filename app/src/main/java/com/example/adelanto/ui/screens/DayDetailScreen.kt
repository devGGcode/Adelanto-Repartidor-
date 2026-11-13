package com.example.adelanto.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.adelanto.data.repository.OperationRepository
import com.example.adelanto.domain.WeekUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailScreen(repo: OperationRepository, dayStart: Long, nav: NavController) {
    val flow = androidx.compose.runtime.remember { repo.observeAll() }
    val all by flow.collectAsState(initial = emptyList())
    val ops = all.filter { WeekUtils.dayStartMillis(it.timestamp) == dayStart }.sortedByDescending { it.timestamp }
    val total = ops.sumOf { it.profit }

    Scaffold(
        containerColor = com.example.adelanto.ui.theme.appBg(),
        topBar = {
            TopAppBar(
                title = { Text(WeekUtils.formatDay(dayStart), color = com.example.adelanto.ui.theme.appText()) },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(containerColor = com.example.adelanto.ui.theme.appBg()),
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
                .padding(16.dp)
        ) {
            Text(
                "Total adelanto: ${WeekUtils.formatMoney(total)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ops, key = { it.id }) { op ->
                    OpCard(op, onDelete = {})
                }
            }
        }
    }
}
