package com.angelapereira.stockly.ui.editproduct

import com.angelapereira.stockly.data.local.Product

sealed interface EditProductUiState {

    data object Idle : EditProductUiState

    data object Loading : EditProductUiState

    data class ProductLoaded(
        val product: Product
    ) : EditProductUiState

    data class UpdateSuccess(
        val productName: String
    ) : EditProductUiState

    data object DuplicateName : EditProductUiState

    data object ProductNotFound : EditProductUiState

    data object Error : EditProductUiState
}