package com.angelapereira.stockly.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.angelapereira.stockly.data.local.Product
import com.angelapereira.stockly.data.repository.ProductRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class ProductStockFilter {
    ALL,
    AVAILABLE,
    LOW_STOCK
}

enum class ProductSortOption {
    NEWEST,
    OLDEST,
    NAME_ASC,
    NAME_DESC,
    STOCK_ASC,
    STOCK_DESC
}

class ProductsViewModel(
    private val repository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductsUiState>(
        ProductsUiState.Loading
    )

    val uiState: StateFlow<ProductsUiState> =
        _uiState.asStateFlow()

    private var productsJob: Job? = null

    private var currentQuery = ""

    private var currentFilter = ProductStockFilter.ALL

    private var currentSort = ProductSortOption.NEWEST

    fun observeProducts(query: String) {

        currentQuery = query

        productsJob?.cancel()

        productsJob = viewModelScope.launch {

            _uiState.value = ProductsUiState.Loading

            try {

                val productsFlow = if (query.isBlank()) {
                    repository.getAllProducts()
                } else {
                    repository.searchProducts(query)
                }

                productsFlow.collectLatest { products ->

                    val filteredProducts =
                        when (currentFilter) {

                            ProductStockFilter.ALL -> {
                                products
                            }

                            ProductStockFilter.AVAILABLE -> {
                                products.filter {
                                    !it.isLowStock
                                }
                            }

                            ProductStockFilter.LOW_STOCK -> {
                                products.filter {
                                    it.isLowStock
                                }
                            }
                        }

                    val sortedProducts =
                        when (currentSort) {

                            ProductSortOption.NEWEST -> {
                                filteredProducts.sortedByDescending {
                                    it.createdAt
                                }
                            }

                            ProductSortOption.OLDEST -> {
                                filteredProducts.sortedBy {
                                    it.createdAt
                                }
                            }

                            ProductSortOption.NAME_ASC -> {
                                filteredProducts.sortedBy {
                                    it.name.lowercase()
                                }
                            }

                            ProductSortOption.NAME_DESC -> {
                                filteredProducts.sortedByDescending {
                                    it.name.lowercase()
                                }
                            }

                            ProductSortOption.STOCK_ASC -> {
                                filteredProducts.sortedBy {
                                    it.quantity
                                }
                            }

                            ProductSortOption.STOCK_DESC -> {
                                filteredProducts.sortedByDescending {
                                    it.quantity
                                }
                            }
                        }

                    _uiState.value =
                        ProductsUiState.Success(
                            sortedProducts
                        )
                }

            } catch (exception: Exception) {

                _uiState.value =
                    ProductsUiState.Error
            }
        }
    }

    fun setStockFilter(
        filter: ProductStockFilter
    ) {

        currentFilter = filter

        observeProducts(currentQuery)
    }

    fun getCurrentFilter(): ProductStockFilter {
        return currentFilter
    }

    fun setSortOption(
        sortOption: ProductSortOption
    ) {

        currentSort = sortOption

        observeProducts(currentQuery)
    }

    fun getCurrentSortOption(): ProductSortOption {
        return currentSort
    }

    fun deleteProduct(product: Product) {

        viewModelScope.launch {

            try {

                repository.deleteProduct(product)

                _uiState.value =
                    ProductsUiState.DeleteSuccess(
                        productName = product.name
                    )

            } catch (exception: Exception) {

                _uiState.value =
                    ProductsUiState.DeleteError
            }
        }
    }

    fun resumeProducts(query: String) {
        observeProducts(query)
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
                    ProductsViewModel::class.java
                )
            ) {
                return ProductsViewModel(
                    repository
                ) as T
            }

            throw IllegalArgumentException(
                "Unknown ViewModel class: ${modelClass.name}"
            )
        }
    }
}