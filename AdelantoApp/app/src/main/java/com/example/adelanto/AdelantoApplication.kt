package com.example.adelanto

import android.app.Application
import com.example.adelanto.data.db.AppDatabase
import com.example.adelanto.data.repository.OperationRepository

class AdelantoApplication : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy { OperationRepository(database.operationDao()) }
}
