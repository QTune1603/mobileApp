package com.example.mobileapp.dao

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.mobileapp.model.Transaction

class TransactionDAO(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "transactions.db"
        private const val DATABASE_VERSION = 2

        const val TABLE_TRANSACTION = "Transactions"
        const val COLUMN_ID = "id"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_SUB_CATEGORY = "sub_category"
        const val COLUMN_AMOUNT = "amount"
        const val COLUMN_TYPE = "type"
        const val COLUMN_DATE = "date"
        const val COLUMN_ICON = "icon"  // Thêm cột icon
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Tạo bảng Transactions khi cơ sở dữ liệu được tạo
        val createTable = ("CREATE TABLE $TABLE_TRANSACTION ("
                + "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COLUMN_CATEGORY TEXT,"
                + "$COLUMN_SUB_CATEGORY TEXT,"
                + "$COLUMN_AMOUNT REAL,"
                + "$COLUMN_TYPE TEXT,"
                + "$COLUMN_DATE TEXT,"
                + "$COLUMN_ICON INTEGER)")  // Thêm cột icon
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // Thêm cột sub_category và icon vào bảng Transactions nếu đang ở phiên bản 1 và nâng cấp lên 2
            db.execSQL("ALTER TABLE $TABLE_TRANSACTION ADD COLUMN $COLUMN_SUB_CATEGORY TEXT DEFAULT ''")
            db.execSQL("ALTER TABLE $TABLE_TRANSACTION ADD COLUMN $COLUMN_ICON INTEGER DEFAULT 0")
        }
    }

    // Phương thức thêm một giao dịch mới
    fun addTransaction(transaction: Transaction): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CATEGORY, transaction.category)
            put(COLUMN_SUB_CATEGORY, transaction.subCategory)
            put(COLUMN_AMOUNT, transaction.amount)
            put(COLUMN_TYPE, transaction.type)
            put(COLUMN_DATE, transaction.date)
            put(COLUMN_ICON, transaction.icon)  // Lưu icon
        }
        return db.insert(TABLE_TRANSACTION, null, values)
    }

    // Phương thức lấy tất cả các giao dịch
    fun getAllTransactions(): List<Transaction> {
        val transactionList = mutableListOf<Transaction>()
        val selectQuery = "SELECT * FROM $TABLE_TRANSACTION"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val transaction = Transaction(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                    subCategory = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUB_CATEGORY)),
                    amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)),
                    type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)),
                    date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                    icon = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ICON)),  // Lấy icon
                    note = ""
                )
                transactionList.add(transaction)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return transactionList
    }

    // Phương thức tính tổng thu
    fun getTotalIncome(): Double {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT SUM($COLUMN_AMOUNT) FROM $TABLE_TRANSACTION WHERE $COLUMN_TYPE = 'Thu'", null)
        var totalIncome = 0.0
        if (cursor.moveToFirst()) {
            totalIncome = cursor.getDouble(0)
        }
        cursor.close()
        return totalIncome
    }

    // Phương thức tính tổng chi
    fun getTotalExpense(): Double {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT SUM($COLUMN_AMOUNT) FROM $TABLE_TRANSACTION WHERE $COLUMN_TYPE = 'Chi'", null)
        var totalExpense = 0.0
        if (cursor.moveToFirst()) {
            totalExpense = cursor.getDouble(0)
        }
        cursor.close()
        return totalExpense
    }

    // Phương thức lấy giao dịch của ngày cụ thể
    fun getTransactionsForDay(date: String): List<Transaction> {
        val transactionList = mutableListOf<Transaction>()
        val selectQuery = "SELECT * FROM $TABLE_TRANSACTION WHERE $COLUMN_DATE = ?"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, arrayOf(date))

        if (cursor.moveToFirst()) {
            do {
                val transaction = Transaction(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                    subCategory = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUB_CATEGORY)),
                    amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)),
                    type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)),
                    date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                    icon = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ICON)),
                    note = ""
                )
                transactionList.add(transaction)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return transactionList
    }
}
