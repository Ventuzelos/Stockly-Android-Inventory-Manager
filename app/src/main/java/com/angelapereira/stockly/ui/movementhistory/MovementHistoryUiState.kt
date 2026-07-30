package com.angelapereira.stockly.ui.movementhistory

import com.angelapereira.stockly.data.local.StockMovementWithProduct

sealed interface MovementHistoryUiState {

    data object Loading : MovementHistoryUiState

    data class Success(
        val movements: List<StockMovementWithProduct>
    ) : MovementHistoryUiState

    data object Error : MovementHistoryUiState
}