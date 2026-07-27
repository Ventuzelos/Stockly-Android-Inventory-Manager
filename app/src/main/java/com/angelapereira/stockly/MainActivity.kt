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
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class MainActivity : AppCompatActivity() {

    private lateinit var productNameInputLayout: TextInputLayout
    private lateinit var productNameEditText: TextInputEditText
    private lateinit var addProductButton: MaterialButton
    private lateinit var viewProductsButton: MaterialButton
    private lateinit var logoutButton: MaterialButton
    private lateinit var productsCountTextView: TextView
    private lateinit var lowStockCountTextView: TextView

    /*
     * Lista temporária.
     * Os produtos desaparecem quando a aplicação é encerrada.
     * Posteriormente será substituída por Room.
     */
    private val products = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        setupWindowInsets()
        bindViews()
        setupListeners()
        updateInventorySummary()
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
            productNameInputLayout.error = getString(R.string.product_name_required)
            productNameEditText.requestFocus()
            return
        }

        val alreadyExists = products.any {
            it.equals(productName, ignoreCase = true)
        }

        if (alreadyExists) {
            productNameInputLayout.error = getString(R.string.product_already_exists)
            productNameEditText.requestFocus()
            return
        }

        productNameInputLayout.error = null
        products.add(productName)

        productNameEditText.text?.clear()
        updateInventorySummary()

        Toast.makeText(
            this,
            getString(R.string.product_added_successfully, productName),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showProducts() {
        if (products.isEmpty()) {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.products_dialog_title)
                .setMessage(R.string.products_empty_message)
                .setPositiveButton(R.string.action_close, null)
                .show()

            return
        }

        val productList = products
            .mapIndexed { index, product ->
                "${index + 1}. $product"
            }
            .joinToString(separator = "\n")

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.products_dialog_title)
            .setMessage(productList)
            .setPositiveButton(R.string.action_close, null)
            .show()
    }

    private fun updateInventorySummary() {
        productsCountTextView.text = resources.getQuantityString(
            R.plurals.inventory_products_total,
            products.size,
            products.size
        )

        /*
         * Nesta fase ainda não existem quantidades.
         * O stock reduzido será calculado quando criarmos o modelo Product.
         */
        lowStockCountTextView.text = getString(
            R.string.inventory_low_stock_dynamic,
            0
        )
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
}