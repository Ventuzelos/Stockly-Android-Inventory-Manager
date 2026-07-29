package com.angelapereira.stockly.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Product::class],
    version = 1,
    exportSchema = false
)
abstract class StocklyDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao

    companion object {

        @Volatile
        private var INSTANCE: StocklyDatabase? = null

        fun getInstance(context: Context): StocklyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StocklyDatabase::class.java,
                    DATABASE_NAME
                ).build()

                INSTANCE = instance
                instance
            }
        }

        private const val DATABASE_NAME = "stockly_database"
    }
}