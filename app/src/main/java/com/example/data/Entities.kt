package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "shops")
data class Shop(
    @PrimaryKey val shopId: String,
    val name: String,
    val ownerName: String,
    val phone: String,
    val email: String,
    val role: String, // "Admin" or "Staff"
    val currency: String = "₹",
    val language: String = "en", // "en" or "hi"
    val thermalPrinterAddress: String = "",
    val isCloudSynced: Boolean = true
) : Serializable

@Entity(tableName = "products")
data class Product(
    @PrimaryKey val barcode: String,
    val name: String,
    val category: String,
    val subCategory: String = "",
    val sellingPrice: Double,
    val costPrice: Double,
    val stockQty: Int,
    val lowStockThreshold: Int = 10,
    val supplierName: String = "",
    val supplierPhone: String = "",
    val expiryDate: String = "", // e.g. "2027-12-31"
    val imageUrl: String = "",
    val sku: String = ""
) : Serializable

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey val phone: String,
    val name: String,
    val email: String = "",
    val loyaltyPoints: Int = 0,
    val creditBalance: Double = 0.0, // Udhaar tracking
    val lastActive: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey val orderId: String,
    val customerPhone: String = "", // Optional link to customer
    val subtotal: Double,
    val taxAmount: Double,
    val discountAmount: Double,
    val finalAmount: Double,
    val paymentMethod: String, // "CASH", "UPI", "CARD"
    val paymentStatus: String, // "PAID", "DEBT" (for Udhaar)
    val cashReceived: Double = 0.0,
    val changeGiven: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val billShared: Boolean = false,
    val staffName: String = "Admin"
) : Serializable

@Entity(tableName = "order_items")
data class OrderItem(
    @PrimaryKey(autoGenerate = true) val itemId: Long = 0,
    val orderId: String,
    val productBarcode: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val itemTotal: Double
) : Serializable

@Entity(tableName = "ai_chat_logs")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String, // "USER" or "AI"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable
