package com.cafe3rasa.kasir.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cafe3rasa.kasir.model.*
import com.cafe3rasa.kasir.state.CashierDashboardState
import com.cafe3rasa.kasir.state.MobileNavigationTab
import com.cafe3rasa.kasir.theme.KasirColors
import com.cafe3rasa.kasir.theme.KasirTypography
import com.cafe3rasa.kasir.state.AdminPanelState
import com.cafe3rasa.kasir.ui.components.*
import kotlinx.coroutines.launch

@Composable
fun CashierDashboardScreen(
    state: CashierDashboardState = remember { CashierDashboardState() },
    onOpenAdmin: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showKasirMenu by remember { mutableStateOf(false) }
    val menuState = remember { AdminPanelState() }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(KasirColors.Background)
    ) {
        val screenWidth = maxWidth
        val isCompact = screenWidth < 680.dp
        val isMedium = screenWidth in 680.dp..1024.dp
        val isExpanded = screenWidth > 1024.dp

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                CashierHeader(
                    isCompact = isCompact,
                    activeTableCount = state.tables.count { it.status != com.cafe3rasa.kasir.model.TableStatus.KOSONG },
                    currentTimeText = "09:41 WIB",
                    onNotificationClick = {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Notifikasi: ${state.requests.size} permintaan meja menunggu.")
                        }
                    },
                    onAdminClick = {
                        onOpenAdmin()
                    },
                    onLogout = { onLogout() },
                    onOpenMenu = { showKasirMenu = true },
                    onDashboard = { showKasirMenu = false }
                )
            },
            bottomBar = {
                val methodLabel = if (state.selectedPaymentMethod != PaymentMethod.BELUM_DIPILIH)
                    state.selectedPaymentMethod.label
                else "TUNAI (CASH)"

                PaymentSummaryBar(
                    itemCount = state.totalItemCount,
                    totalRupiah = state.totalRupiah,
                    paymentMethod = methodLabel,
                    onConfirm = {
                        coroutineScope.launch {
                            val finalMethod = if (state.selectedPaymentMethod != PaymentMethod.BELUM_DIPILIH)
                                state.selectedPaymentMethod
                            else PaymentMethod.CASH
                            state.updatePaymentStatus(PaymentBillStatus.LUNAS, finalMethod)
                            snackbarHostState.showSnackbar("Pesanan Meja ${state.selectedTableNumber} lunas (${finalMethod.label})!")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when {
                    isExpanded -> {
                        // Desktop / POS Terminal: Full 2-column layout matching reference mockup
                        ExpandedDashboardLayout(state = state)
                    }
                    isMedium -> {
                        // Tablet / Medium Screen: Adaptive 2-column layout
                        MediumDashboardLayout(state = state)
                    }
                    else -> {
                        // Mobile Phone: Segmented Tab View with Bottom Sticky Summary
                        CompactDashboardLayout(state = state)
                    }
                }
            }

            // Overlay Kelola Menu Kasir
            if (showKasirMenu) {
                Box(modifier = Modifier.fillMaxSize().background(KasirColors.Background)) {
                    KasirMenuScreen(
                        state = menuState,
                        onBack = { showKasirMenu = false },
                        onLogout = onLogout
                    )
                }
            }

            // Dialog Ganti Meja (Pindah ke meja yang berstatus kosong/belum aktif)
            if (state.showChangeTableDialog) {
                ChangeTableDialog(
                    currentTableNumber = state.selectedTableNumber,
                    emptyTables = state.emptyTables,
                    onTableSelected = { targetNumber ->
                        state.switchTableTo(targetNumber)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Pesanan berhasil dipindahkan ke Meja $targetNumber!")
                        }
                    },
                    onDismiss = { state.showChangeTableDialog = false }
                )
            }
        }
    }
}

/**
 * Layout untuk Desktop / Terminal POS (> 1024dp)
 * Persis seperti pada mockup referensi Kasir-Dashboard.dc.html
 */
@Composable
private fun ExpandedDashboardLayout(
    state: CashierDashboardState
) {
    val leftScrollState = rememberScrollState()
    val rightScrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(KasirColors.Background)
    ) {
        // Left Panel (Aside: 384dp)
        Column(
            modifier = Modifier
                .width(384.dp)
                .fillMaxHeight()
                .background(KasirColors.Surface)
                .border(width = 1.dp, color = KasirColors.Border)
                .verticalScroll(leftScrollState)
                .padding(18.dp, 16.dp, 16.dp, 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Table Map
            TableMapSection(
                tables = state.filteredTables,
                selectedTableNumber = state.selectedTableNumber,
                activeFilter = state.activeTableFilter,
                onFilterSelected = { state.activeTableFilter = it },
                onTableSelected = { state.selectTable(it) }
            )

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(KasirColors.Border)
            )

            // Table Requests
            TableRequestSection(
                requests = state.requests,
                onRequestConfirm = { id, msg -> state.confirmRequest(id, msg) },
                onRequestDismiss = { state.dismissRequest(it) }
            )
        }

        // Right Panel (Main Content Workspace)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rightScrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            val currentTable = state.tables.firstOrNull { it.number == state.selectedTableNumber }

            // Selected Table & Order List
            OrderDetailSection(
                tableNumber = state.selectedTableNumber,
                isOccupied = state.selectedTableOccupied,
                tableStatus = currentTable?.status ?: TableStatus.TERISI_BELUM_BAYAR,
                orderSource = currentTable?.orderSource ?: OrderSource.KASIR,
                paymentMethod = currentTable?.paymentMethod ?: PaymentMethod.BELUM_DIPILIH,
                orderItems = state.orderItems,
                onConfirmNewOrder = { state.confirmNewOrder(state.selectedTableNumber) },
                onToggleOccupancy = { state.toggleOccupancy() },
                onClearTable = { state.clearTable() },
                onChangeTable = { state.showChangeTableDialog = true },
                onIncrementQty = { state.incrementQuantity(it) },
                onDecrementQty = { state.decrementQuantity(it) }
            )

            // Quick Menu Section dengan Filter Kategori & Label Opsional
            QuickMenuSection(
                menuItems = state.filteredMenu,
                activeCategory = state.activeMenuCategory,
                activeTagFilter = state.activeTagFilter,
                availableTags = state.availableTags,
                searchQuery = state.menuSearchQuery,
                onCategorySelected = { state.activeMenuCategory = it },
                onTagFilterSelected = { state.activeTagFilter = it },
                onSearchQueryChange = { state.menuSearchQuery = it },
                onAddItem = { state.addMenuItemToOrder(it) }
            )

            // Payment Status Selection
            PaymentStatusCard(
                tableNumber = state.selectedTableNumber,
                currentStatus = state.paymentStatus,
                currentMethod = state.selectedPaymentMethod,
                onStatusChanged = { newStatus ->
                    state.updatePaymentStatus(newStatus, state.selectedPaymentMethod)
                },
                onMethodSelected = { newMethod ->
                    state.updatePaymentStatus(PaymentBillStatus.LUNAS, newMethod)
                }
            )
        }
    }
}

/**
 * Layout untuk Tablet (680dp - 1024dp)
 */
@Composable
private fun MediumDashboardLayout(
    state: CashierDashboardState
) {
    val leftScrollState = rememberScrollState()
    val rightScrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(KasirColors.Background)
    ) {
        // Left Column (320dp)
        Column(
            modifier = Modifier
                .width(320.dp)
                .fillMaxHeight()
                .background(KasirColors.Surface)
                .border(width = 1.dp, color = KasirColors.Border)
                .verticalScroll(leftScrollState)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TableMapSection(
                tables = state.filteredTables,
                selectedTableNumber = state.selectedTableNumber,
                activeFilter = state.activeTableFilter,
                onFilterSelected = { state.activeTableFilter = it },
                onTableSelected = { state.selectTable(it) }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(KasirColors.Border)
            )

            TableRequestSection(
                requests = state.requests,
                onRequestConfirm = { id, msg -> state.confirmRequest(id, msg) },
                onRequestDismiss = { state.dismissRequest(it) }
            )
        }

        // Right Column
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rightScrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val currentTable = state.tables.firstOrNull { it.number == state.selectedTableNumber }

            OrderDetailSection(
                tableNumber = state.selectedTableNumber,
                isOccupied = state.selectedTableOccupied,
                tableStatus = currentTable?.status ?: TableStatus.TERISI_BELUM_BAYAR,
                orderSource = currentTable?.orderSource ?: OrderSource.KASIR,
                paymentMethod = currentTable?.paymentMethod ?: PaymentMethod.BELUM_DIPILIH,
                orderItems = state.orderItems,
                onConfirmNewOrder = { state.confirmNewOrder(state.selectedTableNumber) },
                onToggleOccupancy = { state.toggleOccupancy() },
                onClearTable = { state.clearTable() },
                onChangeTable = { state.showChangeTableDialog = true },
                onIncrementQty = { state.incrementQuantity(it) },
                onDecrementQty = { state.decrementQuantity(it) }
            )

            QuickMenuSection(
                menuItems = state.filteredMenu,
                activeCategory = state.activeMenuCategory,
                activeTagFilter = state.activeTagFilter,
                availableTags = state.availableTags,
                searchQuery = state.menuSearchQuery,
                onCategorySelected = { state.activeMenuCategory = it },
                onTagFilterSelected = { state.activeTagFilter = it },
                onSearchQueryChange = { state.menuSearchQuery = it },
                onAddItem = { state.addMenuItemToOrder(it) }
            )

            PaymentStatusCard(
                tableNumber = state.selectedTableNumber,
                currentStatus = state.paymentStatus,
                currentMethod = state.selectedPaymentMethod,
                onStatusChanged = { newStatus ->
                    state.updatePaymentStatus(newStatus, state.selectedPaymentMethod)
                },
                onMethodSelected = { newMethod ->
                    state.updatePaymentStatus(PaymentBillStatus.LUNAS, newMethod)
                }
            )
        }
    }
}

/**
 * Layout untuk HP / Smartphone (< 680dp)
 * Navigasi tab responsif agar mudah digunakan dengan 1 tangan oleh kasir.
 */
@Composable
private fun CompactDashboardLayout(
    state: CashierDashboardState
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KasirColors.Background)
    ) {
        // Tab Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(KasirColors.Surface)
                .border(1.dp, KasirColors.Border)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MobileNavigationTab.values().forEach { tab ->
                val isSelected = tab == state.mobileTab
                val badgeCount = when (tab) {
                    MobileNavigationTab.TABLE_MAP -> state.requests.size
                    MobileNavigationTab.ORDER_DETAIL -> state.totalItemCount
                    MobileNavigationTab.QUICK_MENU -> null
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) KasirColors.PrimaryTeal else KasirColors.Surface)
                        .border(
                            1.dp,
                            if (isSelected) KasirColors.PrimaryTeal else KasirColors.BorderLight,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { state.mobileTab = tab },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = tab.label,
                            style = KasirTypography.labelLarge.copy(
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) KasirColors.TextWhite else KasirColors.TextSecondary
                            )
                        )

                        if (badgeCount != null && badgeCount > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(if (isSelected) KasirColors.AccentRust else KasirColors.PrimaryDarkNavy)
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = badgeCount.toString(),
                                    style = KasirTypography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = KasirColors.TextWhite
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Tab Content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (state.mobileTab) {
                MobileNavigationTab.TABLE_MAP -> {
                    TableMapSection(
                        tables = state.filteredTables,
                        selectedTableNumber = state.selectedTableNumber,
                        activeFilter = state.activeTableFilter,
                        onFilterSelected = { state.activeTableFilter = it },
                        onTableSelected = {
                            state.selectTable(it)
                            state.mobileTab = MobileNavigationTab.ORDER_DETAIL
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    TableRequestSection(
                        requests = state.requests,
                        onRequestConfirm = { id, msg -> state.confirmRequest(id, msg) },
                onRequestDismiss = { state.dismissRequest(it) }
                    )
                }

                MobileNavigationTab.ORDER_DETAIL -> {
                    val currentTable = state.tables.firstOrNull { it.number == state.selectedTableNumber }

                    OrderDetailSection(
                        tableNumber = state.selectedTableNumber,
                        isOccupied = state.selectedTableOccupied,
                        tableStatus = currentTable?.status ?: TableStatus.TERISI_BELUM_BAYAR,
                        orderSource = currentTable?.orderSource ?: OrderSource.KASIR,
                        paymentMethod = currentTable?.paymentMethod ?: PaymentMethod.BELUM_DIPILIH,
                        orderItems = state.orderItems,
                        onConfirmNewOrder = { state.confirmNewOrder(state.selectedTableNumber) },
                        onToggleOccupancy = { state.toggleOccupancy() },
                        onClearTable = { state.clearTable() },
                        onChangeTable = { state.showChangeTableDialog = true },
                        onIncrementQty = { state.incrementQuantity(it) },
                        onDecrementQty = { state.decrementQuantity(it) }
                    )

                    PaymentStatusCard(
                        tableNumber = state.selectedTableNumber,
                        currentStatus = state.paymentStatus,
                        currentMethod = state.selectedPaymentMethod,
                        onStatusChanged = { newStatus ->
                            state.updatePaymentStatus(newStatus, state.selectedPaymentMethod)
                        },
                        onMethodSelected = { newMethod ->
                            state.updatePaymentStatus(PaymentBillStatus.LUNAS, newMethod)
                        }
                    )
                }

                MobileNavigationTab.QUICK_MENU -> {
                    QuickMenuSection(
                        menuItems = state.filteredMenu,
                        activeCategory = state.activeMenuCategory,
                        activeTagFilter = state.activeTagFilter,
                        availableTags = state.availableTags,
                        searchQuery = state.menuSearchQuery,
                        onCategorySelected = { state.activeMenuCategory = it },
                        onTagFilterSelected = { state.activeTagFilter = it },
                        onSearchQueryChange = { state.menuSearchQuery = it },
                        onAddItem = {
                            state.addMenuItemToOrder(it)
                        }
                    )
                }
            }
        }
    }
}


// ─── Preview Langsung (buka file ini → lihat preview di panel kanan) ─────────

@org.jetbrains.compose.ui.tooling.preview.Preview
@androidx.compose.runtime.Composable
private fun PreviewDashboardTablet() {
    com.cafe3rasa.kasir.theme.KasirCafeTheme {
        CashierDashboardScreen()
    }
}

@org.jetbrains.compose.ui.tooling.preview.Preview
@androidx.compose.runtime.Composable
private fun PreviewDashboardHP() {
    com.cafe3rasa.kasir.theme.KasirCafeTheme {
        CashierDashboardScreen()
    }
}
