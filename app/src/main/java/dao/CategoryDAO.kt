package com.example.mobileapp.dao

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.mobileapp.R
import com.example.mobileapp.model.Category

class CategoryDAO(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "expense_manager.db"
        const val DATABASE_VERSION = 2
        const val TABLE_CATEGORY = "Category"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_TYPE = "type"
        const val COLUMN_ICON = "icon"
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.d("CategoryDAO", "Creating Category table")
        val createTable = ("CREATE TABLE $TABLE_CATEGORY ("
                + "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_NAME TEXT, "
                + "$COLUMN_TYPE TEXT, "
                + "$COLUMN_ICON INTEGER)")
        db.execSQL(createTable)

        insertDefaultCategories(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // Chỉ thêm cột mới nếu phiên bản cũ nhỏ hơn 2
            db.execSQL("ALTER TABLE $TABLE_CATEGORY ADD COLUMN $COLUMN_ICON INTEGER DEFAULT 0")
        }
    }





    // Thêm danh mục mặc định với icon khi tạo cơ sở dữ liệu lần đầu
    private fun insertDefaultCategories(db: SQLiteDatabase) {
        val defaultCategories = listOf(
            Category(0, "Lương", "Thu", R.drawable.ic_luong),
            Category(0, "Làm thêm", "Thu", R.drawable.ic_lamthem),
            Category(0, "Học bổng", "Thu", R.drawable.ic_luong),
            Category(0, "Bố mẹ cho", "Thu", R.drawable.ic_luong),
            Category(0, "Quà tặng", "Thu", R.drawable.ic_quatang),
            Category(0, "Học phí", "Chi", R.drawable.ic_hocphi),
            Category(0, "Chi tiêu hằng ngày", "Chi", R.drawable.ic_luong),
            Category(0, "Hiếu hỉ", "Chi", R.drawable.ic_luong),
            Category(0, "Quà tặng", "Chi", R.drawable.ic_quatang),
            Category(0, "Giải trí", "Chi", R.drawable.ic_giaitri)
        )

        for (category in defaultCategories) {
            val values = ContentValues()
            values.put(COLUMN_NAME, category.name)
            values.put(COLUMN_TYPE, category.type)
            values.put(COLUMN_ICON, category.icon)
            db.insert(TABLE_CATEGORY, null, values)
        }
        Log.d("CategoryDAO", "Default categories inserted")
    }

    // Lấy danh mục con của "Chi tiêu hằng ngày"
    fun getDailyExpenseSubCategories(): List<Category> {
        val subCategories = listOf(
            Category(0, "Tiền nhà", "Chi", R.drawable.ic_tiennha),
            Category(0, "Tiền điện", "Chi", R.drawable.ic_tiendien),
            Category(0, "Tiền nước", "Chi", R.drawable.ic_tiennuoc),
            Category(0, "Tiền điện thoại", "Chi", R.drawable.ic_tiendienthoai),
            Category(0, "Tiền ăn", "Chi", R.drawable.ic_tienan),
            Category(0, "Tiền đi chợ", "Chi", R.drawable.ic_tiendicho)
        )

        return subCategories
    }

    // Thêm một danh mục mới
    fun addCategory(category: Category): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_NAME, category.name)
        values.put(COLUMN_TYPE, category.type)
        values.put(COLUMN_ICON, category.icon)
        val result = db.insert(TABLE_CATEGORY, null, values)
        Log.d("CategoryDAO", "Added new category: ${category.name}, Result: $result")
        return result
    }

    // Lấy tất cả các danh mục
    fun getAllCategories(): List<Category> {
        val db = this.readableDatabase
        val categories = mutableListOf<Category>()
        val cursor = db.query(TABLE_CATEGORY, null, null, null, null, null, null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
                val type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE))
                val icon = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ICON))
                categories.add(Category(id, name, type, icon))
            } while (cursor.moveToNext())
        }
        cursor.close()

        Log.d("CategoryDAO", "Categories retrieved: ${categories.size}")
        return categories
    }

    // Lấy danh mục Thu
    fun getIncomeCategories(): List<Category> {
        return getAllCategories().filter { it.type == "Thu" }
    }

    // Lấy danh mục Chi
    fun getExpenseCategories(): List<Category> {
        return getAllCategories().filter { it.type == "Chi" }
    }
}
