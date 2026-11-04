package com.example.adelanto

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.adelanto.data.repository.OperationRepository
import com.example.adelanto.ui.screens.AddOperationScreen
import com.example.adelanto.ui.screens.HomeScreen
import com.example.adelanto.ui.screens.HistoryScreen
import com.example.adelanto.ui.screens.DayDetailScreen
import com.example.adelanto.ui.theme.AdelantoTheme

class MainActivity : ComponentActivity() {
    private val repo: OperationRepository by lazy {
        (application as AdelantoApplication).repository
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val openAdd = intent?.action == "com.example.adelanto.ACTION_ADD"
        setContent {
            AdelantoTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNav(repo, openAdd)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        recreate()
    }
}

@Composable
fun AppNav(repo: OperationRepository, openAddInitially: Boolean) {
    val nav = rememberNavController()
    var handled by remember { mutableStateOf(false) }
    LaunchedEffect(openAddInitially) {
        if (openAddInitially && !handled) {
            nav.navigate("add")
            handled = true
        }
    }
    NavHost(navController = nav, startDestination = "home") {
        composable("home") { HomeScreen(repo, nav) }
        composable("add") { AddOperationScreen(repo, nav) }
        composable("history") { HistoryScreen(repo, nav) }
        composable(
            "dayDetail/{dayStart}",
            arguments = listOf(navArgument("dayStart") { type = NavType.LongType })
        ) { back ->
            val ds = back.arguments?.getLong("dayStart") ?: 0L
            DayDetailScreen(repo, ds, nav)
        }
    }
}
