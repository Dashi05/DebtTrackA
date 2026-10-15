package com.example.debttrack.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.debttrack.data.entities.Category
import com.example.debttrack.data.entities.Debt
import com.example.debttrack.data.entities.DebtTagCrossRef
import com.example.debttrack.data.entities.Payment
import com.example.debttrack.data.entities.Tag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Category::class,
        Debt::class,
        Payment::class,
        Tag::class,
        DebtTagCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun debtDao(): DebtDao
    abstract fun paymentDao(): PaymentDao
    abstract fun tagDao(): TagDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "debttrack.db"
                )
                    .addCallback(prepopulateCallback)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /** Предзаполнение стартовыми категориями при первом создании БД. */
        private val prepopulateCallback = object : Callback() {
            override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        database.categoryDao().insertAll(defaultCategories)
                    }
                }
            }
        }

        private val defaultCategories = listOf(
            Category(name = "Электроника", colorHex = "#3B6FE0"),
            Category(name = "Путешествия", colorHex = "#14B8A6"),
            Category(name = "Автомобиль", colorHex = "#EF4444"),
            Category(name = "Еда", colorHex = "#F59E0B"),
            Category(name = "Жильё", colorHex = "#8B5CF6"),
            Category(name = "Другое", colorHex = "#6B7280")
        )
    }
}
