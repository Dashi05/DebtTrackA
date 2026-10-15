package com.example.debttrack.data.backup

import androidx.room.withTransaction
import com.example.debttrack.data.database.AppDatabase
import com.example.debttrack.data.entities.Category
import com.example.debttrack.data.entities.Debt
import com.example.debttrack.data.entities.DebtStatus
import com.example.debttrack.data.entities.DebtTagCrossRef
import com.example.debttrack.data.entities.DebtType
import com.example.debttrack.data.entities.Payment
import com.example.debttrack.data.entities.Tag
import org.json.JSONArray
import org.json.JSONObject

/**
 * Экспорт и импорт всех данных приложения одним файлом (резервное копирование).
 *
 * Формат — JSON: он читается человеком, не требует дополнительных библиотек
 * (org.json входит в состав Android) и переживает обновления приложения.
 * Поле [KEY_VERSION] позволяет в будущем распознать файл старого формата.
 *
 * Импорт заменяет данные целиком, а не дописывает их: иначе повторное
 * восстановление одной и той же копии удваивало бы каждый долг. Всё
 * происходит внутри одной транзакции — при ошибке база остаётся нетронутой.
 */
class BackupRepository(private val database: AppDatabase) {

    /** Собирает все таблицы в JSON-документ. */
    suspend fun export(): String {
        val categories = database.categoryDao().getAllOnce()
        val tags = database.tagDao().getAllOnce()
        val debts = database.debtDao().getAllOnce()
        val payments = database.paymentDao().getAllOnce()
        val crossRefs = database.tagDao().getAllCrossRefsOnce()

        val root = JSONObject()
        root.put(KEY_VERSION, FORMAT_VERSION)
        root.put(KEY_EXPORTED_AT, System.currentTimeMillis())

        root.put(KEY_CATEGORIES, JSONArray().apply {
            categories.forEach { category ->
                put(JSONObject().apply {
                    put("id", category.id)
                    put("name", category.name)
                    put("colorHex", category.colorHex)
                })
            }
        })

        root.put(KEY_TAGS, JSONArray().apply {
            tags.forEach { tag ->
                put(JSONObject().apply {
                    put("id", tag.id)
                    put("name", tag.name)
                })
            }
        })

        root.put(KEY_DEBTS, JSONArray().apply {
            debts.forEach { debt ->
                put(JSONObject().apply {
                    put("id", debt.id)
                    put("personName", debt.personName)
                    put("type", debt.type.name)
                    put("amount", debt.amount)
                    put("paidAmount", debt.paidAmount)
                    put("status", debt.status.name)
                    put("categoryId", debt.categoryId)
                    put("description", debt.description)
                    put("createdDate", debt.createdDate)
                    put("dueDate", debt.dueDate)
                })
            }
        })

        root.put(KEY_PAYMENTS, JSONArray().apply {
            payments.forEach { payment ->
                put(JSONObject().apply {
                    put("id", payment.id)
                    put("debtId", payment.debtId)
                    put("amount", payment.amount)
                    put("date", payment.date)
                })
            }
        })

        root.put(KEY_DEBT_TAGS, JSONArray().apply {
            crossRefs.forEach { crossRef ->
                put(JSONObject().apply {
                    put("debtId", crossRef.debtId)
                    put("tagId", crossRef.tagId)
                })
            }
        })

        return root.toString(2)
    }

    /**
     * Разбирает файл и заменяет им содержимое базы.
     *
     * @return сколько долгов восстановлено
     * @throws BackupFormatException если файл не является резервной копией
     */
    suspend fun import(json: String): Int {
        val root = try {
            JSONObject(json)
        } catch (e: Exception) {
            throw BackupFormatException("Файл не похож на резервную копию DebtTrack")
        }

        if (!root.has(KEY_DEBTS) || !root.has(KEY_CATEGORIES)) {
            throw BackupFormatException("В файле нет данных DebtTrack")
        }
        val version = root.optInt(KEY_VERSION, 0)
        if (version > FORMAT_VERSION) {
            throw BackupFormatException(
                "Копия создана более новой версией приложения — обновите DebtTrack"
            )
        }

        val categories = root.getJSONArray(KEY_CATEGORIES).mapItems { item ->
            Category(
                id = item.getLong("id"),
                name = item.getString("name"),
                colorHex = item.optString("colorHex", "#3B6FE0")
            )
        }
        val tags = root.optJSONArray(KEY_TAGS).mapItems { item ->
            Tag(id = item.getLong("id"), name = item.getString("name"))
        }
        val debts = root.getJSONArray(KEY_DEBTS).mapItems { item ->
            Debt(
                id = item.getLong("id"),
                personName = item.getString("personName"),
                type = runCatching { DebtType.valueOf(item.optString("type")) }
                    .getOrDefault(DebtType.OWED_TO_ME),
                amount = item.getDouble("amount"),
                paidAmount = item.optDouble("paidAmount", 0.0),
                status = runCatching { DebtStatus.valueOf(item.optString("status")) }
                    .getOrDefault(DebtStatus.ACTIVE),
                categoryId = item.getLong("categoryId"),
                description = item.optString("description", ""),
                createdDate = item.optLong("createdDate", System.currentTimeMillis()),
                dueDate = item.getLong("dueDate")
            )
        }
        val payments = root.optJSONArray(KEY_PAYMENTS).mapItems { item ->
            Payment(
                id = item.getLong("id"),
                debtId = item.getLong("debtId"),
                amount = item.getDouble("amount"),
                date = item.getLong("date")
            )
        }
        val crossRefs = root.optJSONArray(KEY_DEBT_TAGS).mapItems { item ->
            DebtTagCrossRef(debtId = item.getLong("debtId"), tagId = item.getLong("tagId"))
        }

        // Долг ссылается на категорию, платёж — на долг. Копия могла потерять
        // категорию, поэтому оставляем только записи с существующими ссылками:
        // иначе вставка упадёт на проверке внешнего ключа и откатит весь импорт.
        val categoryIds = categories.map { it.id }.toSet()
        val validDebts = debts.filter { it.categoryId in categoryIds }
        val debtIds = validDebts.map { it.id }.toSet()
        val validPayments = payments.filter { it.debtId in debtIds }
        val tagIds = tags.map { it.id }.toSet()
        val validCrossRefs = crossRefs.filter { it.debtId in debtIds && it.tagId in tagIds }

        database.withTransaction {
            // Удаляем от зависимых таблиц к основным, вставляем — в обратном порядке.
            database.tagDao().deleteAllCrossRefs()
            database.paymentDao().deleteAll()
            database.debtDao().deleteAll()
            database.tagDao().deleteAll()
            database.categoryDao().deleteAll()

            database.categoryDao().insertAllKeepingIds(categories)
            database.tagDao().insertAllKeepingIds(tags)
            database.debtDao().insertAllKeepingIds(validDebts)
            database.paymentDao().insertAllKeepingIds(validPayments)
            database.tagDao().insertAllCrossRefs(validCrossRefs)
        }

        return validDebts.size
    }

    /** Обходит массив JSON; отсутствующий раздел считается пустым. */
    private fun <T> JSONArray?.mapItems(transform: (JSONObject) -> T): List<T> {
        if (this == null) return emptyList()
        return (0 until length()).map { index -> transform(getJSONObject(index)) }
    }

    private companion object {
        const val FORMAT_VERSION = 1
        const val KEY_VERSION = "version"
        const val KEY_EXPORTED_AT = "exportedAt"
        const val KEY_CATEGORIES = "categories"
        const val KEY_TAGS = "tags"
        const val KEY_DEBTS = "debts"
        const val KEY_PAYMENTS = "payments"
        const val KEY_DEBT_TAGS = "debtTags"
    }
}

/** Файл выбран, но это не резервная копия приложения. */
class BackupFormatException(message: String) : Exception(message)
