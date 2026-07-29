package com.angelapereira.stockly.data.repository

import com.angelapereira.stockly.data.local.Product
import com.angelapereira.stockly.data.local.ProductDao
import kotlinx.coroutines.flow.Flow

class ProductRepository(
    private val productDao: ProductDao
) {

    fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts()
    }

    fun searchProducts(query: String): Flow<List<Product>> {
        return productDao.searchProducts(query)
    }

    fun getProductsCount(): Flow<Int> {
        return productDao.getProductsCount()
    }

    fun getLowStockCount(): Flow<Int> {
        return productDao.getLowStockCount()
    }

    suspend fun getProductById(productId: Int): Product? {
        return productDao.getProductById(productId)
    }

    suspend fun insertProduct(product: Product): Long {
        return productDao.insert(product)
    }

    suspend fun updateProduct(product: Product) {
        productDao.update(product)
    }

    suspend fun deleteProduct(product: Product) {
        productDao.delete(product)
    }

    suspend fun productNameExists(name: String): Boolean {
        return productDao.productNameExists(name)
    }

    suspend fun productNameExistsForAnotherProduct(
        name: String,
        productId: Int
    ): Boolean {
        return productDao.productNameExistsForAnotherProduct(
            name = name,
            productId = productId
        )
    }
}