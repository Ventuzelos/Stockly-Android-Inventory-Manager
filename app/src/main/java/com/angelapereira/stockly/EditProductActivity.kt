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
import com.angelapereira.stockly.ui.editproduct.EditProductUiState
import com.angelapereira.stockly.ui.editproduct.EditProductViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class EditProductActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar

    private lateinit var nameInputLayout: TextInputLayout
    private lateinit var categoryInputLayout: TextInputLayout
    private lateinit var quantityInputLayout: TextInputLayout
    private lateinit var minimumStockInputLayout: TextInputLayout

    private lateinit var nameEditText: TextInputEditText
    private lateinit var categoryEditText: TextInputEditText
    private lateinit var quantityEditText: TextInputEditText
    private lateinit var minimumStockEditText: TextInputEditText

    private lateinit var saveButton: MaterialButton
    private lateinit var cancelButton: MaterialButton

    private lateinit var viewModel: EditProductViewModel

    private var currentProduct: Product? = null
    private var productId: Int = INVALID_PRODUCT_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_product)

        setupWindowInsets()
        bindViews()
        setupViewModel()
        setupToolbar()
        setupListeners()
        observeUiState()
        readProductId()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.editProductMain)
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
        toolbar = findViewById(R.id.editProductToolbar)

        nameInputLayout =
            findViewById(R.id.editProductNameInputLayout)

        categoryInputLayout =
            findViewById(R.id.editProductCategoryInputLayout)

        quantityInputLayout =
            findViewById(R.id.editProductQuantityInputLayout)

        minimumStockInputLayout =
            findViewById(R.id.editProductMinimumStockInputLayout)

        nameEditText =
            findViewById(R.id.editProductNameEditText)

        categoryEditText =
            findViewById(R.id.editProductCategoryEditText)

        quantityEditText =
            findViewById(R.id.editProductQuantityEditText)

        minimumStockEditText =
            findViewById(R.id.editProductMinimumStockEditText)

        saveButton =
            findViewById(R.id.saveProductChangesButton)

        cancelButton =
            findViewById(R.id.cancelEditProductButton)
    }

    private fun setupViewModel() {
        val database = StocklyDatabase.getInstance(
            applicationContext
        )

        val repository = ProductRepository(
            productDao = database.productDao()
        )

        val factory = EditProductViewModel.Factory(repository)

        viewModel = ViewModelProvider(
            this,
            factory
        )[EditProductViewModel::class.java]
    }

    private fun setupToolbar() {
        toolbar.setNavigationIcon(
            androidx.appcompat.R.drawable.abc_ic_ab_back_material
        )

        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupListeners() {
        saveButton.setOnClickListener {
            updateProduct()
        }

        cancelButton.setOnClickListener {
            finish()
        }

        minimumStockEditText.setOnEditorActionListener {
                _,
                actionId,
                _
            ->

            if (actionId == EditorInfo.IME_ACTION_DONE) {
                updateProduct()
                true
            } else {
                false
            }
        }

        nameEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                nameInputLayout.error = null
            }
        }

        categoryEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                categoryInputLayout.error = null
            }
        }

        quantityEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                quantityInputLayout.error = null
            }
        }

        minimumStockEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                minimumStockInputLayout.error = null
            }
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    EditProductUiState.Idle -> {
                        setLoadingState(false)
                    }

                    EditProductUiState.Loading -> {
                        setLoadingState(true)
                    }

                    is EditProductUiState.ProductLoaded -> {
                        setLoadingState(false)
                        currentProduct = state.product
                        fillForm(state.product)
                        viewModel.resetUiState()
                    }

                    is EditProductUiState.UpdateSuccess -> {
                        setLoadingState(false)

                        Toast.makeText(
                            this@EditProductActivity,
                            getString(
                                R.string.product_updated_successfully,
                                state.productName
                            ),
                            Toast.LENGTH_SHORT
                        ).show()

                        finish()
                    }

                    EditProductUiState.DuplicateName -> {
                        setLoadingState(false)

                        nameInputLayout.error =
                            getString(
                                R.string.product_name_used_by_another
                            )

                        nameEditText.requestFocus()
                        viewModel.resetUiState()
                    }

                    EditProductUiState.ProductNotFound -> {
                        setLoadingState(false)
                        showProductNotFound()
                    }

                    EditProductUiState.Error -> {
                        setLoadingState(false)

                        Toast.makeText(
                            this@EditProductActivity,
                            R.string.product_update_error,
                            Toast.LENGTH_SHORT
                        ).show()

                        viewModel.resetUiState()
                    }
                }
            }
        }
    }

    private fun readProductId() {
        productId = intent.getIntExtra(
            EXTRA_PRODUCT_ID,
            INVALID_PRODUCT_ID
        )

        if (productId == INVALID_PRODUCT_ID) {
            showProductNotFound()
            return
        }

        viewModel.loadProduct(productId)
    }

    private fun fillForm(product: Product) {
        nameEditText.setText(product.name)
        categoryEditText.setText(product.category)
        quantityEditText.setText(product.quantity.toString())
        minimumStockEditText.setText(product.minimumStock.toString())
    }

    private fun updateProduct() {
        clearErrors()

        val originalProduct = currentProduct ?: return

        val name = nameEditText.text
            ?.toString()
            ?.trim()
            .orEmpty()

        val category = categoryEditText.text
            ?.toString()
            ?.trim()
            .orEmpty()

        val quantityText = quantityEditText.text
            ?.toString()
            ?.trim()
            .orEmpty()

        val minimumStockText = minimumStockEditText.text
            ?.toString()
            ?.trim()
            .orEmpty()

        when {
            name.isBlank() -> {
                nameInputLayout.error =
                    getString(R.string.product_name_required)

                nameEditText.requestFocus()
                return
            }

            category.isBlank() -> {
                categoryInputLayout.error =
                    getString(R.string.product_category_required)

                categoryEditText.requestFocus()
                return
            }

            quantityText.isBlank() -> {
                quantityInputLayout.error =
                    getString(R.string.product_quantity_required)

                quantityEditText.requestFocus()
                return
            }

            minimumStockText.isBlank() -> {
                minimumStockInputLayout.error =
                    getString(R.string.product_minimum_stock_required)

                minimumStockEditText.requestFocus()
                return
            }
        }

        val quantity = quantityText.toIntOrNull()

        if (quantity == null || quantity < 0) {
            quantityInputLayout.error =
                getString(R.string.product_quantity_invalid)

            quantityEditText.requestFocus()
            return
        }

        val minimumStock = minimumStockText.toIntOrNull()

        if (minimumStock == null || minimumStock < 0) {
            minimumStockInputLayout.error =
                getString(R.string.product_minimum_stock_invalid)

            minimumStockEditText.requestFocus()
            return
        }

        val updatedProduct = originalProduct.copy(
            name = name,
            category = category,
            quantity = quantity,
            minimumStock = minimumStock
        )

        viewModel.updateProduct(updatedProduct)
    }

    private fun setLoadingState(isLoading: Boolean) {
        saveButton.isEnabled = !isLoading
        cancelButton.isEnabled = !isLoading

        nameEditText.isEnabled = !isLoading
        categoryEditText.isEnabled = !isLoading
        quantityEditText.isEnabled = !isLoading
        minimumStockEditText.isEnabled = !isLoading
    }

    private fun clearErrors() {
        nameInputLayout.error = null
        categoryInputLayout.error = null
        quantityInputLayout.error = null
        minimumStockInputLayout.error = null
    }

    private fun showProductNotFound() {
        Toast.makeText(
            this,
            R.string.product_not_found,
            Toast.LENGTH_SHORT
        ).show()

        finish()
    }

    companion object {
        const val EXTRA_PRODUCT_ID = "extra_product_id"
        private const val INVALID_PRODUCT_ID = -1
    }
}