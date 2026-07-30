package com.angelapereira.stockly.ui.editproduct

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.angelapereira.stockly.data.local.Product
import com.angelapereira.stockly.data.repository.ProductRepository
import kotlinx.coroutines.launch

class EditProductViewModel(
    private val repository: ProductRepository
) : ViewModel() {

    fun loadProduct(
        productId: Int,
        onSuccess: (Product) -> Unit,
        onNotFound: () -> Unit,
        onError: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val product = repository.getProductById(productId)

                if (product == null) {
                    onNotFound()
                } else {
                    onSuccess(product)
                }
            } catch (exception: Exception) {
                onError()
            }
        }
    }

    fun updateProduct(
        product: Product,
        onSuccess: () -> Unit,
        onDuplicate: () -> Unit,
        onError: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val duplicateName =
                    repository.productNameExistsForAnotherProduct(
                        name = product.name,
                        productId = product.id
                    )

                if (duplicateName) {
                    onDuplicate()
                    return@launch
                }

                repository.updateProduct(product)
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