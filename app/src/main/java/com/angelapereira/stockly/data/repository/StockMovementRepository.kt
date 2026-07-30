package com.angelapereira.stockly.data.repository

import com.angelapereira.stockly.data.local.ProductDao
import com.angelapereira.stockly.data.local.StockMovement
import com.angelapereira.stockly.data.local.StockMovementDao
import kotlinx.coroutines.flow.Flow

class StockMovementRepository(
    private val stockMovementDao: StockMovementDao,
    private val productDao: ProductDao
) {

    fun getAllMovements(): Flow<List<StockMovement>> {
        return stockMovementDao.getAllMovements()
    }

    fun getMovementsByProduct(
        productId: Int
    ): Flow<List<StockMovement>> {
        return stockMovementDao.getMovementsByProduct(productId)
    }

    suspend fun registerMovement(
        movement: StockMovement
    ) {
        stockMovementDao.registerMovement(
            movement = movement,
            productDao = productDao
        )
    }
}