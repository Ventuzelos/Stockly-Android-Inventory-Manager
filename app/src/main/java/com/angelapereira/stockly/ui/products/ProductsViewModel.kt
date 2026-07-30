package com.angelapereira.stockly.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.angelapereira.stockly.data.local.Product
import com.angelapereira.stockly.data.repository.ProductRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ProductsViewModel(
    private val repository: ProductRepository
) : ViewModel() {

    private var productsJob: Job? = null

    fun getProducts(query: String): Flow<List<Product>> {
        return if (query.isBlank()) {
            repository.getAllProducts()
        } else {
            repository.searchProducts(query)
        }
    }

    fun deleteProduct(
        product: Product,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        productsJob?.cancel()

        productsJob = viewModelScope.launch {
            try {
                repository.deleteProduct(product)
                onSuccess()
            } catch (exception: Exception) {
                onError()
            }
        }
    }

    class Factory(
        private val repository: ProductRepository
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T {
            if (modelClass.isAssignableFrom(ProductsViewModel::class.java)) {
                return ProductsViewModel(repository) as T
            }

            throw IllegalArgumentException(
                "Unknown ViewModel class: ${modelClass.name}"
            )
        }
    }
}