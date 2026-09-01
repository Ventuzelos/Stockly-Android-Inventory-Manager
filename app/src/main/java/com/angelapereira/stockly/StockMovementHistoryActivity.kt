package com.angelapereira.stockly

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.angelapereira.stockly.data.local.StocklyDatabase
import com.angelapereira.stockly.data.repository.ProductRepository
import com.angelapereira.stockly.data.repository.StockMovementRepository
import com.angelapereira.stockly.ui.main.MovementHistoryFilter
import com.angelapereira.stockly.ui.main.StockMovementHistoryViewModel
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class StockMovementHistoryActivity : AppCompatActivity() {

    private lateinit var viewModel: StockMovementHistoryViewModel
    private lateinit var adapter: StockMovementHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(
            R.layout.activity_stock_movement_history
        )

        setupWindowInsets()
        setupViewModel()
        setupRecyclerView()
        setupListeners()
        observeUiState()
    }

    private fun setupWindowInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.stockMovementHistoryMain)
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

    private fun setupViewModel() {

        val database =
            StocklyDatabase.getInstance(
                applicationContext
            )

        val productRepository =
            ProductRepository(
                database.productDao()
            )

        val stockMovementRepository =
            StockMovementRepository(
                stockMovementDao =
                    database.stockMovementDao(),
                productDao =
                    database.productDao()
            )

        viewModel =
            ViewModelProvider(
                this,
                StockMovementHistoryViewModel.Factory(
                    stockMovementRepository,
                    productRepository
                )
            )[StockMovementHistoryViewModel::class.java]
    }

    private fun setupRecyclerView() {

        adapter =
            StockMovementHistoryAdapter()

        findViewById<androidx.recyclerview.widget.RecyclerView>(
            R.id.stockMovementHistoryRecyclerView
        ).apply {

            layoutManager =
                LinearLayoutManager(
                    this@StockMovementHistoryActivity
                )

            adapter =
                this@StockMovementHistoryActivity.adapter

            setHasFixedSize(false)
        }
    }

    private fun setupListeners() {

        findViewById<MaterialButton>(
            R.id.stockMovementHistoryBackButton
        ).setOnClickListener {
            finish()
        }

        findViewById<MaterialButton>(
            R.id.stockMovementHistoryFilterButton
        ).setOnClickListener {
            showFilterDialog()
        }
    }

    private fun observeUiState() {

        lifecycleScope.launch {

            viewModel.uiState.collect { state ->

                val filteredMovements =
                    state.filteredMovements

                adapter.submitList(
                    movements = filteredMovements,
                    productNames = state.productNames
                )

                updateCount(
                    filteredMovements.size
                )

                updateEmptyState(
                    filteredMovements.isEmpty()
                )
            }
        }
    }

    private fun updateCount(
        count: Int
    ) {

        findViewById<android.widget.TextView>(
            R.id.stockMovementHistoryCountTextView
        ).text =
            resources.getQuantityString(
                R.plurals.stock_movement_history_count,
                count,
                count
            )
    }

    private fun updateEmptyState(
        isEmpty: Boolean
    ) {

        findViewById<View>(
            R.id.stockMovementHistoryEmptyState
        ).visibility =
            if (isEmpty) {
                View.VISIBLE
            } else {
                View.GONE
            }

        findViewById<androidx.recyclerview.widget.RecyclerView>(
            R.id.stockMovementHistoryRecyclerView
        ).visibility =
            if (isEmpty) {
                View.GONE
            } else {
                View.VISIBLE
            }
    }

    private fun showFilterDialog() {

        val options = arrayOf(
            getString(
                R.string.stock_movement_history_filter_all
            ),
            getString(
                R.string.stock_movement_history_filter_entry
            ),
            getString(
                R.string.stock_movement_history_filter_exit
            )
        )

        val currentFilter =
            viewModel.uiState.value.filter

        val checkedItem =
            when (currentFilter) {

                MovementHistoryFilter.ALL ->
                    0

                MovementHistoryFilter.ENTRY ->
                    1

                MovementHistoryFilter.EXIT ->
                    2
            }

        com.google.android.material.dialog.MaterialAlertDialogBuilder(
            this
        )
            .setTitle(
                R.string.stock_movement_history_filter_title
            )
            .setSingleChoiceItems(
                options,
                checkedItem
            ) { dialog, which ->

                val filter =
                    when (which) {

                        1 ->
                            MovementHistoryFilter.ENTRY

                        2 ->
                            MovementHistoryFilter.EXIT

                        else ->
                            MovementHistoryFilter.ALL
                    }

                viewModel.setFilter(filter)

                dialog.dismiss()
            }
            .show()
    }
}