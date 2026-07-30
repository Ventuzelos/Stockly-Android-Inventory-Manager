package com.angelapereira.stockly

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.angelapereira.stockly.data.local.Product
import com.angelapereira.stockly.data.local.StocklyDatabase
import com.angelapereira.stockly.data.repository.ProductRepository
import com.angelapereira.stockly.ui.main.MainUiState
import com.angelapereira.stockly.ui.main.MainViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var productNameInputLayout: TextInputLayout
    private lateinit var productCategoryInputLayout: TextInputLayout
    private lateinit var productQuantityInputLayout: TextInputLayout
    private lateinit var productMinimumStockInputLayout: TextInputLayout

    private lateinit var productNameEditText: TextInputEditText
    private lateinit var productCategoryEditText: TextInputEditText
    private lateinit var productQuantityEditText: TextInputEditText
    private lateinit var productMinimumStockEditText: TextInputEditText

    private lateinit var addProductButton: MaterialButton
    private lateinit var viewProductsButton: MaterialButton
    private lateinit var logoutButton: MaterialButton

    private lateinit var productsCountTextView: TextView
    private lateinit var lowStockCountTextView: TextView

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        setupWindowInsets()
        bindViews()
        setupViewModel()
        setupListeners()
        observeInventorySummary()
        observeUiState()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
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
        productNameInputLayout =
            findViewById(R.id.productNameInputLayout)

        productCategoryInputLayout =
            findViewById(R.id.productCategoryInputLayout)

        productQuantityInputLayout =
            findViewById(R.id.productQuantityInputLayout)

        productMinimumStockInputLayout =
            findViewById(R.id.productMinimumStockInputLayout)

        productNameEditText =
            findViewById(R.id.productNameEditText)

        productCategoryEditText =
            findViewById(R.id.productCategoryEditText)

        productQuantityEditText =
            findViewById(R.id.productQuantityEditText)

        productMinimumStockEditText =
            findViewById(R.id.productMinimumStockEditText)

        addProductButton =
            findViewById(R.id.addProductButton)

        viewProductsButton =
            findViewById(R.id.viewProductsButton)

        logoutButton =
            findViewById(R.id.logoutButton)

        productsCountTextView =
            findViewById(R.id.productsCountTextView)

        lowStockCountTextView =
            findViewById(R.id.lowStockCountTextView)
    }

    private fun setupViewModel() {
        val database = StocklyDatabase.getInstance(
            applicationContext
        )

        val repository = ProductRepository(
            productDao = database.productDao()
        )

        val factory = MainViewModel.Factory(repository)

        viewModel = ViewModelProvider(
            this,
            factory
        )[MainViewModel::class.java]
    }

    private fun setupListeners() {
        addProductButton.setOnClickListener {
            addProduct()
        }

        viewProductsButton.setOnClickListener {
            showProducts()
        }

        logoutButton.setOnClickListener {
            confirmLogout()
        }

        productMinimumStockEditText.setOnEditorActionListener {
                _,
                actionId,
                _
            ->

            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addProduct()
                true
            } else {
                false
            }
        }

        productNameEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                productNameInputLayout.error = null
            }
        }

        productCategoryEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                productCategoryInputLayout.error = null
            }
        }

        productQuantityEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                productQuantityInputLayout.error = null
            }
        }

        productMinimumStockEditText.setOnFocusChangeListener {
                _,
                hasFocus
            ->

            if (hasFocus) {
                productMinimumStockInputLayout.error = null
            }
        }
    }

    private fun addProduct() {
        clearFormErrors()

        val name = productNameEditText.text
            ?.toString()
            ?.trim()
            .orEmpty()

        val category = productCategoryEditText.text
            ?.toString()
            ?.trim()
            .orEmpty()

        val quantityText = productQuantityEditText.text
            ?.toString()
            ?.trim()
            .orEmpty()

        val minimumStockText = productMinimumStockEditText.text
            ?.toString()
            ?.trim()
            .orEmpty()

        when {
            name.isBlank() -> {
                productNameInputLayout.error =
                    getString(R.string.product_name_required)

                productNameEditText.requestFocus()
                return
            }

            category.isBlank() -> {
                productCategoryInputLayout.error =
                    getString(R.string.product_category_required)

                productCategoryEditText.requestFocus()
                return
            }

            quantityText.isBlank() -> {
                productQuantityInputLayout.error =
                    getString(R.string.product_quantity_required)

                productQuantityEditText.requestFocus()
                return
            }

            minimumStockText.isBlank() -> {
                productMinimumStockInputLayout.error =
                    getString(R.string.product_minimum_stock_required)

                productMinimumStockEditText.requestFocus()
                return
            }
        }

        val quantity = quantityText.toIntOrNull()

        if (quantity == null || quantity < 0) {
            productQuantityInputLayout.error =
                getString(R.string.product_quantity_invalid)

            productQuantityEditText.requestFocus()
            return
        }

        val minimumStock = minimumStockText.toIntOrNull()

        if (minimumStock == null || minimumStock < 0) {
            productMinimumStockInputLayout.error =
                getString(R.string.product_minimum_stock_invalid)

            productMinimumStockEditText.requestFocus()
            return
        }

        val product = Product(
            name = name,
            category = category,
            quantity = quantity,
            minimumStock = minimumStock
        )

        viewModel.addProduct(product)
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    MainUiState.Idle -> {
                        addProductButton.isEnabled = true
                    }

                    MainUiState.Loading -> {
                        addProductButton.isEnabled = false
                    }

                    is MainUiState.Success -> {
                        addProductButton.isEnabled = true
                        clearForm()

                        Toast.makeText(
                            this@MainActivity,
                            getString(
                                R.string.product_added_successfully,
                                state.productName
                            ),
                            Toast.LENGTH_SHORT
                        ).show()

                        viewModel.resetUiState()
                    }

                    MainUiState.Duplicate -> {
                        addProductButton.isEnabled = true

                        productNameInputLayout.error =
                            getString(R.string.product_already_exists)

                        productNameEditText.requestFocus()
                        viewModel.resetUiState()
                    }

                    is MainUiState.Error -> {
                        addProductButton.isEnabled = true

                        Toast.makeText(
                            this@MainActivity,
                            R.string.product_add_error,
                            Toast.LENGTH_SHORT
                        ).show()

                        viewModel.resetUiState()
                    }
                }
            }
        }
    }

    private fun clearFormErrors() {
        productNameInputLayout.error = null
        productCategoryInputLayout.error = null
        productQuantityInputLayout.error = null
        productMinimumStockInputLayout.error = null
    }

    private fun clearForm() {
        clearFormErrors()

        productNameEditText.text?.clear()
        productCategoryEditText.text?.clear()
        productQuantityEditText.text?.clear()
        productMinimumStockEditText.text?.clear()

        productNameEditText.requestFocus()
    }

    private fun showProducts() {
        startActivity(
            Intent(
                this,
                ProductsActivity::class.java
            )
        )
    }

    private fun observeInventorySummary() {
        lifecycleScope.launch {
            viewModel.productsCount.collect { totalProducts ->
                productsCountTextView.text =
                    resources.getQuantityString(
                        R.plurals.inventory_products_total,
                        totalProducts,
                        totalProducts
                    )
            }
        }

        lifecycleScope.launch {
            viewModel.lowStockCount.collect { lowStockProducts ->
                lowStockCountTextView.text = getString(
                    R.string.inventory_low_stock_dynamic,
                    lowStockProducts
                )
            }
        }
    }

    private fun confirmLogout() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.logout_dialog_title)
            .setMessage(R.string.logout_dialog_message)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_logout) { _, _ ->
                logout()
            }
            .show()
    }

    private fun logout() {
        val intent = Intent(
            this,
            LoginActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        startActivity(intent)
        finish()
    }
}