package com.angelapereira.stockly

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.angelapereira.stockly.data.local.Product
import com.angelapereira.stockly.data.local.StocklyDatabase
import com.angelapereira.stockly.data.repository.ProductRepository
import com.angelapereira.stockly.ui.products.ProductsUiState
import com.angelapereira.stockly.ui.products.ProductsViewModel
import com.angelapereira.stockly.ui.products.ProductStockFilter
import com.angelapereira.stockly.ui.products.ProductSortOption
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import com.google.android.material.button.MaterialButton


class ProductsActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var searchEditText: TextInputEditText
    private lateinit var productsCountLabelTextView: TextView
    private lateinit var productsRecyclerView: RecyclerView
    private lateinit var productsEmptyState: View

    private lateinit var productAdapter: ProductAdapter
    private lateinit var viewModel: ProductsViewModel

    private var currentQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_products)

        setupWindowInsets()
        bindViews()
        setupViewModel()
        setupToolbar()
        setupRecyclerView()
        setupSearch()
        setupListeners()
        observeUiState()

        viewModel.observeProducts(currentQuery)
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.productsMain)
        ) { view, insets ->
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }
    }

    private fun bindViews() {
        toolbar = findViewById(R.id.productsToolbar)
        searchEditText = findViewById(R.id.productSearchEditText)

        productsCountLabelTextView =
            findViewById(R.id.productsCountLabelTextView)

        productsRecyclerView =
            findViewById(R.id.productsRecyclerView)

        productsEmptyState =
            findViewById(R.id.productsEmptyState)
    }

    private fun setupViewModel() {
        val database = StocklyDatabase.getInstance(
            applicationContext
        )

        val repository = ProductRepository(
            productDao = database.productDao()
        )

        val factory = ProductsViewModel.Factory(repository)

        viewModel = ViewModelProvider(
            this,
            factory
        )[ProductsViewModel::class.java]
    }

    private fun setupToolbar() {
        toolbar.setNavigationIcon(
            androidx.appcompat.R.drawable.abc_ic_ab_back_material
        )

        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(
            onMovementClick = { product ->
                openStockMovement(product)
            },
            onEditClick = { product ->
                openEditProduct(product)
            },
            onDeleteClick = { product ->
                confirmDelete(product)
            }
        )

        productsRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(this@ProductsActivity)

            adapter = productAdapter
            setHasFixedSize(true)
        }
    }

    private fun openStockMovement(product: Product) {
        val intent = Intent(
            this,
            StockMovementActivity::class.java
        ).apply {
            putExtra(
                StockMovementActivity.EXTRA_PRODUCT_ID,
                product.id
            )

            putExtra(
                StockMovementActivity.EXTRA_PRODUCT_NAME,
                product.name
            )
        }

        startActivity(intent)
    }

    private fun openEditProduct(product: Product) {
        val intent = Intent(
            this,
            EditProductActivity::class.java
        ).apply {
            putExtra(
                EditProductActivity.EXTRA_PRODUCT_ID,
                product.id
            )
        }

        startActivity(intent)
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(
            object : TextWatcher {

                override fun beforeTextChanged(
                    text: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) = Unit

                override fun onTextChanged(
                    text: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    currentQuery = text
                        ?.toString()
                        ?.trim()
                        .orEmpty()

                    viewModel.observeProducts(currentQuery)
                }

                override fun afterTextChanged(
                    editable: Editable?
                ) = Unit
            }
        )
    }

    private fun setupListeners() {

        findViewById<MaterialButton>(
            R.id.addProductTopButton
        ).setOnClickListener {

            startActivity(
                Intent(
                    this,
                    AddProductActivity::class.java
                )
            )
        }

        findViewById<MaterialButton>(
            R.id.filterProductsButton
        ).setOnClickListener {

            showStockFilterDialog()
        }

        findViewById<MaterialButton>(
            R.id.sortProductsButton
        ).setOnClickListener {

            showSortDialog()
        }
    }

    private fun showStockFilterDialog() {

        val options = arrayOf(
            getString(R.string.filter_all),
            getString(R.string.filter_available),
            getString(R.string.filter_low_stock)
        )

        val currentFilter = viewModel.getCurrentFilter()

        val checkedItem = when (currentFilter) {

            ProductStockFilter.ALL -> 0

            ProductStockFilter.AVAILABLE -> 1

            ProductStockFilter.LOW_STOCK -> 2
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.filter_products_title)
            .setSingleChoiceItems(
                options,
                checkedItem
            ) { dialog, which ->

                val selectedFilter = when (which) {

                    1 -> ProductStockFilter.AVAILABLE

                    2 -> ProductStockFilter.LOW_STOCK

                    else -> ProductStockFilter.ALL
                }

                viewModel.setStockFilter(
                    selectedFilter
                )

                dialog.dismiss()
            }
            .setNegativeButton(
                R.string.action_cancel,
                null
            )
            .show()
    }

    private fun showSortDialog() {

        val options = arrayOf(
            getString(R.string.sort_newest),
            getString(R.string.sort_oldest),
            getString(R.string.sort_name_asc),
            getString(R.string.sort_name_desc),
            getString(R.string.sort_stock_asc),
            getString(R.string.sort_stock_desc)
        )

        val currentSort =
            viewModel.getCurrentSortOption()

        val checkedItem = when (currentSort) {

            ProductSortOption.NEWEST -> 0

            ProductSortOption.OLDEST -> 1

            ProductSortOption.NAME_ASC -> 2

            ProductSortOption.NAME_DESC -> 3

            ProductSortOption.STOCK_ASC -> 4

            ProductSortOption.STOCK_DESC -> 5
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.sort_products_title)
            .setSingleChoiceItems(
                options,
                checkedItem
            ) { dialog, which ->

                val selectedSort =
                    when (which) {

                        1 -> ProductSortOption.OLDEST

                        2 -> ProductSortOption.NAME_ASC

                        3 -> ProductSortOption.NAME_DESC

                        4 -> ProductSortOption.STOCK_ASC

                        5 -> ProductSortOption.STOCK_DESC

                        else -> ProductSortOption.NEWEST
                    }

                viewModel.setSortOption(
                    selectedSort
                )

                dialog.dismiss()
            }
            .setNegativeButton(
                R.string.action_cancel,
                null
            )
            .show()
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    ProductsUiState.Loading -> {
                        showLoadingState()
                    }

                    is ProductsUiState.Success -> {
                        updateProductsList(state.products)
                    }

                    is ProductsUiState.DeleteSuccess -> {
                        Toast.makeText(
                            this@ProductsActivity,
                            getString(
                                R.string.product_deleted_successfully,
                                state.productName
                            ),
                            Toast.LENGTH_SHORT
                        ).show()

                        viewModel.resumeProducts(currentQuery)
                    }

                    ProductsUiState.DeleteError -> {
                        Toast.makeText(
                            this@ProductsActivity,
                            R.string.product_delete_error,
                            Toast.LENGTH_SHORT
                        ).show()

                        viewModel.resumeProducts(currentQuery)
                    }

                    ProductsUiState.Error -> {
                        showErrorState()
                    }
                }
            }
        }
    }

    private fun showLoadingState() {
        productsCountLabelTextView.text =
            getString(R.string.products_loading)

        productsRecyclerView.visibility = View.GONE
        productsEmptyState.visibility = View.GONE
    }

    private fun updateProductsList(products: List<Product>) {
        productAdapter.submitList(products)

        productsCountLabelTextView.text =
            resources.getQuantityString(
                R.plurals.products_count_total,
                products.size,
                products.size
            )

        val hasProducts = products.isNotEmpty()

        productsRecyclerView.visibility = if (hasProducts) {
            View.VISIBLE
        } else {
            View.GONE
        }

        productsEmptyState.visibility = if (hasProducts) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    private fun showErrorState() {
        productsCountLabelTextView.text =
            getString(R.string.products_load_error)

        productsRecyclerView.visibility = View.GONE
        productsEmptyState.visibility = View.VISIBLE
    }

    private fun confirmDelete(product: Product) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_product_dialog_title)
            .setMessage(
                getString(
                    R.string.delete_product_dialog_message,
                    product.name
                )
            )
            .setNegativeButton(
                R.string.action_cancel,
                null
            )
            .setPositiveButton(
                R.string.action_delete
            ) { _, _ ->
                viewModel.deleteProduct(product)
            }
            .show()
    }
}