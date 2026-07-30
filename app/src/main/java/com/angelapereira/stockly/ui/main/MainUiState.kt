package com.angelapereira.stockly.ui.main

sealed interface MainUiState {

    data object Idle : MainUiState

    data object Loading : MainUiState

    data class Success(
        val productName: String
    ) : MainUiState

    data object Duplicate : MainUiState

    data class Error(
        val message: String? = null
    ) : MainUiState
}