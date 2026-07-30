package com.angelapereira.stockly.data.local

data class StockMovementWithProduct(
    val id: Int,
    val productId: Int,
    val productName: String,
    val type: MovementType,
    val quantity: Int,
    val createdAt: Long
)