package com.cafe3rasa.kasir.data

import kotlinx.serialization.Serializable

// ─── Auth ─────────────────────────────────────────────────────────────────

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class UserDto(
    val id: String,
    val username: String,
    val fullName: String,
    val role: String,         // "KASIR" | "ADMIN"
    val isActive: Boolean = true
)

@Serializable
data class LoginResponse(
    val token: String,
    val user: UserDto
)

// ─── Menu ─────────────────────────────────────────────────────────────────

@Serializable
data class MenuDto(
    val id: String,
    val name: String,
    val categoryId: String = "",
    val categoryName: String = "",
    val price: Long = 0,
    val image: String? = null,
    val description: String = "",
    val isAvailable: Boolean = true,
    val filters: List<String> = emptyList()
)

// ─── Pesanan ──────────────────────────────────────────────────────────────

@Serializable
data class OrderItemDto(
    val id: String,
    val menuItemId: String? = null,
    val name: String,
    val categoryName: String = "",
    val quantity: Int,
    val unitPrice: Long,
    val subtotal: Long = 0,
    val note: String? = null
)

@Serializable
data class OrderDto(
    val id: String,
    val orderNumber: String,
    val tableId: Int = 0,
    val tableNumber: String = "",
    val status: String = "MENUNGGU_KONFIRMASI",
    val paymentBillStatus: String = "BELUM_BAYAR",
    val paymentMethod: String = "BELUM_DIPILIH",
    val orderSource: String = "EMENU",
    val customerNotes: String? = null,
    val subtotal: Long = 0,
    val tax: Long = 0,
    val total: Long = 0,
    val itemCount: Int = 0,
    val items: List<OrderItemDto> = emptyList(),
    val createdAt: String? = null,
    val confirmedAt: String? = null,
    val paidAt: String? = null
)

// ─── Meja ─────────────────────────────────────────────────────────────────

@Serializable
data class TableDto(
    val id: Int,
    val number: String,
    val area: String = "Indoor",
    val capacity: Int = 4,
    val status: String = "KOSONG",
    val activeSince: String? = null,
    val currentOrder: OrderDto? = null
)

// ─── Permintaan Bantuan ───────────────────────────────────────────────────

@Serializable
data class ServiceRequestDto(
    val id: String,
    val tableNumber: String,
    val type: String = "LAINNYA",
    val message: String? = null,
    val status: String = "MENUNGGU",
    val responseMessage: String? = null,
    val createdAt: String? = null,
    val confirmedAt: String? = null
)

// ─── Body request ─────────────────────────────────────────────────────────

@Serializable
data class NewItemBody(
    val menuItemId: String,
    val quantity: Int,
    val note: String? = null
)

@Serializable
data class AddItemsBody(val items: List<NewItemBody>)

@Serializable
data class KasirOrderBody(
    val tableNumber: String,
    val items: List<NewItemBody>
)

@Serializable
data class UpdateItemBody(val quantity: Int)

@Serializable
data class PaymentBody(
    val paymentMethod: String,          // "CASH" | "TRANSFER"
    val paymentBillStatus: String       // "LUNAS" | "BELUM_BAYAR"
)

@Serializable
data class TableStatusBody(val status: String)

@Serializable
data class MoveTableBody(val targetTableId: Int)

@Serializable
data class RespondBody(val responseMessage: String)

// ─── Panel Admin ──────────────────────────────────────────────────────────

@Serializable
data class CategoryDto(
    val id: String,
    val name: String,
    val slug: String = "",
    val displayOrder: Int = 0,
    val menuCount: Int = 0
)

@Serializable
data class CategoryBody(val name: String)

@Serializable
data class MenuBody(
    val name: String,
    val categoryId: String,
    val price: Long,
    val description: String = "",
    val filters: List<String> = emptyList(),
    val image: String? = null,
    val isAvailable: Boolean = true
)

@Serializable
data class TableBody(
    val number: String,
    val area: String,
    val capacity: Int
)

@Serializable
data class UserCreateBody(
    val username: String,
    val fullName: String,
    val password: String
)

@Serializable
data class UserUpdateBody(
    val fullName: String? = null,
    val password: String? = null
)

@Serializable
data class TopMenuDto(val name: String, val quantity: Int)

@Serializable
data class ReportDto(
    val period: String = "HARIAN",
    val offset: Int = 0,
    val rangeLabel: String = "",
    val labels: List<String> = emptyList(),
    val values: List<Long> = emptyList(),
    val totalRevenue: Long = 0,
    val orderCount: Int = 0,
    val avgPerOrder: Long = 0,
    val delta: String = "",
    val topMenus: List<TopMenuDto> = emptyList(),
    val orders: List<OrderDto> = emptyList()      // log pesanan pada periode ini
)

@Serializable
data class ImageUploadBody(val data: String)   // isi foto dalam base64

/** Ubah data produk (tanpa foto & status tersedia) */
@Serializable
data class MenuUpdateBody(
    val name: String,
    val categoryId: String,
    val price: Long,
    val description: String = "",
    val filters: List<String> = emptyList()
)

// ─── Rating & Ulasan ──────────────────────────────────────────────────────

@Serializable
data class ReviewSummaryDto(val totalReviews: Int = 0, val avgRating: Double = 0.0, val withComment: Int = 0)

@Serializable
data class ReviewStarCountDto(val stars: Int, val count: Int = 0)

@Serializable
data class ReviewAspectDto(val name: String, val count: Int = 0, val percent: Int = 0, val avgRating: Double = 0.0)

@Serializable
data class ReviewDto(
    val id: String,
    val tableNumber: String = "",
    val rating: Int = 0,
    val aspects: List<String> = emptyList(),
    val comment: String? = null,
    val createdAt: String? = null
)

@Serializable
data class ReviewsDto(
    val summary: ReviewSummaryDto = ReviewSummaryDto(),
    val distribution: List<ReviewStarCountDto> = emptyList(),
    val aspects: List<ReviewAspectDto> = emptyList(),
    val reviews: List<ReviewDto> = emptyList()
)
