package com.angelapereira.stockly.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Product::class,
        StockMovement::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(StocklyConverters::class)
abstract class StocklyDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao

    abstract fun stockMovementDao(): StockMovementDao

    companion object {

        @Volatile
        private var INSTANCE: StocklyDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS stock_movements (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        productId INTEGER NOT NULL,
                        type TEXT NOT NULL,
                        quantity INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY(productId)
                            REFERENCES products(id)
                            ON UPDATE NO ACTION
                            ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_stock_movements_productId
                    ON stock_movements(productId)
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): StocklyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StocklyDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()

                INSTANCE = instance
                instance
            }
        }

        private const val DATABASE_NAME = "stockly_database"
    }
}