package com.angelapereira.stockly

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.angelapereira.stockly.data.local.StockMovementWithProduct
import com.angelapereira.stockly.data.local.StocklyDatabase
import com.angelapereira.stockly.data.repository.StockMovementRepository
import com.angelapereira.stockly.ui.movementhistory.MovementHistoryUiState
import com.angelapereira.stockly.ui.movementhistory.MovementHistoryViewModel
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch

class MovementHistoryActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var countTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: View

    private lateinit var movementAdapter: MovementHistoryAdapter
    private lateinit var viewModel: MovementHistoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_movement_history)

        setupWindowInsets()
        bindViews()
        setupViewModel()
        setupToolbar()
        setupRecyclerView()
        observeUiState()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.movementHistoryMain)
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
        toolbar = findViewById(R.id.movementHistoryToolbar)
        countTextView = findViewById(R.id.movementHistoryCountTextView)
        recyclerView = findViewById(R.id.movementHistoryRecyclerView)
        emptyState = findViewById(R.id.movementHistoryEmptyState)
    }

    private fun setupViewModel() {
        val database = StocklyDatabase.getInstance(applicationContext)

        val repository = StockMovementRepository(
            stockMovementDao = database.stockMovementDao(),
            productDao = database.productDao()
        )

        val factory = MovementHistoryViewModel.Factory(repository)

        viewModel = ViewModelProvider(
            this,
            factory
        )[MovementHistoryViewModel::class.java]
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
        movementAdapter = MovementHistoryAdapter()

        recyclerView.apply {
            layoutManager =
                LinearLayoutManager(this@MovementHistoryActivity)

            adapter = movementAdapter
            setHasFixedSize(true)
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    MovementHistoryUiState.Loading -> {
                        showLoadingState()
                    }

                    is MovementHistoryUiState.Success -> {
                        showMovements(state.movements)
                    }

                    MovementHistoryUiState.Error -> {
                        showErrorState()
                    }
                }
            }
        }
    }

    private fun showLoadingState() {
        countTextView.text =
            getString(R.string.movement_history_loading)

        recyclerView.visibility = View.GONE
        emptyState.visibility = View.GONE
    }

    private fun showMovements(
        movements: List<StockMovementWithProduct>
    ) {
        movementAdapter.submitList(movements)

        countTextView.text = resources.getQuantityString(
            R.plurals.movement_history_count,
            movements.size,
            movements.size
        )

        val hasMovements = movements.isNotEmpty()

        recyclerView.visibility = if (hasMovements) {
            View.VISIBLE
        } else {
            View.GONE
        }

        emptyState.visibility = if (hasMovements) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    private fun showErrorState() {
        countTextView.text =
            getString(R.string.movement_history_error)

        recyclerView.visibility = View.GONE
        emptyState.visibility = View.VISIBLE
    }
}