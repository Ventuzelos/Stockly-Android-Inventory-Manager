package com.angelapereira.stockly

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.angelapereira.stockly.data.local.Product
import com.angelapereira.stockly.databinding.ItemProductBinding

class ProductAdapter(
    private val onMovementClick: (Product) -> Unit,
    private val onEditClick: (Product) -> Unit,
    private val onDeleteClick: (Product) -> Unit
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(
    ProductDiffCallback()
) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ProductViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    inner class ProductViewHolder(
        private val binding: ItemProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            val context = binding.root.context

            binding.productNameTextView.text = product.name

            binding.productCategoryTextView.text = context.getString(
                R.string.product_category_value,
                product.category
            )

            binding.productQuantityTextView.text = context.getString(
                R.string.product_quantity_value,
                product.quantity
            )

            binding.productMinimumStockTextView.text = context.getString(
                R.string.product_minimum_stock_value,
                product.minimumStock
            )

            if (product.isLowStock) {
                binding.productStockStatusTextView.text = context.getString(
                    R.string.stock_status_low
                )

                binding.productStockStatusTextView.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.stockly_warning
                    )
                )

                binding.productStockStatusTextView.backgroundTintList =
                    ColorStateList.valueOf(
                        ContextCompat.getColor(
                            context,
                            R.color.stockly_warning_container
                        )
                    )
            } else {
                binding.productStockStatusTextView.text = context.getString(
                    R.string.stock_status_available
                )

                binding.productStockStatusTextView.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.stockly_success
                    )
                )

                binding.productStockStatusTextView.backgroundTintList =
                    ColorStateList.valueOf(
                        ContextCompat.getColor(
                            context,
                            R.color.stockly_success_container
                        )
                    )
            }

            binding.movementProductButton.setOnClickListener {
                onMovementClick(product)
            }

            binding.editProductButton.setOnClickListener {
                onEditClick(product)
            }

            binding.deleteProductButton.setOnClickListener {
                onDeleteClick(product)
            }
        }
    }

    private class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {

        override fun areItemsTheSame(
            oldItem: Product,
            newItem: Product
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: Product,
            newItem: Product
        ): Boolean {
            return oldItem == newItem
        }
    }
}