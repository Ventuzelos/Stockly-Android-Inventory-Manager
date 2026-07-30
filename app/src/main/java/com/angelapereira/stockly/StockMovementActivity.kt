package com.angelapereira.stockly

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
import com.angelapereira.stockly.data.local.MovementType
import com.angelapereira.stockly.data.local.StockMovement
import com.angelapereira.stockly.data.local.StocklyDatabase
import com.angelapereira.stockly.data.repository.StockMovementRepository
import com.angelapereira.stockly.ui.stockmovement.StockMovementUiState
import com.angelapereira.stockly.ui.stockmovement.StockMovementViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class StockMovementActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var productTextView: TextView
    private lateinit var movementTypeToggleGroup: MaterialButtonToggleGroup
    private lateinit var quantityInputLayout: TextInputLayout
    private lateinit var quantityEditText: TextInputEditText
    private lateinit var registerButton: MaterialButton
    private lateinit var cancelButton: MaterialButton

    private lateinit var viewModel: StockMovementViewModel

    private var productId = INVALID_PRODUCT_ID
    private var productName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_stock_movement)

        setupWindowInsets()
        bindViews()
        setupViewModel()
        setupToolbar()
        setupListeners()
        observeUiState()
        readProductData()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.stockMovementMain)
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
        toolbar = findViewById(R.id.stockMovementToolbar)
        productTextView = findViewById(R.id.stockMovementProductTextView)
        movementTypeToggleGroup =
            findViewById(R.id.movementTypeToggleGroup)
        quantityInputLayout =
            findViewById(R.id.movementQuantityInputLayout)
        quantityEditText =
            findViewById(R.id.movementQuantityEditText)
        registerButton =
            findViewById(R.id.registerMovementButton)
        cancelButton =
            findViewById(R.id.cancelMovementButton)
    }

    private fun setupViewModel() {
        val database = StocklyDatabase.getInstance(applicationContext)

        val repository = StockMovementRepository(
            stockMovementDao = database.stockMovementDao(),
            productDao = database.productDao()
        )

        val factory = StockMovementViewModel.Factory(repository)

        viewModel = ViewModelProvider(
            this,
            factory
        )[StockMovementViewModel::class.java]
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
        registerButton.setOnClickListener {
            registerMovement()
        }

        cancelButton.setOnClickListener {
            finish()
        }

        quantityEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                registerMovement()
                true
            } else {
                false
            }
        }

        quantityEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                quantityInputLayout.error = null
            }
        }
    }

    private fun readProductData() {
        productId = intent.getIntExtra(
            EXTRA_PRODUCT_ID,
            INVALID_PRODUCT_ID
        )

        productName = intent.getStringExtra(EXTRA_PRODUCT_NAME).orEmpty()

        if (productId == INVALID_PRODUCT_ID || productName.isBlank()) {
            Toast.makeText(
                this,
                R.string.stock_movement_product_not_found,
                Toast.LENGTH_SHORT
            ).show()

            finish()
            return
        }

        productTextView.text = getString(
            R.string.stock_movement_product,
            productName
        )
    }

    private fun registerMovement() {
        quantityInputLayout.error = null

        val quantityText = quantityEditText.text
            ?.toString()
            ?.trim()
            .orEmpty()

        if (quantityText.isBlank()) {
            quantityInputLayout.error =
                getString(R.string.stock_movement_quantity_required)

            quantityEditText.requestFocus()
            return
        }

        val quantity = quantityText.toIntOrNull()

        if (quantity == null || quantity <= 0) {
            quantityInputLayout.error =
                getString(R.string.stock_movement_quantity_invalid)

            quantityEditText.requestFocus()
            return
        }

        val movementType = when (
            movementTypeToggleGroup.checkedButtonId
        ) {
            R.id.exitMovementButton -> MovementType.EXIT
            else -> MovementType.ENTRY
        }

        val movement = StockMovement(
            productId = productId,
            type = movementType,
            quantity = quantity
        )

        viewModel.registerMovement(movement)
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    StockMovementUiState.Idle -> {
                        setLoadingState(false)
                    }

                    StockMovementUiState.Loading -> {
                        setLoadingState(true)
                    }

                    StockMovementUiState.Success -> {
                        setLoadingState(false)

                        Toast.makeText(
                            this@StockMovementActivity,
                            R.string.stock_movement_success,
                            Toast.LENGTH_SHORT
                        ).show()

                        finish()
                    }

                    StockMovementUiState.InsufficientStock -> {
                        setLoadingState(false)

                        quantityInputLayout.error = getString(
                            R.string.stock_movement_insufficient_stock
                        )

                        quantityEditText.requestFocus()
                        viewModel.resetUiState()
                    }

                    StockMovementUiState.ProductNotFound -> {
                        setLoadingState(false)

                        Toast.makeText(
                            this@StockMovementActivity,
                            R.string.stock_movement_product_not_found,
                            Toast.LENGTH_SHORT
                        ).show()

                        finish()
                    }

                    StockMovementUiState.InvalidQuantity -> {
                        setLoadingState(false)

                        quantityInputLayout.error = getString(
                            R.string.stock_movement_quantity_invalid
                        )

                        quantityEditText.requestFocus()
                        viewModel.resetUiState()
                    }

                    StockMovementUiState.Error -> {
                        setLoadingState(false)

                        Toast.makeText(
                            this@StockMovementActivity,
                            R.string.stock_movement_error,
                            Toast.LENGTH_SHORT
                        ).show()

                        viewModel.resetUiState()
                    }
                }
            }
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        registerButton.isEnabled = !isLoading
        cancelButton.isEnabled = !isLoading
        quantityEditText.isEnabled = !isLoading
        movementTypeToggleGroup.isEnabled = !isLoading
    }

    companion object {
        const val EXTRA_PRODUCT_ID = "extra_product_id"
        const val EXTRA_PRODUCT_NAME = "extra_product_name"

        private const val INVALID_PRODUCT_ID = -1
    }
}