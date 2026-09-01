package com.angelapereira.stockly.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        Product::class,
        StockMovement::class,
        User::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(StocklyConverters::class)
abstract class StocklyDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao

    abstract fun stockMovementDao(): StockMovementDao

    abstract fun userDao(): UserDao

    companion object {

        @Volatile
        private var INSTANCE: StocklyDatabase? = null

        fun getInstance(context: Context): StocklyDatabase {
            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StocklyDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }

        private const val DATABASE_NAME = "stockly_database"
    }
}