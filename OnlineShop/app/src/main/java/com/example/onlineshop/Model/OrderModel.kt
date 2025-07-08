package com.example.onlineshop.Model

import java.io.Serializable
import java.util.UUID

data class OrderModel (
    val id: String = UUID.randomUUID().toString(),
    val items: List<ItemsModel>,
    val name: String,
    val phone: String,
    val address: String,
    val paymentMethod: String,
    val time: Long = System.currentTimeMillis(),
    var status: String = "Đang xử lý",
    val userEmail: String,
    var paymentStatus: String = "Chờ chuyển khoản" // "Đã thanh toán" nếu COD
) : Serializable