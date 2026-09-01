package com.angelapereira.stockly.ui.products

import com.angelapereira.stockly.data.local.Product

sealed interface ProductsUiState {

    data object Loading : ProductsUiState

    data class Success(
        val products: List<Product>
    ) : ProductsUiState

    data class DeleteSuccess(
        val productName: String
    ) : ProductsUiState

    data object DeleteError : ProductsUiState

    data object Error : ProductsUiState
}