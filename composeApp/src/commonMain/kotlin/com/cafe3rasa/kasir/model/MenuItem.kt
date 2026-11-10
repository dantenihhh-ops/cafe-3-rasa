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
    val imageResName: String? = null
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
