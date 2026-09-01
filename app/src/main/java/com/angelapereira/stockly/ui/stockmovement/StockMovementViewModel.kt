package com.angelapereira.stockly.ui.stockmovement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.angelapereira.stockly.data.local.StockMovement
import com.angelapereira.stockly.data.repository.StockMovementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StockMovementViewModel(
    private val repository: StockMovementRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<StockMovementUiState>(
            StockMovementUiState.Idle
        )

    val uiState: StateFlow<StockMovementUiState> =
        _uiState.asStateFlow()

    fun registerMovement(
        movement: StockMovement
    ) {
        if (_uiState.value is StockMovementUiState.Loading) {
            return
        }

        viewModelScope.launch {
            _uiState.value = StockMovementUiState.Loading

            try {
                repository.registerMovement(movement)

                _uiState.value =
                    StockMovementUiState.Success
            } catch (exception: IllegalArgumentException) {
                _uiState.value = when (
                    exception.message
                ) {
                    "Stock insuficiente." ->
                        StockMovementUiState.InsufficientStock

                    "Produto não encontrado." ->
                        StockMovementUiState.ProductNotFound

                    "A quantidade deve ser superior a zero." ->
                        StockMovementUiState.InvalidQuantity

                    else ->
                        StockMovementUiState.Error
                }
            } catch (exception: Exception) {
                _uiState.value =
                    StockMovementUiState.Error
            }
        }
    }

    fun resetUiState() {
        _uiState.value = StockMovementUiState.Idle
    }

    class Factory(
        private val repository: StockMovementRepository
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T {
            if (
                modelClass.isAssignableFrom(
                    StockMovementViewModel::class.java
                )
            ) {
                return StockMovementViewModel(repository) as T
            }

            throw IllegalArgumentException(
                "Unknown ViewModel class: ${modelClass.name}"
            )
        }
    }
}