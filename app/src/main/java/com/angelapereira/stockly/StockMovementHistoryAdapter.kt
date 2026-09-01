package com.angelapereira.stockly

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.angelapereira.stockly.data.local.MovementType
import com.angelapereira.stockly.data.local.StockMovement
import com.angelapereira.stockly.databinding.ItemStockMovementBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StockMovementHistoryAdapter :
    RecyclerView.Adapter<StockMovementHistoryAdapter.MovementViewHolder>() {

    private var movements: List<StockMovement> =
        emptyList()

    private var productNames: Map<Int, String> =
        emptyMap()

    fun submitList(
        movements: List<StockMovement>,
        productNames: Map<Int, String>
    ) {
        this.movements = movements
        this.productNames = productNames

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MovementViewHolder {

        val binding =
            ItemStockMovementBinding.inflate(
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
        holder.bind(
            movements[position]
        )
    }

    override fun getItemCount(): Int {
        return movements.size
    }

    inner class MovementViewHolder(
        private val binding: ItemStockMovementBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {

        fun bind(
            movement: StockMovement
        ) {

            val context =
                binding.root.context


            val productName =
                productNames[movement.productId]
                    ?: "Produto"

            binding.movementProductNameTextView.text =
                productName



            val dateFormat =
                SimpleDateFormat(
                    "dd/MM/yyyy • HH:mm",
                    Locale.getDefault()
                )

            binding.movementDateTextView.text =
                context.getString(
                    R.string.stock_movement_history_date_format,
                    dateFormat.format(
                        Date(movement.createdAt)
                    )
                )



            val isEntry =
                movement.type == MovementType.ENTRY

            if (isEntry) {

                // Entrada → verde

                binding.movementTypeTextView.text =
                    context.getString(
                        R.string.stock_movement_history_entry
                    )

                binding.movementTypeTextView.setTextColor(
                    Color.parseColor("#15803D")
                )

                binding.movementTypeTextView.backgroundTintList =
                    ColorStateList.valueOf(
                        Color.parseColor("#ECFDF5")
                    )

                binding.movementIconContainer.backgroundTintList =
                    ColorStateList.valueOf(
                        Color.parseColor("#ECFDF5")
                    )

                binding.movementIconImageView.setImageResource(
                    R.drawable.ic_arrow_forward
                )

                binding.movementIconImageView.imageTintList =
                    ColorStateList.valueOf(
                        Color.parseColor("#16A34A")
                    )

                binding.movementQuantityTextView.text =
                    "+ ${movement.quantity}"

                binding.movementQuantityTextView.setTextColor(
                    Color.parseColor("#15803D")
                )

            } else {

                // Saída → laranja

                binding.movementTypeTextView.text =
                    context.getString(
                        R.string.stock_movement_history_exit
                    )

                binding.movementTypeTextView.setTextColor(
                    Color.parseColor("#C2410C")
                )

                binding.movementTypeTextView.backgroundTintList =
                    ColorStateList.valueOf(
                        Color.parseColor("#FFF7ED")
                    )

                binding.movementIconContainer.backgroundTintList =
                    ColorStateList.valueOf(
                        Color.parseColor("#FFF7ED")
                    )

                binding.movementIconImageView.setImageResource(
                    R.drawable.ic_arrow_down
                )

                binding.movementIconImageView.imageTintList =
                    ColorStateList.valueOf(
                        Color.parseColor("#F59E0B")
                    )

                binding.movementQuantityTextView.text =
                    "- ${movement.quantity}"

                binding.movementQuantityTextView.setTextColor(
                    Color.parseColor("#C2410C")
                )
            }


            binding.movementQuantityLabelTextView.text =
                context.getString(
                    R.string.stock_movement_history_quantity_label
                )
        }
    }
}