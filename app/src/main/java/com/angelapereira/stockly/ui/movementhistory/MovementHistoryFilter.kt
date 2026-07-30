package com.angelapereira.stockly.ui.movementhistory

import com.angelapereira.stockly.data.local.MovementType

data class MovementHistoryFilter(
    val movementType: MovementType? = null,
    val productName: String? = null,
    val startDate: Long? = null,
    val endDate: Long? = null
)