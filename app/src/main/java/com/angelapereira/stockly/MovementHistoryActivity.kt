package com.angelapereira.stockly

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.angelapereira.stockly.data.local.MovementType
import com.angelapereira.stockly.data.local.StockMovementWithProduct
import com.angelapereira.stockly.data.local.StocklyDatabase
import com.angelapereira.stockly.data.repository.StockMovementRepository
import com.angelapereira.stockly.ui.movementhistory.MovementHistoryFilter
import com.angelapereira.stockly.ui.movementhistory.MovementHistoryUiState
import com.angelapereira.stockly.ui.movementhistory.MovementHistoryViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

class MovementHistoryActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var countTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: View

    private lateinit var typeFilterToggleGroup:
            MaterialButtonToggleGroup

    private lateinit var productFilterAutoCompleteTextView:
            MaterialAutoCompleteTextView

    private lateinit var startDateFilterButton: MaterialButton
    private lateinit var endDateFilterButton: MaterialButton
    private lateinit var clearFiltersButton: MaterialButton

    private lateinit var movementAdapter: MovementHistoryAdapter
    private lateinit var viewModel: MovementHistoryViewModel

    private var selectedStartDate: Long? = null
    private var selectedEndDate: Long? = null

    private var isUpdatingFilters = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_movement_history)

        setupWindowInsets()
        bindViews()
        setupViewModel()
        setupToolbar()
        setupRecyclerView()
        setupFilterListeners()
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
        toolbar =
            findViewById(R.id.movementHistoryToolbar)

        countTextView =
            findViewById(R.id.movementHistoryCountTextView)

        recyclerView =
            findViewById(R.id.movementHistoryRecyclerView)

        emptyState =
            findViewById(R.id.movementHistoryEmptyState)

        typeFilterToggleGroup =
            findViewById(R.id.movementTypeFilterToggleGroup)

        productFilterAutoCompleteTextView =
            findViewById(
                R.id.productFilterAutoCompleteTextView
            )

        startDateFilterButton =
            findViewById(R.id.startDateFilterButton)

        endDateFilterButton =
            findViewById(R.id.endDateFilterButton)

        clearFiltersButton =
            findViewById(R.id.clearMovementFiltersButton)
    }

    private fun setupViewModel() {
        val database = StocklyDatabase.getInstance(
            applicationContext
        )

        val repository = StockMovementRepository(
            stockMovementDao = database.stockMovementDao(),
            productDao = database.productDao()
        )

        val factory =
            MovementHistoryViewModel.Factory(repository)

        viewModel = ViewModelProvider(
            this,
            factory
        )[MovementHistoryViewModel::class.java]
    }

    private fun setupToolbar() {
        toolbar.setNavigationIcon(
            androidx.appcompat.R.drawable
                .abc_ic_ab_back_material
        )

        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        movementAdapter = MovementHistoryAdapter()

        recyclerView.apply {
            layoutManager =
                LinearLayoutManager(
                    this@MovementHistoryActivity
                )

            adapter = movementAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupFilterListeners() {
        typeFilterToggleGroup.addOnButtonCheckedListener {
                _,
                checkedId,
                isChecked
            ->

            if (!isChecked || isUpdatingFilters) {
                return@addOnButtonCheckedListener
            }

            val movementType = when (checkedId) {
                R.id.entryMovementsFilterButton ->
                    MovementType.ENTRY

                R.id.exitMovementsFilterButton ->
                    MovementType.EXIT

                else -> null
            }

            viewModel.filterByType(movementType)
        }

        productFilterAutoCompleteTextView
            .setOnItemClickListener { parent, _, position, _ ->
                if (isUpdatingFilters) {
                    return@setOnItemClickListener
                }

                val selectedProduct =
                    parent.getItemAtPosition(position)
                        .toString()

                val allProductsText = getString(
                    R.string.movement_filter_all_products
                )

                val productFilter = if (
                    selectedProduct == allProductsText
                ) {
                    null
                } else {
                    selectedProduct
                }

                viewModel.filterByProduct(productFilter)
            }
        productFilterAutoCompleteTextView.setOnEditorActionListener {
                _,
                actionId,
                _
            ->

            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                val typedProduct = productFilterAutoCompleteTextView.text
                    ?.toString()
                    ?.trim()
                    .orEmpty()

                val allProductsText = getString(
                    R.string.movement_filter_all_products
                )

                val productFilter = typedProduct.takeIf {
                    it.isNotBlank() && it != allProductsText
                }

                viewModel.filterByProduct(productFilter)

                true
            } else {
                false
            }
        }

        startDateFilterButton.setOnClickListener {
            showStartDatePicker()
        }

        endDateFilterButton.setOnClickListener {
            showEndDatePicker()
        }

        clearFiltersButton.setOnClickListener {
            viewModel.clearFilters()
        }
    }

    private fun showStartDatePicker() {
        val picker = MaterialDatePicker.Builder
            .datePicker()
            .setTitleText(
                R.string.movement_filter_start_date
            )
            .setSelection(
                selectedStartDate
                    ?: MaterialDatePicker.todayInUtcMilliseconds()
            )
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            selectedStartDate = selection

            viewModel.filterByDateRange(
                startDate = selectedStartDate,
                endDate = selectedEndDate
            )
        }

        picker.show(
            supportFragmentManager,
            START_DATE_PICKER_TAG
        )
    }

    private fun showEndDatePicker() {
        val picker = MaterialDatePicker.Builder
            .datePicker()
            .setTitleText(
                R.string.movement_filter_end_date
            )
            .setSelection(
                selectedEndDate
                    ?: MaterialDatePicker.todayInUtcMilliseconds()
            )
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            selectedEndDate =
                selection + END_OF_DAY_OFFSET

            viewModel.filterByDateRange(
                startDate = selectedStartDate,
                endDate = selectedEndDate
            )
        }

        picker.show(
            supportFragmentManager,
            END_DATE_PICKER_TAG
        )
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    MovementHistoryUiState.Loading -> {
                        showLoadingState()
                    }

                    is MovementHistoryUiState.Success -> {
                        showMovements(
                            movements = state.movements,
                            totalMovements = state.totalMovements
                        )

                        updateProductFilter(
                            products = state.availableProducts,
                            selectedProduct =
                                state.filter.productName
                        )

                        updateFilterControls(state.filter)
                    }

                    MovementHistoryUiState.Error -> {
                        showErrorState()
                    }
                }
            }
        }
    }

    private fun updateProductFilter(
        products: List<String>,
        selectedProduct: String?
    ) {
        val allProductsText = getString(
            R.string.movement_filter_all_products
        )

        val options = listOf(allProductsText) + products

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            options
        )

        productFilterAutoCompleteTextView.setAdapter(adapter)

        isUpdatingFilters = true

        productFilterAutoCompleteTextView.setText(
            selectedProduct ?: allProductsText,
            false
        )

        isUpdatingFilters = false
    }

    private fun updateFilterControls(
        filter: MovementHistoryFilter
    ) {
        isUpdatingFilters = true

        val selectedButtonId = when (filter.movementType) {
            MovementType.ENTRY ->
                R.id.entryMovementsFilterButton

            MovementType.EXIT ->
                R.id.exitMovementsFilterButton

            null ->
                R.id.allMovementsFilterButton
        }

        typeFilterToggleGroup.check(selectedButtonId)

        selectedStartDate = filter.startDate
        selectedEndDate = filter.endDate

        startDateFilterButton.text =
            filter.startDate?.let { date ->
                formatFilterDate(date)
            } ?: getString(
                R.string.movement_filter_start_date
            )

        endDateFilterButton.text =
            filter.endDate?.let { date ->
                formatFilterDate(date)
            } ?: getString(
                R.string.movement_filter_end_date
            )

        isUpdatingFilters = false
    }

    private fun formatFilterDate(timestamp: Long): String {
        val formattedDate = DateFormat
            .getDateInstance(DateFormat.SHORT)
            .format(Date(timestamp))

        return getString(
            R.string.movement_filter_date_value,
            formattedDate
        )
    }

    private fun showLoadingState() {
        countTextView.text =
            getString(R.string.movement_history_loading)

        recyclerView.visibility = View.GONE
        emptyState.visibility = View.GONE
    }

    private fun showMovements(
        movements: List<StockMovementWithProduct>,
        totalMovements: Int
    ) {
        movementAdapter.submitList(movements)

        countTextView.text = if (
            movements.size == totalMovements
        ) {
            resources.getQuantityString(
                R.plurals.movement_history_count,
                movements.size,
                movements.size
            )
        } else {
            getString(
                R.string.movement_history_filtered_count,
                movements.size,
                totalMovements
            )
        }

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

    companion object {
        private const val START_DATE_PICKER_TAG =
            "start_date_picker"

        private const val END_DATE_PICKER_TAG =
            "end_date_picker"

        private const val END_OF_DAY_OFFSET =
            86_399_999L
    }
}