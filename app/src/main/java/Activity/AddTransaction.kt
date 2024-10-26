package com.example.mobileapp

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.mobileapp.dao.CategoryDAO
import com.example.mobileapp.dao.TransactionDAO
import com.example.mobileapp.model.Category
import com.example.mobileapp.model.Transaction
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddTransaction : AppCompatActivity() {
    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerSubCategory: Spinner
    private lateinit var categoryDAO: CategoryDAO
    private lateinit var transactionDAO: TransactionDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        transactionDAO = TransactionDAO(this)
        categoryDAO = CategoryDAO(this)

        spinnerCategory = findViewById(R.id.spinnerCategory)
        spinnerSubCategory = findViewById(R.id.spinnerSubCategory)

        loadDefaultCategory()

        val radioGroup = findViewById<RadioGroup>(R.id.radioGroupIncomeExpense)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioIncome -> {
                    loadCategories(categoryDAO.getIncomeCategories())
                    spinnerSubCategory.visibility = View.GONE
                }
                R.id.radioExpense -> {
                    loadCategories(categoryDAO.getExpenseCategories())
                }
                else -> {
                    loadDefaultCategory()
                }
            }
        }

        // Xử lý khi chọn danh mục chính
        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Lấy đối tượng Category từ spinner
                val selectedCategory = parent.getItemAtPosition(position) as Category
                if (selectedCategory.name == "Chi tiêu hằng ngày") {
                    loadSubCategories(categoryDAO.getDailyExpenseSubCategories())
                    spinnerSubCategory.visibility = View.VISIBLE
                } else {
                    spinnerSubCategory.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                spinnerSubCategory.visibility = View.GONE
            }
        }

        // DatePicker cho chọn ngày
        val editTextDate = findViewById<EditText>(R.id.editTextDate)
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(calendar.time)
        editTextDate.setText(currentDate)

        editTextDate.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                    editTextDate.setText(selectedDate)
                },
                year, month, day
            )
            datePickerDialog.show()
        }

        val buttonAddTransaction = findViewById<Button>(R.id.button_add_transaction)
        buttonAddTransaction.setOnClickListener {
            val selectedCategory = spinnerCategory.selectedItem as Category
            val amountText = findViewById<EditText>(R.id.editTextAmount).text.toString()
            val amount = amountText.toDoubleOrNull()
            val type = if (radioGroup.checkedRadioButtonId == R.id.radioIncome) "Thu" else "Chi"
            val date = editTextDate.text.toString()
            val note = findViewById<EditText>(R.id.editTextNote).text.toString()

            if (selectedCategory.name == "Chọn danh mục") {
                Toast.makeText(this, "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (amount == null || amount <= 0) {
                Toast.makeText(this, "Vui lòng nhập số tiền hợp lệ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (date.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ngày", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (note.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập ghi chú", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val subCategory = if (spinnerSubCategory.visibility == View.VISIBLE) {
                spinnerSubCategory.selectedItem.toString()
            } else {
                null
            }

            // Lấy icon từ danh mục đã chọn
            val icon = selectedCategory.icon

            // Cập nhật Transaction với icon từ danh mục
            val transaction = Transaction(
                amount = amount,
                date = date,
                category = selectedCategory.name,
                subCategory = subCategory ?: "",
                note = note,
                type = type,
                icon = icon // Sử dụng icon từ danh mục đã chọn
            )

            val success = transactionDAO.addTransaction(transaction)
            if (success != -1L) {
                Toast.makeText(this, "Giao dịch đã được thêm", Toast.LENGTH_SHORT).show()
                resetForm()
            } else {
                Toast.makeText(this, "Lỗi khi thêm giao dịch", Toast.LENGTH_SHORT).show()
            }
        }


        val buttonReset = findViewById<Button>(R.id.button_reset)
        buttonReset.setOnClickListener {
            resetForm()
        }

        val imageViewMoreOptions = findViewById<ImageView>(R.id.imageViewMoreOptions)
        imageViewMoreOptions.setOnClickListener {
            val popupMenu = PopupMenu(this, imageViewMoreOptions)
            popupMenu.menuInflater.inflate(R.menu.menu_more_options, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_add_category -> {
                        val intent = Intent(this, AddCategory::class.java)
                        startActivityForResult(intent, 1)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            updateCategorySpinnerWithIcon()
        }
    }

    // Cập nhật Spinner với icon và tên danh mục
    private fun updateCategorySpinnerWithIcon() {
        val categories = categoryDAO.getAllCategories() // Lấy tất cả danh mục từ cơ sở dữ liệu
        val adapter = object : ArrayAdapter<Category>(this, R.layout.spinner_item_with_icon, categories) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val imageView = view.findViewById<ImageView>(R.id.imageViewIcon)
                val textView = view.findViewById<TextView>(R.id.textViewCategoryName)
                val category = getItem(position)

                if (category != null) {
                    imageView.setImageResource(category.icon)
                    textView.text = category.name
                }
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val imageView = view.findViewById<ImageView>(R.id.imageViewIcon)
                val textView = view.findViewById<TextView>(R.id.textViewCategoryName)
                val category = getItem(position)

                if (category != null) {
                    imageView.setImageResource(category.icon)
                    textView.text = category.name
                }
                return view
            }
        }

        spinnerCategory.adapter = adapter
    }

    private fun loadDefaultCategory() {
        val defaultOption = listOf(Category(id = 0, name = "Chọn danh mục", type = "default", icon = R.drawable.ic_launcher_background))  // Đảm bảo giá trị icon là kiểu Int
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, defaultOption)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter
    }


    private fun loadCategories(categories: List<Category>) {
        val adapter = object : ArrayAdapter<Category>(this, android.R.layout.simple_spinner_item, categories) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                return super.getView(position, convertView, parent)
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter
    }

    private fun loadSubCategories(subCategories: List<Category>) {
        val subCategoryNames = subCategories.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, subCategoryNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSubCategory.adapter = adapter
    }

    private fun resetForm() {
        findViewById<EditText>(R.id.editTextAmount).text.clear()
        findViewById<EditText>(R.id.editTextNote).text.clear()
        findViewById<RadioGroup>(R.id.radioGroupIncomeExpense).clearCheck()
        spinnerCategory.setSelection(0)
        spinnerSubCategory.visibility = View.GONE
        findViewById<EditText>(R.id.editTextDate).setText(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().time))
    }
}
