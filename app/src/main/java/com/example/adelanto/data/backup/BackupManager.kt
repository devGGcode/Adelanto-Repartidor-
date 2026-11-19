package com.example.adelanto.data.backup

import android.content.Context
import android.util.Base64
import com.example.adelanto.data.db.DebtEntity
import com.example.adelanto.data.db.DebtPriority
import com.example.adelanto.data.db.DebtType
import com.example.adelanto.data.db.GoalEntity
import com.example.adelanto.data.db.GoalFrequency
import com.example.adelanto.data.db.OpType
import com.example.adelanto.data.db.OperationEntity
import com.example.adelanto.data.db.PayMethod
import com.example.adelanto.domain.WeekUtils
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class BackupData(
    val operations: List<OperationEntity>,
    val debts: List<DebtEntity>,
    val goals: List<GoalEntity> = emptyList(),
    val cash: Map<String, String> = emptyMap(),
    val goalPhotos: Map<Long, ByteArray> = emptyMap(),
    val goalsIncluded: Boolean = true
)

object BackupManager {
    private const val APP = "adelanto"
    private const val VERSION = 1
    private const val PREFS = "adelanto"

    fun build(
        context: Context,
        operations: List<OperationEntity>,
        debts: List<DebtEntity>,
        goals: List<GoalEntity>
    ): String {
        val root = JSONObject()
        root.put("app", APP)
        root.put("version", VERSION)
        root.put("exportedAt", System.currentTimeMillis())

        val ops = JSONArray()
        operations.forEach { op ->
            ops.put(JSONObject().apply {
                put("id", op.id)
                put("type", op.type.name)
                put("paymentAmount", op.paymentAmount ?: JSONObject.NULL)
                put("collectionAmount", op.collectionAmount ?: JSONObject.NULL)
                put("profit", op.profit)
                put("status", op.status)
                put("note", op.note ?: JSONObject.NULL)
                put("timestamp", op.timestamp)
                put("weekStart", op.weekStart)
                put("method", op.method.name)
                put("collectionMethod", op.collectionMethod.name)
                put("conversionDone", op.conversionDone)
                put("conversionTotal", op.conversionTotal)
            })
        }
        root.put("operations", ops)

        val debtsArr = JSONArray()
        debts.forEach { d ->
            debtsArr.put(JSONObject().apply {
                put("id", d.id)
                put("type", d.type.name)
                put("name", d.name)
                put("amount", d.amount)
                put("note", d.note ?: JSONObject.NULL)
                put("createdAt", d.createdAt)
                put("status", d.status)
                put("settledAt", d.settledAt ?: JSONObject.NULL)
                put("dueDate", d.dueDate ?: JSONObject.NULL)
                put("installments", d.installments)
                put("paidInstallments", d.paidInstallments)
                put("installmentAmount", d.installmentAmount)
                put("isFixedMonthly", d.isFixedMonthly)
                put("priority", d.priority.name)
                put("isGoal", d.isGoal)
            })
        }
        root.put("debts", debtsArr)

        val goalsArr = JSONArray()
        goals.forEach { g ->
            goalsArr.put(JSONObject().apply {
                put("id", g.id)
                put("name", g.name)
                put("targetAmount", g.targetAmount)
                put("savedAmount", g.savedAmount)
                put("frequency", g.frequency.name)
                put("targetDate", g.targetDate ?: JSONObject.NULL)
                put("note", g.note ?: JSONObject.NULL)
                put("createdAt", g.createdAt)
                put("completed", g.completed)
                put("completedAt", g.completedAt ?: JSONObject.NULL)
                val photo = g.photoPath?.let { File(it) }
                if (photo != null && photo.exists()) {
                    put("photo", Base64.encodeToString(photo.readBytes(), Base64.NO_WRAP))
                }
            })
        }
        root.put("goals", goalsArr)

        val cashObj = JSONObject()
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).all.forEach { (key, value) ->
            if ((key.startsWith("caja_") || key.startsWith("caja_transfer_")) && value is String) {
                cashObj.put(key, value)
            }
        }
        root.put("cash", cashObj)

        return root.toString(2)
    }

    fun parse(text: String): BackupData {
        val root = JSONObject(text)
        require(root.optString("app") == APP) { "El archivo no es una copia de Adelanto" }
        val goalsIncluded = root.has("goals")

        val operations = mutableListOf<OperationEntity>()
        val opsArray = root.optJSONArray("operations") ?: JSONArray()
        for (i in 0 until opsArray.length()) {
            val o = opsArray.getJSONObject(i)
            val payMethod = parseMethod(o.stringOrNull("method"), PayMethod.EFECTIVO)
            operations.add(
                OperationEntity(
                    id = o.optLong("id", 0L),
                    type = try { OpType.valueOf(o.optString("type", "FULL")) } catch (e: Exception) { OpType.FULL },
                    paymentAmount = o.longOrNull("paymentAmount"),
                    collectionAmount = o.longOrNull("collectionAmount"),
                    profit = o.optLong("profit", 0L),
                    status = o.optString("status", "resolved"),
                    note = o.stringOrNull("note"),
                    timestamp = o.optLong("timestamp", System.currentTimeMillis()),
                    weekStart = o.optLong("weekStart", WeekUtils.weekStartMillis()),
                    method = payMethod,
                    collectionMethod = parseMethod(o.stringOrNull("collectionMethod"), payMethod),
                    conversionDone = o.optBoolean("conversionDone", false),
                    conversionTotal = o.optBoolean("conversionTotal", false)
                )
            )
        }

        val debts = mutableListOf<DebtEntity>()
        val debtsArray = root.optJSONArray("debts") ?: JSONArray()
        for (i in 0 until debtsArray.length()) {
            val d = debtsArray.getJSONObject(i)
            debts.add(
                DebtEntity(
                    id = d.optLong("id", 0L),
                    type = try { DebtType.valueOf(d.optString("type", "PAGAR")) } catch (e: Exception) { DebtType.PAGAR },
                    name = d.optString("name", "Deuda"),
                    amount = d.optLong("amount", 0L),
                    note = d.stringOrNull("note"),
                    createdAt = d.optLong("createdAt", System.currentTimeMillis()),
                    status = d.optString("status", "pendiente"),
                    settledAt = d.longOrNull("settledAt"),
                    dueDate = d.longOrNull("dueDate"),
                    installments = d.optInt("installments", 1),
                    paidInstallments = d.optInt("paidInstallments", 0),
                    installmentAmount = d.optLong("installmentAmount", 0L),
                    isFixedMonthly = d.optBoolean("isFixedMonthly", false),
                    priority = try { DebtPriority.valueOf(d.optString("priority", "NORMAL")) } catch (e: Exception) { DebtPriority.NORMAL },
                    isGoal = d.optBoolean("isGoal", false)
                )
            )
        }

        val goals = mutableListOf<GoalEntity>()
        val goalPhotos = mutableMapOf<Long, ByteArray>()
        val goalsArray = root.optJSONArray("goals") ?: JSONArray()
        for (i in 0 until goalsArray.length()) {
            val g = goalsArray.getJSONObject(i)
            val id = g.optLong("id", 0L)
            val photo = g.stringOrNull("photo")?.let {
                try { Base64.decode(it, Base64.DEFAULT) } catch (e: Exception) { null }
            }
            if (photo != null) goalPhotos[id] = photo
            goals.add(
                GoalEntity(
                    id = id,
                    name = g.optString("name", "Meta"),
                    targetAmount = g.optLong("targetAmount", 0L),
                    savedAmount = g.optLong("savedAmount", 0L),
                    frequency = try { GoalFrequency.valueOf(g.optString("frequency", "SEMANAL")) } catch (e: Exception) { GoalFrequency.SEMANAL },
                    targetDate = g.longOrNull("targetDate"),
                    photoPath = null,
                    note = g.stringOrNull("note"),
                    createdAt = g.optLong("createdAt", System.currentTimeMillis()),
                    completed = g.optBoolean("completed", false),
                    completedAt = g.longOrNull("completedAt")
                )
            )
        }

        val cash = mutableMapOf<String, String>()
        val cashObj = root.optJSONObject("cash")
        if (cashObj != null) {
            val keys = cashObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                cash[key] = cashObj.optString(key, "")
            }
        }

        return BackupData(operations, debts, goals, cash, goalPhotos, goalsIncluded)
    }

    fun writeGoalPhoto(context: Context, goalId: Long, bytes: ByteArray): String? {
        return try {
            val dir = File(context.filesDir, "goals").apply { mkdirs() }
            val file = File(dir, "goal_${goalId}_${System.currentTimeMillis()}.jpg")
            file.writeBytes(bytes)
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    fun restoreCash(context: Context, cash: Map<String, String>) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        prefs.all.keys
            .filter { it.startsWith("caja_") || it.startsWith("caja_transfer_") }
            .forEach { editor.remove(it) }
        cash.forEach { (key, value) -> editor.putString(key, value) }
        editor.apply()
    }

    private fun JSONObject.stringOrNull(key: String): String? =
        if (has(key) && !isNull(key)) getString(key) else null

    private fun JSONObject.longOrNull(key: String): Long? =
        if (has(key) && !isNull(key)) getLong(key) else null

    private fun parseMethod(value: String?, fallback: PayMethod): PayMethod =
        try {
            if (value == null) fallback else PayMethod.valueOf(value)
        } catch (e: Exception) {
            fallback
        }
}
