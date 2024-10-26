package com.example.mobileapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapp.model.Transaction
import java.text.DecimalFormat

class TransactionAdapter(private var transactionList: List<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewCategory: TextView = view.findViewById(R.id.textView_category)
        val textViewAmount: TextView = view.findViewById(R.id.textView_amount)
        val textViewDate: TextView = view.findViewById(R.id.textView_date)
        val imageViewIcon: ImageView = view.findViewById(R.id.imageView_category_icon)  // ImageView for displaying icons
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactionList[position]
        holder.textViewCategory.text = transaction.category

        // Format amount using DecimalFormat
        val decimalFormat = DecimalFormat("#,###.##")
        holder.textViewAmount.text = "${decimalFormat.format(transaction.amount)} VND"
        holder.textViewDate.text = transaction.date

        // Determine and set the correct icon based on category and subCategory
        val category = transaction.category
        val subCategory = transaction.subCategory

        when (category) {
            "Chi tiêu hằng ngày" -> {
                // For "Chi tiêu hằng ngày", determine icon by subCategory
                when (subCategory) {
                    "Tiền nhà" -> holder.imageViewIcon.setImageResource(R.drawable.ic_tiennha)
                    "Tiền điện" -> holder.imageViewIcon.setImageResource(R.drawable.ic_tiendien)
                    "Tiền nước" -> holder.imageViewIcon.setImageResource(R.drawable.ic_tiennuoc)
                    "Tiền điện thoại" -> holder.imageViewIcon.setImageResource(R.drawable.ic_tiendienthoai)
                    "Tiền ăn" -> holder.imageViewIcon.setImageResource(R.drawable.ic_tienan)
                    "Tiền đi chợ" -> holder.imageViewIcon.setImageResource(R.drawable.ic_tiendicho)
                    else -> holder.imageViewIcon.setImageResource(R.drawable.ic_launcher_background)  // Default icon for unknown subCategories
                }
            }
            else -> {
                // For other categories, check if a custom icon exists in the transaction data
                if (transaction.icon != 0) {
                    holder.imageViewIcon.setImageResource(transaction.icon)
                } else {
                    holder.imageViewIcon.setImageResource(R.drawable.ic_launcher_background)  // Default icon if no custom icon is set
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return transactionList.size
    }

    // Function to update the list of transactions and refresh the RecyclerView
    fun updateTransactions(newTransactions: List<Transaction>) {
        transactionList = newTransactions
        notifyDataSetChanged()
    }
}
