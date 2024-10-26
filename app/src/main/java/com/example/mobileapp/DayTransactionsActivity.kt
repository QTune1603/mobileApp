package com.example.mobileapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapp.dao.TransactionDAO
import java.text.DecimalFormat

class DayTransactionsActivity : AppCompatActivity() {

    private lateinit var transactionDAO: TransactionDAO
    private lateinit var textViewDate: TextView
    private lateinit var textViewTotalIncome: TextView
    private lateinit var textViewTotalExpense: TextView
    private lateinit var recyclerViewTransactions: RecyclerView
    private var selectedDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_day_transactions)

        // Get the selected date from the intent
        selectedDate = intent.getStringExtra("selected_date")

        // Initialize database helper
        transactionDAO = TransactionDAO(this)

        // Initialize views
        textViewDate = findViewById(R.id.textView_date)
        textViewTotalIncome = findViewById(R.id.textView_total_income)
        textViewTotalExpense = findViewById(R.id.textView_total_expense)
        recyclerViewTransactions = findViewById(R.id.recyclerView_transactions)

        // Set layout manager for RecyclerView
        recyclerViewTransactions.layoutManager = LinearLayoutManager(this)

        // Display the selected date at the top
        textViewDate.text = "Ngày: $selectedDate"

        // Fetch and display transactions for the selected date
        displayTransactionsForDate()
    }

    private fun displayTransactionsForDate() {
        selectedDate?.let { date ->
            // Fetch transactions for the selected date
            val transactions = transactionDAO.getTransactionsForDay(date)

            // Separate transactions into income and expense
            val incomeTransactions = transactions.filter { it.type == "Thu" }
            val expenseTransactions = transactions.filter { it.type == "Chi" }

            // Calculate total income and total expense
            val totalIncome = incomeTransactions.sumOf { it.amount }
            val totalExpense = expenseTransactions.sumOf { it.amount }

            // Format total values
            val decimalFormat = DecimalFormat("#,### VND")
            textViewTotalIncome.text = decimalFormat.format(totalIncome)
            textViewTotalExpense.text = decimalFormat.format(totalExpense)

            // Set up the RecyclerView adapter
            val adapter = TransactionAdapter(transactions)
            recyclerViewTransactions.adapter = adapter
        }
    }
}
