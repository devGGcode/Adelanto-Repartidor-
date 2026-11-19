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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.adelanto.data.repository.OperationRepository
import com.example.adelanto.data.repository.DebtRepository
import com.example.adelanto.data.repository.GoalRepository
import com.example.adelanto.ui.screens.AddOperationScreen
import com.example.adelanto.ui.screens.AddDebtScreen
import com.example.adelanto.ui.screens.BackupScreen
import com.example.adelanto.ui.screens.DebtsScreen
import com.example.adelanto.ui.screens.GoalFormScreen
import com.example.adelanto.ui.screens.GoalsScreen
import com.example.adelanto.ui.screens.HomeScreen
import com.example.adelanto.ui.screens.HistoryScreen
import com.example.adelanto.ui.screens.DayDetailScreen
import com.example.adelanto.ui.screens.EditDebtScreen
import com.example.adelanto.ui.screens.WeekSummaryScreen
import com.example.adelanto.ui.theme.AdelantoTheme

class MainActivity : ComponentActivity() {
    private val repo: OperationRepository by lazy {
        (application as AdelantoApplication).repository
    }
    private val debtRepo: DebtRepository by lazy {
        (application as AdelantoApplication).debtRepository
    }
    private val goalRepo: GoalRepository by lazy {
        (application as AdelantoApplication).goalRepository
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val openAdd = intent?.action == "com.example.adelanto.ACTION_ADD"
        val prefs = getSharedPreferences("adelanto", MODE_PRIVATE)
        setContent {
            val systemDark = isSystemInDarkTheme()
            var darkTheme by remember { mutableStateOf(prefs.getBoolean("dark_mode", systemDark)) }
            AdelantoTheme(darkTheme = darkTheme) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNav(
                        repo = repo,
                        debtRepo = debtRepo,
                        goalRepo = goalRepo,
                        openAddInitially = openAdd,
                        onToggleTheme = {
                            darkTheme = !darkTheme
                            prefs.edit().putBoolean("dark_mode", darkTheme).apply()
                        }
                    )
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
fun AppNav(
    repo: OperationRepository,
    debtRepo: DebtRepository,
    goalRepo: GoalRepository,
    openAddInitially: Boolean,
    onToggleTheme: () -> Unit
) {
    val nav = rememberNavController()
    var handled by remember { mutableStateOf(false) }
    LaunchedEffect(openAddInitially) {
        if (openAddInitially && !handled) {
            nav.navigate("add")
            handled = true
        }
    }
    NavHost(navController = nav, startDestination = "home") {
        composable("home") { HomeScreen(repo, debtRepo, goalRepo, nav, onToggleTheme) }
        composable("add") { AddOperationScreen(repo, nav) }
        composable("debts") { DebtsScreen(debtRepo, nav) }
        composable("addDebt") { AddDebtScreen(debtRepo, nav) }
        composable("history") { HistoryScreen(repo, nav) }
        composable(
            "dayDetail/{dayStart}",
            arguments = listOf(navArgument("dayStart") { type = NavType.LongType })
        ) { back ->
            val ds = back.arguments?.getLong("dayStart") ?: 0L
            DayDetailScreen(repo, ds, nav)
        }
        composable(
            "weekSummary/{weekStart}",
            arguments = listOf(navArgument("weekStart") { type = NavType.LongType })
        ) { back ->
            val ws = back.arguments?.getLong("weekStart") ?: 0L
            WeekSummaryScreen(repo, ws, nav)
        }
        composable(
            "editDebt/{debtId}",
            arguments = listOf(navArgument("debtId") { type = NavType.LongType })
        ) { back ->
            val id = back.arguments?.getLong("debtId") ?: 0L
            EditDebtScreen(debtRepo, id, nav)
        }
        composable(
            "editOperation/{opId}",
            arguments = listOf(navArgument("opId") { type = NavType.LongType })
        ) { back ->
            val id = back.arguments?.getLong("opId") ?: 0L
            AddOperationScreen(repo, nav, id)
        }
        composable("backup") { BackupScreen(nav) }
        composable("goals") { GoalsScreen(goalRepo, nav) }
        composable("addGoal") { GoalFormScreen(goalRepo, null, nav) }
        composable(
            "editGoal/{goalId}",
            arguments = listOf(navArgument("goalId") { type = NavType.LongType })
        ) { back ->
            val id = back.arguments?.getLong("goalId") ?: 0L
            GoalFormScreen(goalRepo, id, nav)
        }
    }
}
