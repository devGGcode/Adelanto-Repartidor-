package com.example.adelanto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.remember
import com.example.adelanto.data.repository.OperationRepository
import com.example.adelanto.ui.screens.QuickAddDialog
import com.example.adelanto.ui.theme.AdelantoTheme

/**
 * Ventana flotante que abre el tile de acceso rápido,
 * para registrar una operación sin entrar a la app completa.
 */
class QuickAddActivity : ComponentActivity() {
    private val repo: OperationRepository by lazy {
        (application as AdelantoApplication).repository
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("adelanto", MODE_PRIVATE)
        setContent {
            val systemDark = isSystemInDarkTheme()
            val darkTheme = remember { prefs.getBoolean("dark_mode", systemDark) }
            AdelantoTheme(darkTheme = darkTheme) {
                QuickAddDialog(repo = repo, onDismiss = { finish() })
            }
        }
    }
}
