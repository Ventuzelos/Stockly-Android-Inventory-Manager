package com.angelapereira.stockly.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stock_movements",
    foreignKeys = [
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["productId"])
    ]
)
data class StockMovement(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val productId: Int,
    val type: MovementType,
    val quantity: Int,
    val createdAt: Long = System.currentTimeMillis()
)