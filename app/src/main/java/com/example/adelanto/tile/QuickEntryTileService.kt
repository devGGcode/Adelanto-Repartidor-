package com.example.adelanto.tile

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.TileService
import com.example.adelanto.QuickAddActivity

class QuickEntryTileService : TileService() {
    override fun onClick() {
        super.onClick()
        if (isLocked) {
            unlockAndRun { launchQuickAdd() }
        } else {
            launchQuickAdd()
        }
    }

    /** abre una ventana flotante para registrar la operación sin entrar a la app */
    private fun launchQuickAdd() {
        val intent = Intent(this, QuickAddActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pi = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            startActivityAndCollapse(pi)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        qsTile?.label = "Adelanto"
        qsTile?.updateTile()
    }
}
