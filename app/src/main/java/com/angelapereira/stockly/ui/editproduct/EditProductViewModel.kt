package com.angelapereira.stockly.ui.editproduct

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.angelapereira.stockly.data.local.Product
import com.angelapereira.stockly.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditProductViewModel(
    private val repository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<EditProductUiState>(
        EditProductUiState.Idle
    )

    val uiState: StateFlow<EditProductUiState> =
        _uiState.asStateFlow()

    fun loadProduct(productId: Int) {
        if (_uiState.value is EditProductUiState.Loading) {
            return
        }

        viewModelScope.launch {
            _uiState.value = EditProductUiState.Loading

            try {
                val product = repository.getProductById(productId)

                _uiState.value = if (product == null) {
                    EditProductUiState.ProductNotFound
                } else {
                    EditProductUiState.ProductLoaded(product)
                }
            } catch (exception: Exception) {
                _uiState.value = EditProductUiState.Error
            }
        }
    }

    fun updateProduct(product: Product) {
        if (_uiState.value is EditProductUiState.Loading) {
            return
        }

        viewModelScope.launch {
            _uiState.value = EditProductUiState.Loading

            try {
                val duplicateName =
                    repository.productNameExistsForAnotherProduct(
                        name = product.name,
                        productId = product.id
                    )

                if (duplicateName) {
                    _uiState.value =
                        EditProductUiState.DuplicateName

                    return@launch
                }

                repository.updateProduct(product)

                _uiState.value =
                    EditProductUiState.UpdateSuccess(
                        productName = product.name
                    )
            } catch (exception: Exception) {
                _uiState.value = EditProductUiState.Error
            }
        }
    }

    fun resetUiState() {
        _uiState.value = EditProductUiState.Idle
    }

    class Factory(
        private val repository: ProductRepository
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T {
            if (
                modelClass.isAssignableFrom(
                    EditProductViewModel::class.java
                )
            ) {
                return EditProductViewModel(repository) as T
            }

            throw IllegalArgumentException(
                "Unknown ViewModel class: ${modelClass.name}"
            )
        }
    }
}