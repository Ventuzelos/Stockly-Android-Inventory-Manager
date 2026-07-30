package com.angelapereira.stockly.ui.movementhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.angelapereira.stockly.data.local.MovementType
import com.angelapereira.stockly.data.local.StockMovementWithProduct
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

    private var allMovements: List<StockMovementWithProduct> = emptyList()

    private var currentFilter = MovementHistoryFilter()

    init {
        observeMovementHistory()
    }

    private fun observeMovementHistory() {
        viewModelScope.launch {
            try {
                repository.getMovementHistory().collect { movements ->
                    allMovements = movements
                    applyFilters()
                }
            } catch (exception: Exception) {
                _uiState.value = MovementHistoryUiState.Error
            }
        }
    }

    fun filterByType(type: MovementType?) {
        currentFilter = currentFilter.copy(
            movementType = type
        )

        applyFilters()
    }

    fun filterByProduct(productName: String?) {
        currentFilter = currentFilter.copy(
            productName = productName
                ?.takeIf { it.isNotBlank() }
        )

        applyFilters()
    }

    fun filterByDateRange(
        startDate: Long?,
        endDate: Long?
    ) {
        currentFilter = currentFilter.copy(
            startDate = startDate,
            endDate = endDate
        )

        applyFilters()
    }

    fun clearFilters() {
        currentFilter = MovementHistoryFilter()
        applyFilters()
    }

    private fun applyFilters() {
        val filteredMovements = allMovements.filter { movement ->
            matchesMovementType(movement) &&
                    matchesProduct(movement) &&
                    matchesDateRange(movement)
        }

        val availableProducts = allMovements
            .map { movement ->
                movement.productName
            }
            .distinct()
            .sorted()

        _uiState.value = MovementHistoryUiState.Success(
            movements = filteredMovements,
            availableProducts = availableProducts,
            filter = currentFilter,
            totalMovements = allMovements.size
        )
    }

    private fun matchesMovementType(
        movement: StockMovementWithProduct
    ): Boolean {
        val selectedType = currentFilter.movementType

        return selectedType == null ||
                movement.type == selectedType
    }

    private fun matchesProduct(
        movement: StockMovementWithProduct
    ): Boolean {
        val selectedProduct = currentFilter.productName

        return selectedProduct == null ||
                movement.productName == selectedProduct
    }

    private fun matchesDateRange(
        movement: StockMovementWithProduct
    ): Boolean {
        val startDate = currentFilter.startDate
        val endDate = currentFilter.endDate

        val isAfterStart = startDate == null ||
                movement.createdAt >= startDate

        val isBeforeEnd = endDate == null ||
                movement.createdAt <= endDate

        return isAfterStart && isBeforeEnd
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