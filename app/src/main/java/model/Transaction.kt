package com.example.mobileapp.model

data class Transaction(
    val id: Long = 0,
    val amount: Double,
    val date: String,
    val category: String,
    val subCategory: String = "",
    val note: String = "",
    val type: String,
    val icon: Int  // Đổi kiểu icon thành Int để lưu resource ID
)


