package com.angelapereira.stockly.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.angelapereira.stockly.data.local.MovementType
import com.angelapereira.stockly.data.local.Product
import com.angelapereira.stockly.data.local.StockMovement
import com.angelapereira.stockly.data.repository.ProductRepository
import com.angelapereira.stockly.data.repository.StockMovementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

enum class MovementHistoryFilter {
    ALL,
    ENTRY,
    EXIT
}

data class StockMovementHistoryUiState(
    val movements: List<StockMovement> = emptyList(),
    val products: List<Product> = emptyList(),
    val filter: MovementHistoryFilter = MovementHistoryFilter.ALL
) {
    val filteredMovements: List<StockMovement>
        get() {
            return when (filter) {
                MovementHistoryFilter.ALL -> movements

                MovementHistoryFilter.ENTRY ->
                    movements.filter {
                        it.type == MovementType.ENTRY
                    }

                MovementHistoryFilter.EXIT ->
                    movements.filter {
                        it.type == MovementType.EXIT
                    }
            }
        }

    val productNames: Map<Int, String>
        get() = products.associate {
            it.id to it.name
        }
}

class StockMovementHistoryViewModel(
    private val stockMovementRepository: StockMovementRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            StockMovementHistoryUiState()
        )

    val uiState: StateFlow<StockMovementHistoryUiState> =
        _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                stockMovementRepository.getAllMovements(),
                productRepository.getAllProducts()
            ) { movements, products ->

                StockMovementHistoryUiState(
                    movements = movements,
                    products = products,
                    filter = _uiState.value.filter
                )

            }.collect { state ->

                _uiState.value = state
            }
        }
    }

    fun setFilter(
        filter: MovementHistoryFilter
    ) {
        _uiState.value =
            _uiState.value.copy(
                filter = filter
            )
    }

    class Factory(
        private val stockMovementRepository: StockMovementRepository,
        private val productRepository: ProductRepository
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T {

            if (
                modelClass.isAssignableFrom(
                    StockMovementHistoryViewModel::class.java
                )
            ) {
                return StockMovementHistoryViewModel(
                    stockMovementRepository,
                    productRepository
                ) as T
            }

            throw IllegalArgumentException(
                "Unknown ViewModel class"
            )
        }
    }
}