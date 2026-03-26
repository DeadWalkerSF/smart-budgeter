package com.deadwalkersf.smartbudgeter.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.deadwalkersf.smartbudgeter.data.local.entity.CategoryEntity
import com.deadwalkersf.smartbudgeter.data.local.entity.RecurringExpenseEntity
import com.deadwalkersf.smartbudgeter.data.local.entity.VaultGoalEntity
import com.deadwalkersf.smartbudgeter.data.local.model.TransactionRecord
import com.deadwalkersf.smartbudgeter.domain.CurrencyOption
import com.deadwalkersf.smartbudgeter.domain.DateFilterPreset
import com.deadwalkersf.smartbudgeter.domain.ExportFormat
import com.deadwalkersf.smartbudgeter.domain.util.BiometricAuthManager
import com.deadwalkersf.smartbudgeter.domain.util.asCurrency
import com.deadwalkersf.smartbudgeter.domain.util.asPercent
import com.deadwalkersf.smartbudgeter.domain.util.toComposeColor
import com.deadwalkersf.smartbudgeter.domain.util.toDisplayDateTime
import com.deadwalkersf.smartbudgeter.ui.components.CategoryDonutChart
import com.deadwalkersf.smartbudgeter.ui.components.SpendingTrendChart
import com.deadwalkersf.smartbudgeter.ui.theme.BudgetGold
import com.deadwalkersf.smartbudgeter.ui.theme.Danger
import com.deadwalkersf.smartbudgeter.ui.theme.DeepBlack
import com.deadwalkersf.smartbudgeter.ui.theme.ElevatedBlack
import com.deadwalkersf.smartbudgeter.ui.theme.Graphite
import com.deadwalkersf.smartbudgeter.ui.theme.MutedText
import com.deadwalkersf.smartbudgeter.ui.theme.SoftGold
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

private enum class BudgetTab {
    HOME,
    HISTORY,
}

private sealed interface ActiveSheet {
    data class Transaction(val preselectedCategoryId: Long? = null) : ActiveSheet
    data class CategoryEditor(val category: CategoryEntity? = null) : ActiveSheet
    data object CurrencyPicker : ActiveSheet
}

private data class CategoryIconOption(
    val key: String,
    val label: String,
    val icon: ImageVector,
)

private val categoryIconOptions = listOf(
    CategoryIconOption("coffee", "Coffee", Icons.Default.LocalCafe),
    CategoryIconOption("dining", "Dining", Icons.Default.Restaurant),
    CategoryIconOption("entertainment", "Fun", Icons.Default.Movie),
    CategoryIconOption("groceries", "Groceries", Icons.Default.ShoppingCart),
    CategoryIconOption("rent", "Home", Icons.Default.Apartment),
    CategoryIconOption("shopping", "Shopping", Icons.Default.ShoppingBag),
    CategoryIconOption("subscriptions", "Bills", Icons.Default.ReceiptLong),
    CategoryIconOption("transport", "Travel", Icons.Default.DirectionsCar),
    CategoryIconOption("savings", "Vault", Icons.Default.Savings),
)

private val categoryColorOptions = listOf(
    "#D4AF37",
    "#F59E0B",
    "#EAB308",
    "#F97316",
    "#10B981",
    "#14B8A6",
    "#3B82F6",
    "#8B5CF6",
    "#EC4899",
)

@Composable
fun BudgeterRoot(
    activity: FragmentActivity,
    viewModel: BudgetViewModel = viewModel(),
) {
    val homeUiState by viewModel.homeUiState.collectAsStateWithLifecycle()
    val historyUiState by viewModel.historyUiState.collectAsStateWithLifecycle()
    val transactions = viewModel.pagedTransactions.collectAsLazyPagingItems()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }
    val biometricManager = remember(activity) { BiometricAuthManager(activity) }
    val biometricAvailable = remember(activity) { biometricManager.canAuthenticate() }

    var selectedTab by rememberSaveable { mutableStateOf(BudgetTab.HOME) }
    var activeSheet by remember { mutableStateOf<ActiveSheet?>(null) }
    var categoryToDelete by remember { mutableStateOf<CategoryEntity?>(null) }
    var isUnlocked by rememberSaveable { mutableStateOf(!biometricAvailable) }
    var hasAutoPrompted by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is UiEvent.Message -> snackbarHostState.showSnackbar(event.value)
                is UiEvent.Share -> {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = event.mimeType
                        putExtra(Intent.EXTRA_STREAM, event.uri)
                        putExtra(Intent.EXTRA_SUBJECT, event.title)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    activity.startActivity(Intent.createChooser(shareIntent, event.title))
                }
            }
        }
    }

    LaunchedEffect(biometricAvailable, isUnlocked, hasAutoPrompted) {
        if (biometricAvailable && !isUnlocked && !hasAutoPrompted) {
            hasAutoPrompted = true
            biometricManager.authenticate(
                onSuccess = { isUnlocked = true },
                onError = { message ->
                    scope.launch { snackbarHostState.showSnackbar(message) }
                },
            )
        }
    }

    if (!isUnlocked) {
        LockedScreen(
            snackbarHostState = snackbarHostState,
            onUnlock = {
                biometricManager.authenticate(
                    onSuccess = { isUnlocked = true },
                    onError = { message ->
                        scope.launch { snackbarHostState.showSnackbar(message) }
                    },
                )
            },
        )
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = DeepBlack,
        bottomBar = {
            NavigationBar(
                containerColor = ElevatedBlack,
                tonalElevation = 0.dp,
            ) {
                NavigationBarItem(
                    selected = selectedTab == BudgetTab.HOME,
                    onClick = { selectedTab = BudgetTab.HOME },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Home") },
                )
                NavigationBarItem(
                    selected = selectedTab == BudgetTab.HISTORY,
                    onClick = { selectedTab = BudgetTab.HISTORY },
                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                    label = { Text("History") },
                )
            }
        },
    ) { innerPadding ->
        when (selectedTab) {
            BudgetTab.HOME -> HomeScreen(
                modifier = Modifier.padding(innerPadding),
                uiState = homeUiState,
                onAddTransaction = { categoryId ->
                    activeSheet = if (homeUiState.categories.isEmpty()) {
                        ActiveSheet.CategoryEditor()
                    } else {
                        ActiveSheet.Transaction(categoryId)
                    }
                },
                onCreateCategory = { activeSheet = ActiveSheet.CategoryEditor() },
                onChooseCurrency = { activeSheet = ActiveSheet.CurrencyPicker },
                onEditCategory = { category -> activeSheet = ActiveSheet.CategoryEditor(category) },
                onExportCsv = { viewModel.exportMonthlySummary(ExportFormat.CSV) },
                onExportPdf = { viewModel.exportMonthlySummary(ExportFormat.PDF) },
            )

            BudgetTab.HISTORY -> HistoryScreen(
                modifier = Modifier.padding(innerPadding),
                uiState = historyUiState,
                transactions = transactions,
                onPresetSelected = viewModel::updateHistoryPreset,
                onCategorySelected = viewModel::updateHistoryCategory,
            )
        }
    }

    when (val sheet = activeSheet) {
        is ActiveSheet.Transaction -> AddTransactionSheet(
            categories = homeUiState.categories,
            quickSuggestions = homeUiState.quickSuggestions,
            initialCategoryId = sheet.preselectedCategoryId,
            onDismissRequest = { activeSheet = null },
            onCreateCategory = { activeSheet = ActiveSheet.CategoryEditor() },
            onSave = { amount, note, categoryId ->
                viewModel.addTransaction(amount, categoryId, note)
                activeSheet = null
            },
        )

        is ActiveSheet.CategoryEditor -> CategoryEditorSheet(
            category = sheet.category,
            onDismissRequest = { activeSheet = null },
            onDeleteRequest = { category ->
                activeSheet = null
                categoryToDelete = category
            },
            onSave = { categoryId, name, limit, iconKey, colorHex ->
                viewModel.saveCategory(categoryId, name, limit, iconKey, colorHex)
                activeSheet = null
            },
        )

        ActiveSheet.CurrencyPicker -> CurrencyPickerSheet(
            selectedCurrencyCode = homeUiState.selectedCurrencyCode,
            currencyOptions = homeUiState.currencyOptions,
            onDismissRequest = { activeSheet = null },
            onSelect = { currencyCode ->
                viewModel.updateCurrency(currencyCode)
                activeSheet = null
            },
        )

        null -> Unit
    }

    categoryToDelete?.let { category ->
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text("Delete ${category.name}?") },
            text = {
                Text("Deleting this category also removes its linked transactions and recurring entries.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCategory(category)
                        categoryToDelete = null
                    },
                ) {
                    Text("Delete", color = Danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun LockedScreen(
    snackbarHostState: SnackbarHostState,
    onUnlock: () -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = DeepBlack,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = BudgetGold,
                )
                Text(
                    text = "Smart Budgeter is locked",
                    style = MaterialTheme.typography.headlineMedium,
                )
                ExtendedFloatingActionButton(
                    onClick = onUnlock,
                    containerColor = BudgetGold,
                    contentColor = DeepBlack,
                    icon = { Icon(Icons.Default.Payments, contentDescription = null) },
                    text = { Text("Unlock with Biometrics") },
                )
            }
        }
    }
}

@Composable
private fun HomeScreen(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onAddTransaction: (Long?) -> Unit,
    onCreateCategory: () -> Unit,
    onChooseCurrency: () -> Unit,
    onEditCategory: (CategoryEntity) -> Unit,
    onExportCsv: () -> Unit,
    onExportPdf: () -> Unit,
) {
    val statusById = uiState.snapshot.categoryStatuses.associateBy { it.categoryId }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack),
        contentPadding = PaddingValues(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            HeroBudgetCard(
                monthLabel = uiState.monthLabel,
                totalBudget = uiState.snapshot.totalBudget,
                totalSpent = uiState.snapshot.totalSpent,
                remainingBudget = uiState.snapshot.remainingBudget,
                dailyAllowance = uiState.snapshot.dailyAllowance,
                budgetProgress = uiState.snapshot.budgetProgress,
                categoryCount = uiState.categories.size,
                selectedCurrencyCode = uiState.selectedCurrencyCode,
                onAddTransaction = { onAddTransaction(null) },
                onCreateCategory = onCreateCategory,
                onChooseCurrency = onChooseCurrency,
            )
        }

        item {
            if (uiState.categories.isEmpty()) {
                EmptyCategoryCard()
            } else {
                CategoryStudioSection(
                    categories = uiState.categories,
                    statusById = statusById,
                    onEditCategory = onEditCategory,
                )
            }
        }

        if (uiState.quickSuggestions.isNotEmpty()) {
            item {
                ModernCard(title = "Quick Add") {
                    Text(
                        text = "Suggested Categories.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        uiState.quickSuggestions.forEach { category ->
                            AssistChip(
                                onClick = { onAddTransaction(category.id) },
                                label = { Text(category.name) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = category.colorHex.toComposeColor().copy(alpha = 0.18f),
                                    labelColor = MaterialTheme.colorScheme.onSurface,
                                ),
                            )
                        }
                    }
                }
            }
        }

        item {
            AnimatedVisibility(visible = uiState.snapshot.burnRateTriggered) {
                ElevatedCard(
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = Danger.copy(alpha = 0.12f),
                    ),
                    shape = RoundedCornerShape(28.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Danger.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = Danger,
                            )
                        }
                        Text(
                            text = "You have burned through over half your plan before the 15th. Consider tightening the remaining categories.",
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }

        item {
            ModernCard(title = "Insights") {
                Text(
                    text = "Weekly Expense Chart",
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(modifier = Modifier.height(12.dp))
                SpendingTrendChart(points = uiState.snapshot.trend)
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Uncover Your Spending Habits",
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (uiState.snapshot.categoryBreakdown.isEmpty()) {
                    Text(
                        text = "Create categories and log transactions to unlock the category breakdown.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    val breakdownTotal = uiState.snapshot.categoryBreakdown
                        .sumOf { it.amount }
                        .takeIf { it > 0.0 }
                        ?: 1.0
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CategoryDonutChart(slices = uiState.snapshot.categoryBreakdown)
                        Spacer(modifier = Modifier.height(16.dp))
                        uiState.snapshot.categoryBreakdown.forEach { slice ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.White.copy(alpha = 0.04f))
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(slice.colorHex.toComposeColor()),
                                    )
                                    Column {
                                        Text(text = slice.label)
                                        Text(
                                            text = "${((slice.amount / breakdownTotal) * 100).asPercent()} of spending",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }
                                }
                                Text(
                                    text = slice.amount.asCurrency(),
                                    color = BudgetGold,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        if (uiState.recurringExpenses.isNotEmpty()) {
            item {
                ModernCard(title = "Recurring Expenses") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        val categoriesById = uiState.categories.associateBy { it.id }
                        uiState.recurringExpenses.forEach { expense ->
                            RecurringExpenseRow(
                                expense = expense,
                                categoryName = categoriesById[expense.categoryId]?.name ?: "Budget item",
                            )
                        }
                    }
                }
            }
        }

        if (uiState.vaultGoals.isNotEmpty()) {
            item {
                ModernCard(title = "Vault Goals") {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        uiState.vaultGoals.forEach { goal ->
                            VaultGoalRow(goal = goal)
                        }
                    }
                }
            }
        }

        item {
            ModernCard(title = "Exports") {
                Text(
                    text = "Share a monthly summary as CSV or PDF whenever you need a snapshot.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onExportCsv) {
                        Text("Share CSV")
                    }
                    OutlinedButton(onClick = onExportPdf) {
                        Text("Share PDF")
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryScreen(
    modifier: Modifier = Modifier,
    uiState: HistoryUiState,
    transactions: LazyPagingItems<TransactionRecord>,
    onPresetSelected: (DateFilterPreset) -> Unit,
    onCategorySelected: (Long?) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
            .padding(20.dp),
    ) {
        Text(
            text = "History",
            style = MaterialTheme.typography.displaySmall,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DateFilterPreset.entries.forEach { preset ->
                FilterChip(
                    selected = uiState.selectedPreset == preset,
                    onClick = { onPresetSelected(preset) },
                    label = { Text(preset.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BudgetGold.copy(alpha = 0.18f),
                        selectedLabelColor = MaterialTheme.colorScheme.onSurface,
                    ),
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = uiState.selectedCategoryId == null,
                onClick = { onCategorySelected(null) },
                label = { Text("All Categories") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = BudgetGold.copy(alpha = 0.18f),
                    selectedLabelColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
            uiState.categories.forEach { category ->
                FilterChip(
                    selected = uiState.selectedCategoryId == category.id,
                    onClick = { onCategorySelected(category.id) },
                    label = { Text(category.name) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = category.colorHex.toComposeColor().copy(alpha = 0.22f),
                        selectedLabelColor = MaterialTheme.colorScheme.onSurface,
                    ),
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        when (transactions.loadState.refresh) {
            is LoadState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Loading transactions...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (transactions.itemCount == 0) {
                        item {
                            ModernCard(title = "No transactions yet") {
                                Text(
                                    text = "Try a different filter or add a transaction from the dashboard.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    items(
                        count = transactions.itemCount,
                        key = transactions.itemKey { it.id },
                    ) { index ->
                        transactions[index]?.let { item ->
                            TransactionHistoryRow(item = item)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun AddTransactionSheet(
    categories: List<CategoryEntity>,
    quickSuggestions: List<CategoryEntity>,
    initialCategoryId: Long?,
    onDismissRequest: () -> Unit,
    onCreateCategory: () -> Unit,
    onSave: (String, String, Long?) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var amount by rememberSaveable(initialCategoryId) { mutableStateOf("") }
    var note by rememberSaveable(initialCategoryId) { mutableStateOf("") }
    var selectedCategoryId by rememberSaveable(initialCategoryId) { mutableStateOf<Long?>(initialCategoryId) }
    val lockedCategory = categories.firstOrNull { it.id == initialCategoryId }

    LaunchedEffect(initialCategoryId) {
        if (initialCategoryId != null) {
            selectedCategoryId = initialCategoryId
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = ElevatedBlack,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Log transaction",
                style = MaterialTheme.typography.headlineMedium,
            )

            if (categories.isEmpty()) {
                Text(
                    text = "Create a category first so every transaction has a budget bucket.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(onClick = onCreateCategory) {
                    Text("Create category")
                }
                Spacer(modifier = Modifier.height(12.dp))
                return@ModalBottomSheet
            }

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Note") },
                maxLines = 2,
            )

            if (lockedCategory != null) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleLarge,
                )
                ElevatedCard(
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = lockedCategory.colorHex.toComposeColor().copy(alpha = 0.14f),
                    ),
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CategoryIconBadge(
                            icon = categoryIcon(lockedCategory.icon),
                            tint = lockedCategory.colorHex.toComposeColor(),
                        )
                        Column {
                            Text(
                                text = lockedCategory.name,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = "Added from Quick Add",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            } else if (quickSuggestions.isNotEmpty()) {
                Text(
                    text = "Suggested right now",
                    style = MaterialTheme.typography.titleLarge,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    quickSuggestions.forEach { category ->
                        AssistChip(
                            onClick = { selectedCategoryId = category.id },
                            label = { Text(category.name) },
                            leadingIcon = {
                                Icon(
                                    imageVector = categoryIcon(category.icon),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (selectedCategoryId == category.id) {
                                    BudgetGold.copy(alpha = 0.18f)
                                } else {
                                    category.colorHex.toComposeColor().copy(alpha = 0.14f)
                                },
                            ),
                        )
                    }
                }
            }

            if (lockedCategory == null) {
                Text(
                    text = "Pick a category",
                    style = MaterialTheme.typography.titleLarge,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    categories.forEach { category ->
                        FilterChip(
                            selected = selectedCategoryId == category.id,
                            onClick = { selectedCategoryId = category.id },
                            label = { Text(category.name) },
                            leadingIcon = {
                                Icon(
                                    imageVector = categoryIcon(category.icon),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = category.colorHex.toComposeColor().copy(alpha = 0.18f),
                            ),
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(onClick = onDismissRequest) {
                    Text("Cancel")
                }
                Button(
                    onClick = { onSave(amount, note, selectedCategoryId) },
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log transaction")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ModernCard(
    title: String,
    action: (@Composable () -> Unit)? = null,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = ElevatedBlack),
        shape = RoundedCornerShape(30.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                )
                action?.invoke()
            }
            content()
        }
    }
}

@Composable
private fun HeroBudgetCard(
    monthLabel: String,
    totalBudget: Double,
    totalSpent: Double,
    remainingBudget: Double,
    dailyAllowance: Double,
    budgetProgress: Float,
    categoryCount: Int,
    selectedCurrencyCode: String,
    onAddTransaction: () -> Unit,
    onCreateCategory: () -> Unit,
    onChooseCurrency: () -> Unit,
) {
    ElevatedCard(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = ElevatedBlack),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            SoftGold.copy(alpha = 0.18f),
                            BudgetGold.copy(alpha = 0.08f),
                            ElevatedBlack,
                        ),
                    ),
                )
                .padding(22.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = monthLabel,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (totalBudget > 0.0) totalBudget.asCurrency() else "Set your limits",
                            style = MaterialTheme.typography.displaySmall,
                            color = BudgetGold,
                        )
                        Text(
                            text = if (totalBudget > 0.0) {
                                "Planned across $categoryCount categor${if (categoryCount == 1) "y" else "ies"}"
                            } else {
                                "Create custom categories and monthly limits"
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        AssistChip(
                            onClick = onChooseCurrency,
                            label = { Text("Currency Â· $selectedCurrencyCode") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = BudgetGold.copy(alpha = 0.14f),
                                labelColor = MaterialTheme.colorScheme.onSurface,
                            ),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(BudgetGold.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = BudgetGold,
                        )
                    }
                }

                BudgetProgressBar(progress = budgetProgress)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    MetricPill(
                        modifier = Modifier.weight(1f),
                        label = "Spent",
                        value = totalSpent.asCurrency(),
                    )
                    MetricPill(
                        modifier = Modifier.weight(1f),
                        label = "Remaining",
                        value = remainingBudget.asCurrency(),
                    )
                    MetricPill(
                        modifier = Modifier.weight(1f),
                        label = "Daily",
                        value = dailyAllowance.asCurrency(),
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onCreateCategory,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                    ) {
                        Text("Add category")
                    }
                    OutlinedButton(
                        onClick = onAddTransaction,
                        enabled = categoryCount > 0,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                    ) {
                        Text("Log transaction")
                    }
                    if (categoryCount == 0) {
                        Text(
                            text = "Create one category first to unlock transaction logging.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricPill(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
) {
    Column(
        modifier = modifier
            .heightIn(min = 96.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun EmptyCategoryCard(
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = ElevatedBlack),
        shape = RoundedCornerShape(30.dp),
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Build your own budget categories",
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = "The app no longer depends on preset labels like coffee or entertainment. Create exactly the categories you want and assign the monthly limits yourself.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CategoryStudioSection(
    categories: List<CategoryEntity>,
    statusById: Map<Long, com.deadwalkersf.smartbudgeter.domain.CategoryBudgetStatus>,
    onEditCategory: (CategoryEntity) -> Unit,
) {
    ModernCard(
        title = "Category Studio",
    ) {
        Text(
            text = "Edit names, icons, and monthly limits whenever your budget changes. Tap the pencil on any card to update it.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(14.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            items(categories, key = { it.id }) { category ->
                CategoryBudgetCard(
                    category = category,
                    status = statusById[category.id],
                    onEdit = { onEditCategory(category) },
                )
            }
        }
    }
}

@Composable
private fun CategoryBudgetCard(
    category: CategoryEntity,
    status: com.deadwalkersf.smartbudgeter.domain.CategoryBudgetStatus?,
    onEdit: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.width(240.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White.copy(alpha = 0.03f)),
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(18.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CategoryIconBadge(
                    icon = categoryIcon(category.icon),
                    tint = category.colorHex.toComposeColor(),
                )
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit ${category.name}",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = category.name, fontWeight = FontWeight.SemiBold)
                Text(
                    text = "Limit ${category.monthlyLimit.asCurrency()}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            BudgetProgressBar(progress = status?.progress?.coerceIn(0f, 1f) ?: 0f)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = "Spent",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(text = (status?.spent ?: 0.0).asCurrency())
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Left",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = (status?.remaining ?: category.monthlyLimit).asCurrency(),
                        color = if ((status?.remaining ?: category.monthlyLimit) < 0.0) Danger else BudgetGold,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyPickerSheet(
    selectedCurrencyCode: String,
    currencyOptions: List<CurrencyOption>,
    onDismissRequest: () -> Unit,
    onSelect: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = ElevatedBlack,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Choose currency",
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = "This changes how amounts are displayed across your dashboard, history, charts, and exports. It does not convert existing values.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            currencyOptions.forEach { option ->
                OutlinedButton(
                    onClick = { onSelect(option.code) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(
                        1.dp,
                        if (option.code == selectedCurrencyCode) {
                            BudgetGold.copy(alpha = 0.75f)
                        } else {
                            Color.White.copy(alpha = 0.08f)
                        },
                    ),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                text = "${option.symbol} ${option.code}",
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = option.name,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        if (option.code == selectedCurrencyCode) {
                            Text(
                                text = "Current",
                                color = BudgetGold,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                }
            }

            TextButton(onClick = onDismissRequest) {
                Text("Close")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun CategoryEditorSheet(
    category: CategoryEntity?,
    onDismissRequest: () -> Unit,
    onDeleteRequest: (CategoryEntity) -> Unit,
    onSave: (Long?, String, String, String, String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by rememberSaveable(category?.id) { mutableStateOf(category?.name.orEmpty()) }
    var limit by rememberSaveable(category?.id) {
        mutableStateOf(category?.monthlyLimit?.takeIf { it > 0.0 }?.toString().orEmpty())
    }
    var selectedIconKey by rememberSaveable(category?.id) {
        mutableStateOf(category?.icon ?: categoryIconOptions.first().key)
    }
    var selectedColorHex by rememberSaveable(category?.id) {
        mutableStateOf(category?.colorHex ?: categoryColorOptions.first())
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = ElevatedBlack,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = if (category == null) "Add category" else "Edit category",
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = "Choose your own label, icon, accent color, and monthly limit.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Category name") },
                singleLine = true,
            )
            OutlinedTextField(
                value = limit,
                onValueChange = { limit = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Monthly limit") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
            )
            if (category == null) {
                Button(
                    onClick = {
                        onSave(
                            null,
                            name,
                            limit,
                            selectedIconKey,
                            selectedColorHex,
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Text("Add category")
                }
                Text(
                    text = "You can add it now with the default icon and accent, or customize them below.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Text(
                text = "Icon",
                style = MaterialTheme.typography.titleLarge,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                categoryIconOptions.forEach { option ->
                    FilterChip(
                        selected = selectedIconKey == option.key,
                        onClick = { selectedIconKey = option.key },
                        label = { Text(option.label) },
                        leadingIcon = {
                            Icon(
                                imageVector = option.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BudgetGold.copy(alpha = 0.18f),
                        ),
                    )
                }
            }
            Text(
                text = "Accent",
                style = MaterialTheme.typography.titleLarge,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                categoryColorOptions.forEach { colorHex ->
                    ColorSwatch(
                        color = colorHex.toComposeColor(),
                        selected = selectedColorHex == colorHex,
                        onClick = { selectedColorHex = colorHex },
                    )
                }
            }
            if (category != null) {
                OutlinedButton(
                    onClick = { onDeleteRequest(category) },
                    border = BorderStroke(1.dp, Danger.copy(alpha = 0.6f)),
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        tint = Danger,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete category", color = Danger)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(onClick = onDismissRequest) {
                    Text("Cancel")
                }
                if (category != null) {
                    Button(
                        onClick = {
                            onSave(
                                category.id,
                                name,
                                limit,
                                selectedIconKey,
                                selectedColorHex,
                            )
                        },
                        shape = RoundedCornerShape(18.dp),
                    ) {
                        Text("Save changes")
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ColorSwatch(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier.size(34.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(DeepBlack),
                )
            }
        }
    }
}

@Composable
private fun CategoryIconBadge(
    icon: ImageVector,
    tint: Color,
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun BudgetProgressBar(
    progress: Float,
) {
    LinearProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(999.dp)),
        color = lerp(BudgetGold, Graphite, progress.coerceIn(0f, 1f)),
        trackColor = Graphite.copy(alpha = 0.24f),
    )
}

@Composable
private fun RecurringExpenseRow(
    expense: RecurringExpenseEntity,
    categoryName: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(text = expense.name, fontWeight = FontWeight.SemiBold)
            Text(
                text = "$categoryName on day ${expense.dayOfMonth}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(text = expense.amount.asCurrency(), color = BudgetGold)
    }
}

@Composable
private fun VaultGoalRow(
    goal: VaultGoalEntity,
) {
    val progress = if (goal.targetAmount > 0.0) {
        (goal.savedAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CategoryIconBadge(
                    icon = Icons.Default.Savings,
                    tint = goal.colorHex.toComposeColor(),
                )
                Column {
                    Text(text = goal.name, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "${goal.savedAmount.asCurrency()} of ${goal.targetAmount.asCurrency()}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(text = "${(progress * 100).toInt()}%", color = BudgetGold)
        }
        BudgetProgressBar(progress = progress)
    }
}

@Composable
private fun TransactionHistoryRow(
    item: TransactionRecord,
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = ElevatedBlack),
        shape = RoundedCornerShape(28.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(0.72f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CategoryIconBadge(
                    icon = categoryIcon(item.categoryIcon),
                    tint = item.categoryColor.toComposeColor(),
                )
                Column {
                    Text(text = item.categoryName, fontWeight = FontWeight.SemiBold)
                    if (item.note.isNotBlank()) {
                        Text(
                            text = item.note,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        text = item.timestamp.toDisplayDateTime(),
                        color = MutedText,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            Text(
                text = item.amount.asCurrency(),
                color = BudgetGold,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private fun categoryIcon(key: String): ImageVector {
    return categoryIconOptions.firstOrNull { it.key == key }?.icon ?: Icons.Default.AccountBalanceWallet
}
