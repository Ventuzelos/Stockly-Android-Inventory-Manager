package com.angelapereira.stockly.ui.stockmovement

sealed interface StockMovementUiState {

    data object Idle : StockMovementUiState

    data object Loading : StockMovementUiState

    data object Success : StockMovementUiState

    data object InsufficientStock : StockMovementUiState

    data object ProductNotFound : StockMovementUiState

    data object InvalidQuantity : StockMovementUiState

    data object Error : StockMovementUiState
}