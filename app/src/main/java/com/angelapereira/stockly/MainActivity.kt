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
import androidx.lifecycle.lifecycleScope
import com.angelapereira.stockly.data.local.Product
import com.angelapereira.stockly.data.local.ProductDao
import com.angelapereira.stockly.data.local.StocklyDatabase
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var productNameInputLayout: TextInputLayout
    private lateinit var productNameEditText: TextInputEditText
    private lateinit var addProductButton: MaterialButton
    private lateinit var viewProductsButton: MaterialButton
    private lateinit var logoutButton: MaterialButton
    private lateinit var productsCountTextView: TextView
    private lateinit var lowStockCountTextView: TextView

    private lateinit var productDao: ProductDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        setupWindowInsets()
        bindViews()
        setupDatabase()
        setupListeners()
        observeInventorySummary()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

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
        productNameInputLayout = findViewById(R.id.productNameInputLayout)
        productNameEditText = findViewById(R.id.productNameEditText)
        addProductButton = findViewById(R.id.addProductButton)
        viewProductsButton = findViewById(R.id.viewProductsButton)
        logoutButton = findViewById(R.id.logoutButton)
        productsCountTextView = findViewById(R.id.productsCountTextView)
        lowStockCountTextView = findViewById(R.id.lowStockCountTextView)
    }

    private fun setupDatabase() {
        productDao = StocklyDatabase
            .getInstance(applicationContext)
            .productDao()
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

        productNameEditText.setOnEditorActionListener { _, actionId, _ ->
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
    }

    private fun addProduct() {
        val productName = productNameEditText.text
            ?.toString()
            ?.trim()
            .orEmpty()

        if (productName.isBlank()) {
            productNameInputLayout.error =
                getString(R.string.product_name_required)

            productNameEditText.requestFocus()
            return
        }

        lifecycleScope.launch {
            addProductButton.isEnabled = false

            try {
                val alreadyExists = productDao.productNameExists(productName)

                if (alreadyExists) {
                    productNameInputLayout.error =
                        getString(R.string.product_already_exists)

                    productNameEditText.requestFocus()
                    return@launch
                }

                val product = Product(
                    name = productName,
                    category = DEFAULT_CATEGORY,
                    quantity = DEFAULT_QUANTITY,
                    minimumStock = DEFAULT_MINIMUM_STOCK
                )

                productDao.insert(product)

                productNameInputLayout.error = null
                productNameEditText.text?.clear()

                Toast.makeText(
                    this@MainActivity,
                    getString(
                        R.string.product_added_successfully,
                        productName
                    ),
                    Toast.LENGTH_SHORT
                ).show()
            } catch (exception: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Não foi possível adicionar o produto.",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                addProductButton.isEnabled = true
            }
        }
    }

    private fun showProducts() {
        lifecycleScope.launch {
            try {
                val products = productDao.getAllProducts().first()

                if (products.isEmpty()) {
                    MaterialAlertDialogBuilder(this@MainActivity)
                        .setTitle(R.string.products_dialog_title)
                        .setMessage(R.string.products_empty_message)
                        .setPositiveButton(R.string.action_close, null)
                        .show()

                    return@launch
                }

                val productList = products
                    .mapIndexed { index, product ->
                        """
                        ${index + 1}. ${product.name}
                        Categoria: ${product.category}
                        Quantidade: ${product.quantity}
                        Stock mínimo: ${product.minimumStock}
                        """.trimIndent()
                    }
                    .joinToString(separator = "\n\n")

                MaterialAlertDialogBuilder(this@MainActivity)
                    .setTitle(R.string.products_dialog_title)
                    .setMessage(productList)
                    .setPositiveButton(R.string.action_close, null)
                    .show()
            } catch (exception: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Não foi possível consultar os produtos.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun observeInventorySummary() {
        lifecycleScope.launch {
            productDao.getProductsCount().collect { totalProducts ->
                productsCountTextView.text = resources.getQuantityString(
                    R.plurals.inventory_products_total,
                    totalProducts,
                    totalProducts
                )
            }
        }

        lifecycleScope.launch {
            productDao.getLowStockCount().collect { lowStockProducts ->
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
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        startActivity(intent)
        finish()
    }

    companion object {
        private const val DEFAULT_CATEGORY = "Sem categoria"
        private const val DEFAULT_QUANTITY = 0
        private const val DEFAULT_MINIMUM_STOCK = 5
    }
}