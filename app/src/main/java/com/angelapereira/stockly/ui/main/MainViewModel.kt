package com.angelapereira.stockly.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.angelapereira.stockly.data.local.Product
import com.angelapereira.stockly.data.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class MainViewModel(
    private val repository: ProductRepository
) : ViewModel() {

    val productsCount: Flow<Int> = repository.getProductsCount()

    val lowStockCount: Flow<Int> = repository.getLowStockCount()

    private val _uiState = MutableStateFlow<MainUiState>(
        MainUiState.Idle
    )

    val uiState: StateFlow<MainUiState> =
        _uiState.asStateFlow()

    fun addProduct(product: Product) {
        if (_uiState.value is MainUiState.Loading) {
            return
        }

        viewModelScope.launch {
            _uiState.value = MainUiState.Loading

            try {
                val alreadyExists =
                    repository.productNameExists(product.name)

                if (alreadyExists) {
                    _uiState.value = MainUiState.Duplicate
                    return@launch
                }

                repository.insertProduct(product)

                _uiState.value = MainUiState.Success(
                    productName = product.name
                )
            } catch (exception: Exception) {
                _uiState.value = MainUiState.Error(
                    message = exception.message
                )
            }
        }
    }

    fun resetUiState() {
        _uiState.value = MainUiState.Idle
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