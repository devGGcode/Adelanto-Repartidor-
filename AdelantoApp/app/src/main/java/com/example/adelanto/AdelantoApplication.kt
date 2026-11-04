package com.example.adelanto

import android.app.Application

class AdelantoApplication : Application() {
    val database by lazy { data.db.AppDatabase.getInstance(this) }
    val repository by lazy { data.repository.OperationRepository(database.operationDao()) }
}
