package com.angelapereira.stockly

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.angelapereira.stockly.data.local.MovementType
import com.angelapereira.stockly.data.local.StockMovementWithProduct
import com.angelapereira.stockly.databinding.ItemStockMovementBinding
import java.text.DateFormat
import java.util.Date

class MovementHistoryAdapter :
    ListAdapter<
            StockMovementWithProduct,
            MovementHistoryAdapter.MovementViewHolder
            >(MovementDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MovementViewHolder {
        val binding = ItemStockMovementBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return MovementViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: MovementViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    inner class MovementViewHolder(
        private val binding: ItemStockMovementBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(movement: StockMovementWithProduct) {
            val context = binding.root.context

            binding.movementProductNameTextView.text =
                movement.productName

            binding.movementQuantityTextView.text =
                context.getString(
                    R.string.movement_history_quantity,
                    movement.quantity
                )

            val formattedDate = DateFormat
                .getDateTimeInstance(
                    DateFormat.SHORT,
                    DateFormat.SHORT
                )
                .format(Date(movement.createdAt))

            binding.movementDateTextView.text =
                context.getString(
                    R.string.movement_history_date,
                    formattedDate
                )

            when (movement.type) {
                MovementType.ENTRY -> {
                    binding.movementTypeTextView.text =
                        context.getString(
                            R.string.movement_history_entry
                        )

                    binding.movementTypeTextView.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.stockly_success
                        )
                    )

                    binding.movementTypeTextView.backgroundTintList =
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                context,
                                R.color.stockly_success_container
                            )
                        )
                }

                MovementType.EXIT -> {
                    binding.movementTypeTextView.text =
                        context.getString(
                            R.string.movement_history_exit
                        )

                    binding.movementTypeTextView.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.stockly_warning
                        )
                    )

                    binding.movementTypeTextView.backgroundTintList =
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                context,
                                R.color.stockly_warning_container
                            )
                        )
                }
            }
        }
    }

    private class MovementDiffCallback :
        DiffUtil.ItemCallback<StockMovementWithProduct>() {

        override fun areItemsTheSame(
            oldItem: StockMovementWithProduct,
            newItem: StockMovementWithProduct
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: StockMovementWithProduct,
            newItem: StockMovementWithProduct
        ): Boolean {
            return oldItem == newItem
        }
    }
}