package com.example.mobileapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapp.dao.CategoryDAO
import com.example.mobileapp.dao.TransactionDAO
import java.text.DecimalFormat
import com.example.mobileapp.model.Transaction
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var transactionDAO: TransactionDAO
    private lateinit var categoryDAO: CategoryDAO
    private lateinit var transactionList: List<Transaction>
    private lateinit var adapter: TransactionAdapter
    private lateinit var textViewCurrentDate: TextView  // Định nghĩa biến toàn cục cho TextView ngày hiện tại

    companion object {
        const val ADD_TRANSACTION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Hiển thị icon 3 gạch (Hamburger icon)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_hamburger_icon) // Đảm bảo bạn có icon

        // Nút View để mở lịch
        val buttonView: Button = findViewById(R.id.button_view_calendar)
        buttonView.setOnClickListener {
            // Hành động khi bấm nút View
            Toast.makeText(this, "View Calendar Clicked", Toast.LENGTH_SHORT).show()
        }

        // Khởi tạo TextView để hiển thị ngày
        textViewCurrentDate = findViewById(R.id.textView_current_date)

        // Kiểm tra nếu có ngày được chọn từ ViewCalendarActivity
        val selectedDate = intent.getStringExtra("selected_date")
        if (selectedDate != null) {
            // Hiển thị ngày được chọn và thông tin giao dịch
            updateUIForSelectedDate(selectedDate)
        } else {
            // Mặc định hiển thị ngày hiện tại
            updateUIForToday()
        }

        val buttonViewCalendar = findViewById<Button>(R.id.button_view_calendar)
        buttonViewCalendar.setOnClickListener {
            val intent = Intent(this, ViewCalendarActivity::class.java)
            startActivity(intent)
        }

        transactionDAO = TransactionDAO(this)

        // Khởi tạo CategoryDAO
        categoryDAO = CategoryDAO(this)

        // Gọi phương thức để kiểm tra và truy vấn bảng Category
        val categories = categoryDAO.getAllCategories()  // Lấy tất cả danh mục
        if (categories.isNotEmpty()) {
            // Bạn có thể log kết quả để kiểm tra xem bảng có được khởi tạo không
            Log.d("MainActivity", "Categories retrieved: ${categories.size}")
        } else {
            Log.d("MainActivity", "No categories found")
        }

        // Tạo nút "Thêm mới" cho giao dịch
        val buttonAddNew = findViewById<Button>(R.id.button_add_new)
        buttonAddNew.setOnClickListener {
            val intent = Intent(this, AddTransaction::class.java)
            startActivityForResult(intent, ADD_TRANSACTION_REQUEST_CODE)
        }

        // Lấy dữ liệu từ database
        transactionList = transactionDAO.getAllTransactions()

        // Hiển thị tổng thu và tổng chi
        updateTotalValues()

        // Thiết lập RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView_transactions)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TransactionAdapter(transactionList)
        recyclerView.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    // Xử lý sự kiện khi nhấn vào Hamburger icon
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Chuyển hướng đến StatisticsActivity khi bấm vào Hamburger icon
                val intent = Intent(this, StatisticsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        // Cập nhật lại danh sách giao dịch sau khi quay lại từ AddTransaction
        transactionList = transactionDAO.getAllTransactions()
        adapter.updateTransactions(transactionList)
        updateTotalValues()
    }


    private fun updateTotalValues() {
        val totalIncome = transactionDAO.getTotalIncome()
        val totalExpense = transactionDAO.getTotalExpense()

        // Định dạng số tiền với DecimalFormat
        val decimalFormat = DecimalFormat("#,###.##")
        findViewById<TextView>(R.id.textView_total_income_value).text = "${decimalFormat.format(totalIncome)} VND"
        findViewById<TextView>(R.id.textView_total_expense_value).text = "${decimalFormat.format(totalExpense)} VND"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_TRANSACTION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Cập nhật lại danh sách giao dịch và tổng thu/chi
            transactionList = transactionDAO.getAllTransactions()
            adapter.updateTransactions(transactionList)
            updateTotalValues()
        }
    }

    private fun updateUIForSelectedDate(selectedDate: String) {
        // Cập nhật giao diện cho ngày được chọn
        textViewCurrentDate.text = "Ngày được chọn: $selectedDate"
        // Hiển thị thông tin giao dịch cho ngày được chọn
        // (gọi database để lấy thông tin theo ngày và cập nhật vào RecyclerView)
    }

    private fun updateUIForToday() {
        // Lấy ngày hiện tại và hiển thị
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(calendar.time)

        // Cập nhật TextView với ngày hiện tại
        textViewCurrentDate.text = "Ngày hiện tại: $currentDate"
    }
}
