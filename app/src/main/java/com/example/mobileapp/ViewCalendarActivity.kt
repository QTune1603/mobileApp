package com.example.mobileapp

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.gridlayout.widget.GridLayout
import com.example.mobileapp.dao.TransactionDAO
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ViewCalendarActivity : AppCompatActivity() {

    private lateinit var gridLayoutCalendar: GridLayout
    private lateinit var textViewCurrentMonth: TextView
    private lateinit var buttonPreviousMonth: Button
    private lateinit var buttonNextMonth: Button
    private lateinit var transactionDAO: TransactionDAO
    private var calendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_calendar)

        // Khởi tạo các view
        gridLayoutCalendar = findViewById(R.id.gridLayout_calendar)
        textViewCurrentMonth = findViewById(R.id.textView_current_month)
        buttonPreviousMonth = findViewById(R.id.button_previous_month)
        buttonNextMonth = findViewById(R.id.button_next_month)

        // Khởi tạo database helper
        transactionDAO = TransactionDAO(this)

        // Hiển thị tháng hiện tại
        updateMonthDisplay()

        // Nút quay lại MainActivity
        val buttonBack = findViewById<Button>(R.id.button_back)
        buttonBack.setOnClickListener {
            finish()  // Quay lại MainActivity
        }

        // Xử lý nút Previous và Next
        buttonPreviousMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateMonthDisplay()
        }

        buttonNextMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateMonthDisplay()
        }

        // Hiển thị lịch
        populateCalendar()
    }

    private fun updateMonthDisplay() {
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        textViewCurrentMonth.text = dateFormat.format(calendar.time)
        populateCalendar()
    }

    private fun populateCalendar() {
        gridLayoutCalendar.removeAllViews()

        // Hiển thị các thứ trong tuần (từ thứ Hai đến Chủ nhật)
        val daysOfWeek = listOf("M", "T", "W", "T", "F", "S", "S")
        for (dayOfWeek in daysOfWeek) {
            val dayOfWeekView = TextView(this)
            dayOfWeekView.text = dayOfWeek
            dayOfWeekView.textSize = 16f
            dayOfWeekView.gravity = Gravity.CENTER
            dayOfWeekView.setPadding(16, 16, 16, 16)
            dayOfWeekView.setTypeface(null, Typeface.BOLD)
            dayOfWeekView.layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            }
            gridLayoutCalendar.addView(dayOfWeekView)
        }

        // Đặt calendar tới ngày 1 của tháng hiện tại
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        // Lấy thứ của ngày 1 trong tuần (Chủ Nhật là 1, Thứ Hai là 2, ...)
        val firstDayOfMonth = calendar.get(Calendar.DAY_OF_WEEK)
        val offset = if (firstDayOfMonth == Calendar.SUNDAY) 6 else firstDayOfMonth - 2

        // Điền các ô trống trước ngày 1 (nếu ngày 1 không phải là Thứ Hai)
        for (i in 0 until offset) {
            val emptyView = TextView(this)
            emptyView.layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            }
            gridLayoutCalendar.addView(emptyView)
        }

        // Điền các ngày trong tháng
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (day in 1..daysInMonth) {
            val dayLayout = LinearLayout(this)
            dayLayout.orientation = LinearLayout.VERTICAL
            dayLayout.gravity = Gravity.CENTER

            // Hiển thị số ngày
            val dayView = TextView(this)
            dayView.text = day.toString()
            dayView.textSize = 16f
            dayView.gravity = Gravity.CENTER
            dayView.setPadding(16, 16, 16, 16)
            dayLayout.addView(dayView)

            // Kiểm tra số lần thu và chi cho ngày này
            val transactionCount = getTransactionCountForDay(day)
            val (incomeCount, expenseCount) = transactionCount

            // Hiển thị dấu chấm nếu có thu hoặc chi
            if (incomeCount > 0) {
                val incomeDot = View(this)
                incomeDot.layoutParams = LinearLayout.LayoutParams(20, 20).apply {
                    topMargin = 4
                }
                incomeDot.setBackgroundResource(R.drawable.circle_green)  // Xanh cho thu nhập
                dayLayout.addView(incomeDot)
            }

            if (expenseCount > 0) {
                val expenseDot = View(this)
                expenseDot.layoutParams = LinearLayout.LayoutParams(20, 20).apply {
                    topMargin = 4
                }
                expenseDot.setBackgroundResource(R.drawable.circle_red)  // Đỏ cho chi tiêu
                dayLayout.addView(expenseDot)
            }

            // Đặt layout params cho mỗi ô ngày trong GridLayout
            dayLayout.layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            }

            val selectedDate = "$day/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}"

            // Thêm sự kiện click vào mỗi ngày
            dayLayout.setOnClickListener {
                val intent = Intent(this, DayTransactionsActivity::class.java)
                intent.putExtra("selected_date", selectedDate) // Pass the selected date to the new activity
                startActivity(intent)
            }


            // Thêm dayLayout vào gridLayoutCalendar
            gridLayoutCalendar.addView(dayLayout)
        }

        // Sau khi điền hết số ngày trong tháng, thêm các ô trống nếu cần để đảm bảo 6 hàng đầy đủ
        val totalCells = offset + daysInMonth
        val cellsNeeded = 42 - totalCells  // Cần đủ 42 ô cho 6 hàng, 7 cột
        for (i in 0 until cellsNeeded) {
            val emptyView = TextView(this)
            emptyView.layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            }
            gridLayoutCalendar.addView(emptyView)
        }
    }

    private fun getTransactionCountForDay(day: Int): Pair<Int, Int> {
        // Lấy tháng và năm hiện tại từ calendar
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        val currentYear = calendar.get(Calendar.YEAR)
        val selectedDate = "$day/$currentMonth/$currentYear"  // Tạo ngày được chọn theo định dạng dd/MM/yyyy

        val transactions = transactionDAO.getTransactionsForDay(selectedDate)

        var incomeCount = 0
        var expenseCount = 0
        for (transaction in transactions) {
            if (transaction.type == "Thu") {
                incomeCount++
            } else if (transaction.type == "Chi") {
                expenseCount++
            }
        }
        return Pair(incomeCount, expenseCount)
    }
}
