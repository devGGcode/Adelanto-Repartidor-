package com.example.adelanto.data.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context

class Converters {
    @TypeConverter
    fun fromOpType(v: OpType): String = v.name
    @TypeConverter
    fun toOpType(v: String): OpType = OpType.valueOf(v)

    @TypeConverter
    fun fromPayMethod(v: PayMethod): String = v.name
    @TypeConverter
    fun toPayMethod(v: String): PayMethod =
        try { PayMethod.valueOf(v) } catch (e: Exception) { PayMethod.EFECTIVO }

    @TypeConverter
    fun fromDebtType(v: DebtType): String = v.name
    @TypeConverter
    fun toDebtType(v: String): DebtType =
        try { DebtType.valueOf(v) } catch (e: Exception) { DebtType.PAGAR }

    @TypeConverter
    fun fromDebtPriority(v: DebtPriority): String = v.name
    @TypeConverter
    fun toDebtPriority(v: String): DebtPriority =
        try { DebtPriority.valueOf(v) } catch (e: Exception) { DebtPriority.NORMAL }

    @TypeConverter
    fun fromGoalFrequency(v: GoalFrequency): String = v.name
    @TypeConverter
    fun toGoalFrequency(v: String): GoalFrequency =
        try { GoalFrequency.valueOf(v) } catch (e: Exception) { GoalFrequency.SEMANAL }
}

@Database(
    entities = [OperationEntity::class, DebtEntity::class, GoalEntity::class],
    version = 12,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun operationDao(): OperationDao
    abstract fun debtDao(): DebtDao
    abstract fun goalDao(): GoalDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        // agrega la columna "method" sin borrar los datos existentes
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE operations ADD COLUMN method TEXT NOT NULL DEFAULT 'EFECTIVO'")
            }
        }

        // agrega la tabla de deudas sin borrar los datos existentes
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `debts` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`type` TEXT NOT NULL, " +
                        "`name` TEXT NOT NULL, " +
                        "`amount` INTEGER NOT NULL, " +
                        "`note` TEXT, " +
                        "`createdAt` INTEGER NOT NULL, " +
                        "`status` TEXT NOT NULL, " +
                        "`settledAt` INTEGER)"
                )
            }
        }

        // agrega vencimiento, cuotas, prioridad y objetivo de pago sin borrar datos
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE debts ADD COLUMN dueDate INTEGER")
                db.execSQL("ALTER TABLE debts ADD COLUMN installments INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE debts ADD COLUMN priority TEXT NOT NULL DEFAULT 'NORMAL'")
                db.execSQL("ALTER TABLE debts ADD COLUMN isGoal INTEGER NOT NULL DEFAULT 0")
            }
        }

        // agrega el conteo de cuotas pagadas sin borrar datos
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE debts ADD COLUMN paidInstallments INTEGER NOT NULL DEFAULT 0")
            }
        }

        // agrega deudas fijas mensuales sin borrar datos
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE debts ADD COLUMN isFixedMonthly INTEGER NOT NULL DEFAULT 0")
            }
        }

        // separa el método del pago y del cobro sin borrar datos
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE operations ADD COLUMN collectionMethod TEXT NOT NULL DEFAULT 'EFECTIVO'")
                db.execSQL("UPDATE operations SET collectionMethod = method")
            }
        }

        // marca si ya se hizo el cambio de transferencia a efectivo
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE operations ADD COLUMN conversionDone INTEGER NOT NULL DEFAULT 0")
            }
        }

        // distingue cambio parcial (solo el pago) de cambio total (con adelanto)
        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE operations ADD COLUMN conversionTotal INTEGER NOT NULL DEFAULT 0")
            }
        }

        // valor de cada cuota (las deudas antiguas guardaban el total)
        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE debts ADD COLUMN installmentAmount INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    "UPDATE debts SET installmentAmount = " +
                        "CASE WHEN installments > 1 THEN amount / installments ELSE amount END"
                )
            }
        }

        // metas de ahorro (con foto y fecha objetivo) sin borrar datos
        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `goals` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`name` TEXT NOT NULL, " +
                        "`targetAmount` INTEGER NOT NULL, " +
                        "`savedAmount` INTEGER NOT NULL, " +
                        "`frequency` TEXT NOT NULL, " +
                        "`targetDate` INTEGER, " +
                        "`photoPath` TEXT, " +
                        "`note` TEXT, " +
                        "`createdAt` INTEGER NOT NULL, " +
                        "`completed` INTEGER NOT NULL, " +
                        "`completedAt` INTEGER)"
                )
            }
        }

        fun getInstance(ctx: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(ctx, AppDatabase::class.java, "adelanto.db")
                    .addMigrations(
                        MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6,
                        MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10,
                        MIGRATION_10_11, MIGRATION_11_12
                    )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
