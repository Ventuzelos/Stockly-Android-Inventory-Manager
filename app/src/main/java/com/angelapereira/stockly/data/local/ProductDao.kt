package com.angelapereira.stockly.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(product: Product): Long

    @Update
    suspend fun update(product: Product)

    @Delete
    suspend fun delete(product: Product)

    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    suspend fun getProductById(productId: Int): Product?

    @Query(
        """
        SELECT * FROM products
        WHERE name LIKE '%' || :query || '%'
        ORDER BY name ASC
        """
    )
    fun searchProducts(query: String): Flow<List<Product>>

    @Query("SELECT COUNT(*) FROM products")
    fun getProductsCount(): Flow<Int>

    @Query(
        """
        SELECT COUNT(*) FROM products
        WHERE quantity <= minimumStock
        """
    )
    fun getLowStockCount(): Flow<Int>

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM products
            WHERE LOWER(name) = LOWER(:name)
        )
        """
    )
    suspend fun productNameExists(name: String): Boolean
}