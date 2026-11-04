package com.example.adelanto.tile

import android.content.Intent
import android.service.quicksettings.TileService
import com.example.adelanto.MainActivity

class QuickEntryTileService : TileService() {
    override fun onClick() {
        super.onClick()
        val intent = Intent(this, MainActivity::class.java).apply {
            action = "com.example.adelanto.ACTION_ADD"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = android.app.PendingIntent.getActivity(this, 0, intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)
        startActivityAndCollapse(pi)
    }

    override fun onStartListening() {
        super.onStartListening()
        qsTile?.label = "Adelanto"
        qsTile?.updateTile()
    }
}
