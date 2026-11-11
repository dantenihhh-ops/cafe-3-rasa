package com.cafe3rasa.kasir.model

enum class MenuCategory(val displayName: String) {
    SEMUA("Semua"),
    MAKANAN("Makanan"),
    MINUMAN("Minuman"),
    CAMILAN("Camilan")
}

data class MenuItemModel(
    val id: String,
    val name: String,
    val category: MenuCategory,
    val price: Long,
    val imageResName: String? = null,
    val description: String = "",
    val isAvailable: Boolean = true
)

data class OrderItemModel(
    val id: String,
    val name: String,
    val category: MenuCategory,
    val unitPrice: Long,
    val quantity: Int,
    val note: String? = null,
    val imageResName: String? = null
) {
    val subtotal: Long get() = unitPrice * quantity
}

enum class PaymentBillStatus {
    BELUM_BAYAR,
    LUNAS
}

/** Status pembayaran untuk log admin (08). */
enum class PaymentMethod(val label: String) {
    CASH("Cash"),
    TRANSFER("Transfer")
}

/** Status pesanan untuk log admin (08). */
enum class OrderStatus(val label: String) {
    SELESAI("Selesai"),
    DIPROSES("Diproses"),
    DIBATALKAN("Dibatalkan")
}

data class OrderLogEntry(
    val id: String,
    val time: String,
    val tableNumber: String,
    val itemSummary: String,
    val total: Long,
    val paymentMethod: PaymentMethod,
    val status: OrderStatus
)
