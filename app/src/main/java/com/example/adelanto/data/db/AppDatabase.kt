package com.example.adelanto.data.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import android.content.Context

class Converters {
    @TypeConverter
    fun fromOpType(v: OpType): String = v.name
    @TypeConverter
    fun toOpType(v: String): OpType = OpType.valueOf(v)
}

@Database(entities = [OperationEntity::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun operationDao(): OperationDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getInstance(ctx: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(ctx, AppDatabase::class.java, "adelanto.db").fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}
