package com.angelapereira.stockly.ui.movementhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.angelapereira.stockly.data.repository.StockMovementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MovementHistoryViewModel(
    private val repository: StockMovementRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<MovementHistoryUiState>(
            MovementHistoryUiState.Loading
        )

    val uiState: StateFlow<MovementHistoryUiState> =
        _uiState.asStateFlow()

    init {
        observeMovementHistory()
    }

    private fun observeMovementHistory() {
        viewModelScope.launch {
            try {
                repository.getMovementHistory().collect { movements ->
                    _uiState.value =
                        MovementHistoryUiState.Success(movements)
                }
            } catch (exception: Exception) {
                _uiState.value =
                    MovementHistoryUiState.Error
            }
        }
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
                    MovementHistoryViewModel::class.java
                )
            ) {
                return MovementHistoryViewModel(repository) as T
            }

            throw IllegalArgumentException(
                "Unknown ViewModel class: ${modelClass.name}"
            )
        }
    }
}