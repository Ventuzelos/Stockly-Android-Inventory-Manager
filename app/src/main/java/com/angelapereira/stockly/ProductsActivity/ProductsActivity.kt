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
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

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