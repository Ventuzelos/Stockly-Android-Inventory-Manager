package com.angelapereira.stockly

import android.content.res.ColorStateList
import android.graphics.Color
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
import com.angelapereira.stockly.data.local.Product
import com.angelapereira.stockly.data.local.StockMovement
import com.angelapereira.stockly.data.local.StocklyDatabase
import com.angelapereira.stockly.data.repository.StockMovementRepository
import com.angelapereira.stockly.ui.stockmovement.StockMovementUiState
import com.angelapereira.stockly.ui.stockmovement.StockMovementViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class StockMovementActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar

    private lateinit var productNameTextView: TextView
    private lateinit var productCategoryTextView: TextView
    private lateinit var currentStockTextView: TextView

    private lateinit var entryMovementButton: MaterialButton
    private lateinit var exitMovementButton: MaterialButton

    private lateinit var quantityInputLayout: TextInputLayout
    private lateinit var quantityEditText: TextInputEditText

    private lateinit var registerButton: MaterialButton
    private lateinit var cancelButton: MaterialButton

    private lateinit var viewModel: StockMovementViewModel

    private var productId = INVALID_PRODUCT_ID
    private var productName = ""

    private var selectedMovementType = MovementType.ENTRY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(
            R.layout.activity_stock_movement
        )

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

        toolbar = findViewById(
            R.id.stockMovementToolbar
        )

        productNameTextView = findViewById(
            R.id.stockMovementProductNameTextView
        )

        productCategoryTextView = findViewById(
            R.id.stockMovementProductCategoryTextView
        )

        currentStockTextView = findViewById(
            R.id.stockMovementCurrentStockTextView
        )

        entryMovementButton = findViewById(
            R.id.entryMovementButton
        )

        exitMovementButton = findViewById(
            R.id.exitMovementButton
        )

        quantityInputLayout = findViewById(
            R.id.movementQuantityInputLayout
        )

        quantityEditText = findViewById(
            R.id.movementQuantityEditText
        )

        registerButton = findViewById(
            R.id.registerMovementButton
        )

        cancelButton = findViewById(
            R.id.cancelMovementButton
        )
    }

    private fun setupViewModel() {

        val database =
            StocklyDatabase.getInstance(
                applicationContext
            )

        val repository =
            StockMovementRepository(
                stockMovementDao = database.stockMovementDao(),
                productDao = database.productDao()
            )

        val factory =
            StockMovementViewModel.Factory(
                repository
            )

        viewModel =
            ViewModelProvider(
                this,
                factory
            )[StockMovementViewModel::class.java]
    }

    private fun setupToolbar() {

        toolbar.setNavigationIcon(
            androidx.appcompat.R.drawable.abc_ic_ab_back_material
        )

        toolbar.setNavigationContentDescription(
            R.string.action_back
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

        entryMovementButton.setOnClickListener {

            selectedMovementType =
                MovementType.ENTRY

            updateMovementTypeSelection()
        }

        exitMovementButton.setOnClickListener {

            selectedMovementType =
                MovementType.EXIT

            updateMovementTypeSelection()
        }

        quantityEditText.setOnEditorActionListener {
                _,
                actionId,
                _ ->

            if (
                actionId ==
                EditorInfo.IME_ACTION_DONE
            ) {
                registerMovement()
                true
            } else {
                false
            }
        }

        quantityEditText.setOnFocusChangeListener {
                _,
                hasFocus ->

            if (hasFocus) {
                quantityInputLayout.error = null
            }
        }

        updateMovementTypeSelection()
    }

    private fun updateMovementTypeSelection() {

        val isEntry =
            selectedMovementType == MovementType.ENTRY

        val entrySelectedBackground =
            ColorStateList.valueOf(
                Color.parseColor("#ECFDF5")
            )

        val exitSelectedBackground =
            ColorStateList.valueOf(
                Color.parseColor("#FFF7ED")
            )

        val unselectedBackground =
            ColorStateList.valueOf(
                Color.WHITE
            )

        val entrySelectedStroke =
            ColorStateList.valueOf(
                Color.parseColor("#A7E3C3")
            )

        val exitSelectedStroke =
            ColorStateList.valueOf(
                Color.parseColor("#FED7AA")
            )

        val unselectedStroke =
            ColorStateList.valueOf(
                Color.parseColor("#D1D5DB")
            )



        entryMovementButton.backgroundTintList =
            if (isEntry) {
                entrySelectedBackground
            } else {
                unselectedBackground
            }

        entryMovementButton.strokeColor =
            if (isEntry) {
                entrySelectedStroke
            } else {
                unselectedStroke
            }

        entryMovementButton.setTextColor(
            if (isEntry) {
                Color.parseColor("#15803D")
            } else {
                Color.parseColor("#111827")
            }
        )

        entryMovementButton.iconTint =
            ColorStateList.valueOf(
                if (isEntry) {
                    Color.parseColor("#16A34A")
                } else {
                    Color.parseColor("#6B7280")
                }
            )



        exitMovementButton.backgroundTintList =
            if (isEntry) {
                unselectedBackground
            } else {
                exitSelectedBackground
            }

        exitMovementButton.strokeColor =
            if (isEntry) {
                unselectedStroke
            } else {
                exitSelectedStroke
            }

        exitMovementButton.setTextColor(
            if (isEntry) {
                Color.parseColor("#111827")
            } else {
                Color.parseColor("#C2410C")
            }
        )

        exitMovementButton.iconTint =
            ColorStateList.valueOf(
                if (isEntry) {
                    Color.parseColor("#F59E0B")
                } else {
                    Color.parseColor("#F59E0B")
                }
            )
    }
    private fun readProductData() {

        productId = intent.getIntExtra(
            EXTRA_PRODUCT_ID,
            INVALID_PRODUCT_ID
        )

        productName =
            intent.getStringExtra(
                EXTRA_PRODUCT_NAME
            ).orEmpty()

        if (
            productId == INVALID_PRODUCT_ID ||
            productName.isBlank()
        ) {

            Toast.makeText(
                this,
                R.string.stock_movement_product_not_found,
                Toast.LENGTH_SHORT
            ).show()

            finish()
            return
        }

        productNameTextView.text =
            productName

        loadProductDetails()
    }

    private fun loadProductDetails() {

        lifecycleScope.launch {

            val database =
                StocklyDatabase.getInstance(
                    applicationContext
                )

            val product: Product? =
                database.productDao()
                    .getProductById(productId)

            if (product != null) {

                productNameTextView.text =
                    product.name

                productCategoryTextView.text =
                    product.category

                currentStockTextView.text =
                    getString(
                        R.string.stock_movement_current_stock,
                        product.quantity
                    )
            }
        }
    }

    private fun registerMovement() {

        quantityInputLayout.error = null

        val quantityText =
            quantityEditText.text
                ?.toString()
                ?.trim()
                .orEmpty()

        if (quantityText.isBlank()) {

            quantityInputLayout.error =
                getString(
                    R.string.stock_movement_quantity_required
                )

            quantityEditText.requestFocus()
            return
        }

        val quantity =
            quantityText.toIntOrNull()

        if (
            quantity == null ||
            quantity <= 0
        ) {

            quantityInputLayout.error =
                getString(
                    R.string.stock_movement_quantity_invalid
                )

            quantityEditText.requestFocus()
            return
        }

        val movement =
            StockMovement(
                productId = productId,
                type = selectedMovementType,
                quantity = quantity
            )

        viewModel.registerMovement(
            movement
        )
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

                        quantityInputLayout.error =
                            getString(
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

                        quantityInputLayout.error =
                            getString(
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

    private fun setLoadingState(
        isLoading: Boolean
    ) {

        registerButton.isEnabled =
            !isLoading

        cancelButton.isEnabled =
            !isLoading

        quantityEditText.isEnabled =
            !isLoading

        entryMovementButton.isEnabled =
            !isLoading

        exitMovementButton.isEnabled =
            !isLoading
    }

    companion object {

        const val EXTRA_PRODUCT_ID =
            "extra_product_id"

        const val EXTRA_PRODUCT_NAME =
            "extra_product_name"

        private const val INVALID_PRODUCT_ID =
            -1
    }
}