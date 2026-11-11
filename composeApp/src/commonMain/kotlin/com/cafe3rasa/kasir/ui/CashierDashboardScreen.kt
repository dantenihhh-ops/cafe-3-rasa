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
import com.cafe3rasa.kasir.model.PaymentBillStatus
import com.cafe3rasa.kasir.state.CashierDashboardState
import com.cafe3rasa.kasir.state.MobileNavigationTab
import com.cafe3rasa.kasir.theme.KasirColors
import com.cafe3rasa.kasir.theme.KasirTypography
import com.cafe3rasa.kasir.ui.components.*
import kotlinx.coroutines.launch

@Composable
fun CashierDashboardScreen(
    state: CashierDashboardState = remember { CashierDashboardState() },
    onOpenAdmin: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

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
                    }
                )
            },
            bottomBar = {
                PaymentSummaryBar(
                    itemCount = state.totalItemCount,
                    totalRupiah = state.totalRupiah,
                    paymentMethod = "TUNAI (CASH)",
                    onConfirm = {
                        coroutineScope.launch {
                            state.paymentStatus = PaymentBillStatus.LUNAS
                            snackbarHostState.showSnackbar("Pesanan Meja ${state.selectedTableNumber} berhasil dikonfirmasi!")
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
                onRequestHandled = { state.dismissRequest(it) }
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
            // Selected Table & Order List
            OrderDetailSection(
                tableNumber = state.selectedTableNumber,
                isOccupied = state.selectedTableOccupied,
                orderItems = state.orderItems,
                onToggleOccupancy = { state.toggleOccupancy() },
                onClearTable = { state.clearTable() },
                onChangeTable = { state.showChangeTableDialog = true },
                onIncrementQty = { state.incrementQuantity(it) },
                onDecrementQty = { state.decrementQuantity(it) }
            )

            // Quick Menu Section
            QuickMenuSection(
                menuItems = state.filteredMenu,
                activeCategory = state.activeMenuCategory,
                onCategorySelected = { state.activeMenuCategory = it },
                onAddItem = { state.addMenuItemToOrder(it) }
            )

            // Payment Status Selection
            PaymentStatusCard(
                tableNumber = state.selectedTableNumber,
                currentStatus = state.paymentStatus,
                onStatusChanged = { state.paymentStatus = it }
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
                onRequestHandled = { state.dismissRequest(it) }
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
            OrderDetailSection(
                tableNumber = state.selectedTableNumber,
                isOccupied = state.selectedTableOccupied,
                orderItems = state.orderItems,
                onToggleOccupancy = { state.toggleOccupancy() },
                onClearTable = { state.clearTable() },
                onChangeTable = { state.showChangeTableDialog = true },
                onIncrementQty = { state.incrementQuantity(it) },
                onDecrementQty = { state.decrementQuantity(it) }
            )

            QuickMenuSection(
                menuItems = state.filteredMenu,
                activeCategory = state.activeMenuCategory,
                onCategorySelected = { state.activeMenuCategory = it },
                onAddItem = { state.addMenuItemToOrder(it) }
            )

            PaymentStatusCard(
                tableNumber = state.selectedTableNumber,
                currentStatus = state.paymentStatus,
                onStatusChanged = { state.paymentStatus = it }
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
                        onRequestHandled = { state.dismissRequest(it) }
                    )
                }

                MobileNavigationTab.ORDER_DETAIL -> {
                    OrderDetailSection(
                        tableNumber = state.selectedTableNumber,
                        isOccupied = state.selectedTableOccupied,
                        orderItems = state.orderItems,
                        onToggleOccupancy = { state.toggleOccupancy() },
                        onClearTable = { state.clearTable() },
                        onChangeTable = { state.showChangeTableDialog = true },
                        onIncrementQty = { state.incrementQuantity(it) },
                        onDecrementQty = { state.decrementQuantity(it) }
                    )

                    PaymentStatusCard(
                        tableNumber = state.selectedTableNumber,
                        currentStatus = state.paymentStatus,
                        onStatusChanged = { state.paymentStatus = it }
                    )
                }

                MobileNavigationTab.QUICK_MENU -> {
                    QuickMenuSection(
                        menuItems = state.filteredMenu,
                        activeCategory = state.activeMenuCategory,
                        onCategorySelected = { state.activeMenuCategory = it },
                        onAddItem = {
                            state.addMenuItemToOrder(it)
                        }
                    )
                }
            }
        }
    }
}
