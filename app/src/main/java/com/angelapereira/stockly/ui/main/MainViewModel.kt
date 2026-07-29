package com.angelapereira.stockly.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.angelapereira.stockly.data.local.Product
import com.angelapereira.stockly.data.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: ProductRepository
) : ViewModel() {

    val productsCount: Flow<Int> = repository.getProductsCount()

    val lowStockCount: Flow<Int> = repository.getLowStockCount()

    fun addProduct(
        product: Product,
        onSuccess: () -> Unit,
        onDuplicate: () -> Unit,
        onError: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val alreadyExists = repository.productNameExists(product.name)

                if (alreadyExists) {
                    onDuplicate()
                    return@launch
                }

                repository.insertProduct(product)
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
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(repository) as T
            }

            throw IllegalArgumentException(
                "Unknown ViewModel class: ${modelClass.name}"
            )
        }
    }
}