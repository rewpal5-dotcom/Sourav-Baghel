package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One placed nursery order. Saved locally so the customer can see order history
 * and so the order survives app restarts.
 *
 * status: "PLACED" (cash on delivery) or "PAYMENT_PENDING" (UPI app opened, payment
 * not yet confirmed — confirming it needs a server, see FIXES.md).
 */
@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderCode: String,
    val itemsSummary: String,
    val itemCount: Int,
    val amount: Double,
    val address: String,
    val phone: String,
    val paymentMode: String,
    val status: String,
    val createdAt: Long = System.currentTimeMillis()
)
