package com.angelapereira.stockly.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val category: String,
    val quantity: Int,
    val minimumStock: Int,
    val price: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
) {
    val isLowStock: Boolean
        get() = quantity <= minimumStock
}