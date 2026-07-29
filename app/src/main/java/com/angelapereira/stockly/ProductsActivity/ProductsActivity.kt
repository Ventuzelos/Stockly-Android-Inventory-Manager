package com.angelapereira.stockly

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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.angelapereira.stockly.data.local.Product
import com.angelapereira.stockly.data.local.ProductDao
import com.angelapereira.stockly.data.local.StocklyDatabase
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProductsActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var searchEditText: TextInputEditText
    private lateinit var productsCountLabelTextView: TextView
    private lateinit var productsRecyclerView: RecyclerView
    private lateinit var productsEmptyState: View

    private lateinit var productDao: ProductDao
    private lateinit var productAdapter: ProductAdapter

    private var productsJob: Job? = null
    private var currentQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_products)

        setupWindowInsets()
        bindViews()
        setupDatabase()
        setupToolbar()
        setupRecyclerView()
        setupSearch()
        observeProducts()
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
        productsRecyclerView = findViewById(R.id.productsRecyclerView)
        productsEmptyState = findViewById(R.id.productsEmptyState)
    }

    private fun setupDatabase() {
        productDao = StocklyDatabase
            .getInstance(applicationContext)
            .productDao()
    }

    private fun setupToolbar() {
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)

        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(
            onEditClick = { product ->
                showEditNotAvailableMessage(product)
            },
            onDeleteClick = { product ->
                confirmDelete(product)
            }
        )

        productsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ProductsActivity)
            adapter = productAdapter
            setHasFixedSize(true)
        }
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

                    observeProducts()
                }

                override fun afterTextChanged(
                    editable: Editable?
                ) = Unit
            }
        )
    }

    private fun observeProducts() {
        productsJob?.cancel()

        productsJob = lifecycleScope.launch {
            val productsFlow = if (currentQuery.isBlank()) {
                productDao.getAllProducts()
            } else {
                productDao.searchProducts(currentQuery)
            }

            productsFlow.collectLatest { products ->
                updateProductsList(products)
            }
        }
    }

    private fun updateProductsList(products: List<Product>) {
        productAdapter.submitList(products)

        productsCountLabelTextView.text = resources.getQuantityString(
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

    private fun confirmDelete(product: Product) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_product_dialog_title)
            .setMessage(
                getString(
                    R.string.delete_product_dialog_message,
                    product.name
                )
            )
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                deleteProduct(product)
            }
            .show()
    }

    private fun deleteProduct(product: Product) {
        lifecycleScope.launch {
            try {
                productDao.delete(product)

                Toast.makeText(
                    this@ProductsActivity,
                    getString(
                        R.string.product_deleted_successfully,
                        product.name
                    ),
                    Toast.LENGTH_SHORT
                ).show()
            } catch (exception: Exception) {
                Toast.makeText(
                    this@ProductsActivity,
                    R.string.product_delete_error,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showEditNotAvailableMessage(product: Product) {
        Toast.makeText(
            this,
            getString(
                R.string.product_edit_not_available,
                product.name
            ),
            Toast.LENGTH_SHORT
        ).show()
    }
}