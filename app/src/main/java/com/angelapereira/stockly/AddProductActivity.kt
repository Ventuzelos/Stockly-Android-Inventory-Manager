package com.angelapereira.stockly

import android.os.Bundle
import android.view.inputmethod.EditorInfo
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
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class AddProductActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar

    private lateinit var productNameInputLayout: TextInputLayout
    private lateinit var productCategoryInputLayout: TextInputLayout
    private lateinit var productQuantityInputLayout: TextInputLayout
    private lateinit var productPriceInputLayout: TextInputLayout
    private lateinit var productMinimumStockInputLayout: TextInputLayout

    private lateinit var productNameEditText: TextInputEditText
    private lateinit var productCategoryEditText: TextInputEditText
    private lateinit var productQuantityEditText: TextInputEditText
    private lateinit var productPriceEditText: TextInputEditText
    private lateinit var productMinimumStockEditText: TextInputEditText

    private lateinit var saveNewProductButton: MaterialButton
    private lateinit var cancelAddProductButton: MaterialButton

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_add_product)

        setupWindowInsets()
        bindViews()
        setupViewModel()
        setupToolbar()
        setupListeners()
        restoreForm(savedInstanceState)
        observeUiState()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.addProductMain)
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

        toolbar = findViewById(R.id.addProductToolbar)

        productNameInputLayout =
            findViewById(R.id.addProductNameInputLayout)

        productCategoryInputLayout =
            findViewById(R.id.addProductCategoryInputLayout)

        productQuantityInputLayout =
            findViewById(R.id.addProductQuantityInputLayout)

        productPriceInputLayout =
            findViewById(R.id.addProductPriceInputLayout)

        productMinimumStockInputLayout =
            findViewById(R.id.addProductMinimumStockInputLayout)

        productNameEditText =
            findViewById(R.id.addProductNameEditText)

        productCategoryEditText =
            findViewById(R.id.addProductCategoryEditText)

        productQuantityEditText =
            findViewById(R.id.addProductQuantityEditText)

        productPriceEditText =
            findViewById(R.id.addProductPriceEditText)

        productMinimumStockEditText =
            findViewById(R.id.addProductMinimumStockEditText)

        saveNewProductButton =
            findViewById(R.id.saveNewProductButton)

        cancelAddProductButton =
            findViewById(R.id.cancelAddProductButton)
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

    private fun setupToolbar() {

        toolbar.setNavigationIcon(
            androidx.appcompat.R.drawable.abc_ic_ab_back_material
        )

        toolbar.setNavigationOnClickListener {
            saveFormState()
            finish()
        }
    }

    private fun setupListeners() {

        saveNewProductButton.setOnClickListener {
            addProduct()
        }

        cancelAddProductButton.setOnClickListener {
            saveFormState()
            finish()
        }

        productPriceEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                productPriceInputLayout.error = null
            }
        }

        productMinimumStockEditText.setOnEditorActionListener {
                _,
                actionId,
                _ ->

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
                hasFocus ->

            if (hasFocus) {
                productMinimumStockInputLayout.error = null
            }
        }
    }

    private fun saveFormState() {

        val preferences = getSharedPreferences(
            "add_product_form",
            MODE_PRIVATE
        )

        preferences.edit()
            .putString(
                "name",
                productNameEditText.text?.toString().orEmpty()
            )
            .putString(
                "category",
                productCategoryEditText.text?.toString().orEmpty()
            )
            .putString(
                "quantity",
                productQuantityEditText.text?.toString().orEmpty()
            )
            .putString(
                "price",
                productPriceEditText.text?.toString().orEmpty()
            )
            .putString(
                "minimum_stock",
                productMinimumStockEditText.text?.toString().orEmpty()
            )
            .apply()
    }

    private fun restoreForm(savedInstanceState: Bundle?) {

        val preferences = getSharedPreferences(
            "add_product_form",
            MODE_PRIVATE
        )

        val name = savedInstanceState?.getString("product_name")
            ?: preferences.getString("name", "")

        val category = savedInstanceState?.getString("product_category")
            ?: preferences.getString("category", "")

        val quantity = savedInstanceState?.getString("product_quantity")
            ?: preferences.getString("quantity", "")

        val price = savedInstanceState?.getString("product_price")
            ?: preferences.getString("price", "")

        val minimumStock =
            savedInstanceState?.getString("product_minimum_stock")
                ?: preferences.getString("minimum_stock", "")

        productNameEditText.setText(name)
        productCategoryEditText.setText(category)
        productQuantityEditText.setText(quantity)
        productPriceEditText.setText(price)
        productMinimumStockEditText.setText(minimumStock)
    }

    override fun onSaveInstanceState(outState: Bundle) {

        outState.putString(
            "product_name",
            productNameEditText.text?.toString()
        )

        outState.putString(
            "product_category",
            productCategoryEditText.text?.toString()
        )

        outState.putString(
            "product_quantity",
            productQuantityEditText.text?.toString()
        )

        outState.putString(
            "product_price",
            productPriceEditText.text?.toString()
        )

        outState.putString(
            "product_minimum_stock",
            productMinimumStockEditText.text?.toString()
        )

        super.onSaveInstanceState(outState)
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

        val priceText = productPriceEditText.text
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

            priceText.isBlank() -> {
                productPriceInputLayout.error =
                    getString(R.string.product_price_required)

                productPriceEditText.requestFocus()
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

        val price = priceText
            .replace(",", ".")
            .toDoubleOrNull()

        if (price == null || price < 0) {

            productPriceInputLayout.error =
                getString(R.string.product_price_invalid)

            productPriceEditText.requestFocus()
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
            minimumStock = minimumStock,
            price = price
        )

        viewModel.addProduct(product)
    }

    private fun observeUiState() {

        lifecycleScope.launch {

            viewModel.uiState.collect { state ->

                when (state) {

                    MainUiState.Idle -> {
                        setLoadingState(false)
                    }

                    MainUiState.Loading -> {
                        setLoadingState(true)
                    }

                    is MainUiState.Success -> {

                        setLoadingState(false)

                        Toast.makeText(
                            this@AddProductActivity,
                            getString(
                                R.string.product_added_successfully,
                                state.productName
                            ),
                            Toast.LENGTH_SHORT
                        ).show()

                        getSharedPreferences(
                            "add_product_form",
                            MODE_PRIVATE
                        ).edit()
                            .clear()
                            .apply()

                        setResult(RESULT_OK)
                        finish()
                    }

                    MainUiState.Duplicate -> {

                        setLoadingState(false)

                        productNameInputLayout.error =
                            getString(
                                R.string.product_already_exists
                            )

                        productNameEditText.requestFocus()

                        viewModel.resetUiState()
                    }

                    is MainUiState.Error -> {

                        setLoadingState(false)

                        Toast.makeText(
                            this@AddProductActivity,
                            R.string.product_add_error,
                            Toast.LENGTH_SHORT
                        ).show()

                        viewModel.resetUiState()
                    }
                }
            }
        }
    }

    private fun setLoadingState(isLoading: Boolean) {

        saveNewProductButton.isEnabled = !isLoading
        cancelAddProductButton.isEnabled = !isLoading

        productNameEditText.isEnabled = !isLoading
        productCategoryEditText.isEnabled = !isLoading
        productQuantityEditText.isEnabled = !isLoading
        productMinimumStockEditText.isEnabled = !isLoading
        productPriceEditText.isEnabled = !isLoading
    }

    private fun clearFormErrors() {

        productNameInputLayout.error = null
        productCategoryInputLayout.error = null
        productQuantityInputLayout.error = null
        productMinimumStockInputLayout.error = null
        productPriceInputLayout.error = null
    }
}