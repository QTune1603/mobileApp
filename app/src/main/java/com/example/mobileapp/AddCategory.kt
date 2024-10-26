package com.example.mobileapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.mobileapp.dao.CategoryDAO
import com.example.mobileapp.model.Category

class AddCategory : AppCompatActivity() {
    private lateinit var categoryDAO: CategoryDAO
    private lateinit var spinnerType: Spinner
    private lateinit var editTextCategoryName: EditText
    private lateinit var spinnerIcon: Spinner
    private lateinit var buttonReset: Button
    private lateinit var buttonAddCategory: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_category)

        // Khởi tạo CategoryDAO với context hiện tại
        categoryDAO = CategoryDAO(this)

        spinnerType = findViewById(R.id.spinnerType)
        spinnerIcon = findViewById(R.id.spinnerIcon)
        editTextCategoryName = findViewById(R.id.editTextCategoryName)
        buttonReset = findViewById(R.id.buttonReset)
        buttonAddCategory = findViewById(R.id.buttonAddCategory)

        // Tạo danh sách cho Spinner Type (Thu/Chi)
        val typeOptions = listOf("Thu", "Chi")
        val typeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, typeOptions)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = typeAdapter

        // Tạo danh sách icon cho Spinner Icon (Sử dụng icon từ Vector Assets)
        val icons = listOf(
            R.drawable.ic_luong,
            R.drawable.ic_lamthem,
            R.drawable.ic_hocphi,
            R.drawable.ic_quatang,
            R.drawable.ic_giaitri,
            R.drawable.ic_choithethao,
            R.drawable.ic_tienan,
            R.drawable.ic_tiennha,
            R.drawable.ic_tiennuoc,
            R.drawable.ic_tiendicho
        )

        // Custom ArrayAdapter for displaying only icons
        val iconAdapter = object : ArrayAdapter<Int>(this, R.layout.spinner_item_with_icon, icons) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                return createCustomView(position, convertView, parent)
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                return createCustomView(position, convertView, parent)
            }

            private fun createCustomView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.spinner_item_with_icon, parent, false)
                val imageView = view.findViewById<ImageView>(R.id.imageViewIcon)

                // Set only the icon, don't set text for the label
                imageView.setImageResource(icons[position])

                // Hide the TextView (optional, if you don’t want to remove the text view from the layout)
                val textView = view.findViewById<TextView>(R.id.textViewCategoryName)
                textView.visibility = View.GONE

                return view
            }
        }

        spinnerIcon.adapter = iconAdapter

        // Xử lý sự kiện khi bấm nút Reset
        buttonReset.setOnClickListener {
            resetForm()
        }

        buttonAddCategory.setOnClickListener {
            val categoryName = editTextCategoryName.text.toString()
            val categoryType = spinnerType.selectedItem.toString()
            val iconPosition = spinnerIcon.selectedItemPosition
            val selectedIcon = icons[iconPosition]

            if (categoryName.isNotEmpty()) {
                // Tạo đối tượng Category mới và thêm vào cơ sở dữ liệu
                val newCategory = Category(id = 0, name = categoryName, type = categoryType, icon = selectedIcon)
                val success = categoryDAO.addCategory(newCategory)

                if (success != -1L) {
                    Toast.makeText(this, "Đã thêm chủ đề mới", Toast.LENGTH_SHORT).show()
                    resetForm()  // Reset the form without going back
                } else {
                    Toast.makeText(this, "Lỗi khi thêm chủ đề", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Vui lòng nhập tên chủ đề", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Hàm reset các trường nhập liệu
    private fun resetForm() {
        editTextCategoryName.text.clear()
        spinnerType.setSelection(0)
        spinnerIcon.setSelection(0)
    }
}
