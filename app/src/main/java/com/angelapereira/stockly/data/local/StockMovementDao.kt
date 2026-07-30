package com.angelapereira.stockly.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface StockMovementDao {

    @Insert
    suspend fun insert(movement: StockMovement): Long

    @Query(
        """
        SELECT * FROM stock_movements
        ORDER BY createdAt DESC
        """
    )
    fun getAllMovements(): Flow<List<StockMovement>>

    @Query(
        """
        SELECT * FROM stock_movements
        WHERE productId = :productId
        ORDER BY createdAt DESC
        """
    )
    fun getMovementsByProduct(
        productId: Int
    ): Flow<List<StockMovement>>

    @Query(
        """
    SELECT
        stock_movements.id,
        stock_movements.productId,
        products.name AS productName,
        stock_movements.type,
        stock_movements.quantity,
        stock_movements.createdAt
    FROM stock_movements
    INNER JOIN products
        ON products.id = stock_movements.productId
    ORDER BY stock_movements.createdAt DESC
    """
    )
    fun getMovementHistory(): Flow<List<StockMovementWithProduct>>

    @Transaction
    suspend fun registerMovement(
        movement: StockMovement,
        productDao: ProductDao
    ) {
        val product = productDao.getProductById(
            movement.productId
        ) ?: throw IllegalArgumentException(
            "Produto não encontrado."
        )

        require(movement.quantity > 0) {
            "A quantidade deve ser superior a zero."
        }

        val updatedQuantity = when (movement.type) {
            MovementType.ENTRY -> {
                product.quantity + movement.quantity
            }

            MovementType.EXIT -> {
                require(movement.quantity <= product.quantity) {
                    "Stock insuficiente."
                }

                product.quantity - movement.quantity
            }
        }

        productDao.update(
            product.copy(quantity = updatedQuantity)
        )

        insert(movement)
    }
}