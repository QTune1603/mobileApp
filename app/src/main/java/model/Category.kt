package com.example.mobileapp.model

data class Category(
    val id: Int,
    val name: String,
    val type: String,
    val icon: Int
) {
    override fun toString(): String {
        return name  // Chỉ hiển thị tên danh mục khi sử dụng đối tượng Category trong Spinner
    }
}
