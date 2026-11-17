package com.cafe3rasa.kasir.ui.admin

import androidx.compose.ui.graphics.Color
import com.cafe3rasa.kasir.model.MenuCategory
import com.cafe3rasa.kasir.theme.KasirColors

/**
 * Warna badge per kategori menu (tipe [MenuCategory]).
 * Dipakai `KasirMenuScreen`; versi `String` ada di `AdminModels.kt`.
 */
internal fun categoryColor(category: MenuCategory): Color = when (category) {
    MenuCategory.MAKANAN -> KasirColors.PrimaryTeal
    MenuCategory.MINUMAN -> KasirColors.AccentRust
    MenuCategory.CAMILAN -> Color(0xFFB4543C)
    MenuCategory.SEMUA -> KasirColors.TextMuted
}
