package com.angelapereira.stockly.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.angelapereira.stockly.data.local.Product
import com.angelapereira.stockly.data.repository.ProductRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProductsViewModel(
    private val repository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductsUiState>(
        ProductsUiState.Loading
    )

    val uiState: StateFlow<ProductsUiState> =
        _uiState.asStateFlow()

    private var productsJob: Job? = null

    fun observeProducts(query: String) {
        productsJob?.cancel()

        productsJob = viewModelScope.launch {
            _uiState.value = ProductsUiState.Loading

            try {
                val productsFlow = if (query.isBlank()) {
                    repository.getAllProducts()
                } else {
                    repository.searchProducts(query)
                }

                productsFlow.collectLatest { products ->
                    _uiState.value = ProductsUiState.Success(products)
                }
            } catch (exception: Exception) {
                _uiState.value = ProductsUiState.Error
            }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            try {
                repository.deleteProduct(product)

                _uiState.value = ProductsUiState.DeleteSuccess(
                    productName = product.name
                )
            } catch (exception: Exception) {
                _uiState.value = ProductsUiState.DeleteError
            }
        }
    }

    fun resumeProducts(query: String) {
        observeProducts(query)
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