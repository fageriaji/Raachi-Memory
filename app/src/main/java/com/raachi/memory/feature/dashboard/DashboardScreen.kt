package com.raachi.memory.feature.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raachi.memory.R
import com.raachi.memory.core.ui.AppSection
import com.raachi.memory.core.ui.LedgerMainColor
import com.raachi.memory.core.ui.LedgerOnMainColor
import com.raachi.memory.core.ui.RaachiBottomBar
import com.raachi.memory.core.ui.RaachiWordmark
import com.raachi.memory.core.designsystem.theme.RaachiBrandNavy
import com.raachi.memory.core.ui.ledgerKindColor
import com.raachi.memory.core.ui.ledgerKindSymbol
import com.raachi.memory.core.ui.raachiSuccessColor
import com.raachi.memory.core.ui.raachiSuccessContainerColor
import com.raachi.memory.core.ui.reminderCategoryAccent
import com.raachi.memory.core.ui.reminderCategoryEmoji
import com.raachi.memory.core.ui.ExpenseCreditColor
import com.raachi.memory.core.ui.ExpenseDebitColor
import com.raachi.memory.core.ui.ExpenseMainColor
import com.raachi.memory.domain.model.LedgerEntry
import com.raachi.memory.domain.model.LedgerKind
import com.raachi.memory.domain.model.Reminder
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onOpenProfile: () -> Unit,
    onOpenSection: (AppSection) -> Unit,
    onAddReminder: () -> Unit,
    onAddLedger: () -> Unit,
    onOpenReminder: (Long) -> Unit,
    onOpenLedger: (Long) -> Unit,
    onOpenExpenses: () -> Unit,
    onAddExpense: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showQuickAdd by remember { mutableStateOf(false) }

    if (showQuickAdd) {
        QuickAddSheet(
            onDismiss = { showQuickAdd = false },
            onAddReminder = {
                showQuickAdd = false
                onAddReminder()
            },
            onAddLedger = {
                showQuickAdd = false
                onAddLedger()
            },
            onAddExpense = {
                showQuickAdd = false
                onAddExpense()
            },
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    RaachiWordmark(
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    )
                },
                actions = {
                    IconButton(onClick = onOpenProfile) {
                        Icon(
                            imageVector = Icons.Outlined.AccountCircle,
                            contentDescription = stringResource(R.string.open_profile),
                            modifier = Modifier.size(32.dp),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    actionIconContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
        bottomBar = {
            RaachiBottomBar(
                selected = AppSection.HOME,
                onSelected = onOpenSection,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showQuickAdd = true },
                containerColor = RaachiBrandNavy,
                contentColor = Color.White,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.quick_add),
                )
            }
        },
    ) { innerPadding ->
        when (val state = uiState) {
            DashboardUiState.Loading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            DashboardUiState.MissingProfile -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.profile_not_found),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            is DashboardUiState.Ready -> DashboardContent(
                state = state,
                onAddReminder = onAddReminder,
                onAddLedger = onAddLedger,
                onViewReminders = { onOpenSection(AppSection.REMINDERS) },
                onViewLedger = { onOpenSection(AppSection.LEDGER) },
                onOpenReminder = onOpenReminder,
                onOpenLedger = onOpenLedger,
                onOpenExpenses = onOpenExpenses,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun DashboardContent(
    state: DashboardUiState.Ready,
    onAddReminder: () -> Unit,
    onAddLedger: () -> Unit,
    onViewReminders: () -> Unit,
    onViewLedger: () -> Unit,
    onOpenReminder: (Long) -> Unit,
    onOpenLedger: (Long) -> Unit,
    onOpenExpenses: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val ledgerContainer = if (isDark) LedgerMainContainerDark else LedgerMainContainer
    val ledgerContent = LedgerMainColor

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.dashboard_hello, state.name.firstName()),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(state.greeting.labelRes()),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = formatDashboardDate(LocalDate.now()),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SummaryTile(
                    label = stringResource(R.string.summary_reminders),
                    value = state.reminderCount,
                    icon = Icons.Outlined.CalendarMonth,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                SummaryTile(
                    label = stringResource(R.string.summary_ledger),
                    value = state.ledgerCount,
                    icon = Icons.Outlined.AccountBalanceWallet,
                    containerColor = ledgerContainer,
                    contentColor = ledgerContent,
                    modifier = Modifier.weight(1f),
                )
                SummaryTile(
                    label = stringResource(R.string.summary_done),
                    value = state.completedCount,
                    icon = Icons.Outlined.CheckCircle,
                    containerColor = raachiSuccessContainerColor(),
                    contentColor = raachiSuccessColor(),
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            ExpenseDashboardCard(
                balancePaise = state.expenseBalancePaise,
                todayExpensePaise = state.todayExpensePaise,
                accountCount = state.expenseAccountCount,
                onClick = onOpenExpenses,
            )
        }
        item {
            if (state.upcomingReminders.isEmpty()) {
                DashboardSection(
                    title = stringResource(R.string.up_next),
                    emptyTitle = stringResource(R.string.no_upcoming_reminders),
                    emptySupport = stringResource(R.string.no_upcoming_reminders_support),
                    actionLabel = stringResource(R.string.add_reminder),
                    icon = Icons.Outlined.Notifications,
                    accentColor = MaterialTheme.colorScheme.primary,
                    onAction = onAddReminder,
                )
            } else {
                UpcomingReminderSection(
                    reminders = state.upcomingReminders,
                    onViewAll = onViewReminders,
                    onOpenReminder = onOpenReminder,
                )
            }
        }
        item {
            if (state.pendingLedger.isEmpty()) {
                DashboardSection(
                    title = stringResource(R.string.pending_ledger),
                    emptyTitle = stringResource(R.string.no_pending_ledger),
                    emptySupport = stringResource(R.string.no_pending_ledger_support),
                    actionLabel = stringResource(R.string.add_ledger),
                    icon = Icons.Outlined.AccountBalanceWallet,
                    accentColor = ledgerContent,
                    onAction = onAddLedger,
                )
            } else {
                PendingLedgerSection(
                    entries = state.pendingLedger,
                    onViewAll = onViewLedger,
                    onOpenLedger = onOpenLedger,
                )
            }
        }
    }
}

@Composable
private fun PendingLedgerSection(
    entries: List<LedgerEntry>,
    onViewAll: () -> Unit,
    onOpenLedger: (Long) -> Unit,
) {
    val today = LocalDate.now()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DashboardSectionHeader(
            title = stringResource(R.string.pending_ledger),
            color = LedgerMainColor,
            onViewAll = onViewAll,
        )
        entries.forEach { entry ->
            val isOverdue = entry.isOverdue(today)
            val kindColor = ledgerKindColor(entry.kind)
            val accent = if (isOverdue) MaterialTheme.colorScheme.error else kindColor
            Surface(
                onClick = { onOpenLedger(entry.id) },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.large,
                tonalElevation = 1.dp,
                border = BorderStroke(1.dp, accent.copy(alpha = 0.18f)),
            ) {
                Row(
                    modifier = Modifier.height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .width(5.dp)
                            .fillMaxHeight()
                            .background(accent),
                    )
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            modifier = Modifier.size(50.dp),
                            color = kindColor.copy(alpha = 0.12f),
                            shape = MaterialTheme.shapes.medium,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = ledgerKindSymbol(entry.kind),
                                    color = if (entry.kind == LedgerKind.MONEY) kindColor else Color.Unspecified,
                                    fontSize = 25.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
                            Text(
                                text = entry.personName,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = when {
                                    isOverdue -> stringResource(R.string.overdue)
                                    entry.dueDate == today -> stringResource(R.string.due_today)
                                    entry.dueDate != null -> stringResource(
                                        R.string.due_on,
                                        entry.dueDate.format(DASHBOARD_DATE_FORMAT),
                                    )
                                    else -> stringResource(R.string.no_due_date)
                                },
                                color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        Text(
                            text = entry.amountPaise?.let(::formatDashboardCurrency) ?: entry.itemName.orEmpty(),
                            color = kindColor,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UpcomingReminderSection(
    reminders: List<Reminder>,
    onViewAll: () -> Unit,
    onOpenReminder: (Long) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DashboardSectionHeader(
            title = stringResource(R.string.up_next),
            onViewAll = onViewAll,
        )
        reminders.forEach { reminder ->
            val accent = reminderCategoryAccent(reminder.category)
            Surface(
                onClick = { onOpenReminder(reminder.id) },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.large,
                tonalElevation = 1.dp,
                border = BorderStroke(1.dp, accent.copy(alpha = 0.18f)),
            ) {
                Row(
                    modifier = Modifier.height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .width(5.dp)
                            .fillMaxHeight()
                            .background(accent),
                    )
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(accent.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = reminderCategoryEmoji(reminder.category),
                                color = Color.Unspecified,
                                fontSize = 26.sp,
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = reminder.title,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            reminder.description?.let { description ->
                                Text(
                                    text = description,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        Text(
                            text = reminder.nextTriggerAt
                                ?.atZone(ZoneId.systemDefault())
                                ?.format(DASHBOARD_TIME_FORMAT)
                                .orEmpty(),
                            color = accent,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryTile(
    label: String,
    value: Int,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(112.dp),
        color = containerColor,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label,
                color = contentColor,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = value.toString(),
                    color = contentColor,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }
    }
}

@Composable
private fun DashboardSectionHeader(
    title: String,
    color: Color = MaterialTheme.colorScheme.primary,
    onViewAll: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = color,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
        TextButton(
            onClick = onViewAll,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
        ) {
            Text(stringResource(R.string.view_all), color = color)
        }
    }
}

@Composable
private fun DashboardSection(
    title: String,
    emptyTitle: String,
    emptySupport: String,
    actionLabel: String,
    icon: ImageVector,
    accentColor: Color,
    onAction: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            color = accentColor,
            style = MaterialTheme.typography.labelLarge,
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.large,
            tonalElevation = 1.dp,
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = emptyTitle, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = emptySupport,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    TextButton(
                        onClick = onAction,
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp),
                    ) {
                        Text(actionLabel, color = accentColor)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickAddSheet(
    onDismiss: () -> Unit,
    onAddReminder: () -> Unit,
    onAddLedger: () -> Unit,
    onAddExpense: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.quick_add),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.close),
                    )
                }
            }
            ExtendedFloatingActionButton(
                onClick = onAddReminder,
                modifier = Modifier.fillMaxWidth(),
                icon = { Icon(Icons.Outlined.Notifications, contentDescription = null) },
                text = { Text(stringResource(R.string.add_reminder)) },
            )
            ExtendedFloatingActionButton(
                onClick = onAddLedger,
                modifier = Modifier.fillMaxWidth(),
                containerColor = LedgerMainColor,
                contentColor = LedgerOnMainColor,
                icon = { Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = null) },
                text = { Text(stringResource(R.string.add_ledger)) },
            )
            ExtendedFloatingActionButton(
                onClick = onAddExpense,
                modifier = Modifier.fillMaxWidth(),
                containerColor = ExpenseMainColor,
                contentColor = Color.Black,
                icon = { Icon(Icons.Outlined.Payments, contentDescription = null) },
                text = { Text("Add expense transaction") },
            )
        }
    }
}

@Composable
private fun ExpenseDashboardCard(
    balancePaise: Long,
    todayExpensePaise: Long,
    accountCount: Int,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = ExpenseMainColor.copy(alpha = 0.12f),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, ExpenseMainColor.copy(alpha = 0.30f)),
    ) {
        Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(52.dp),
                color = ExpenseMainColor,
                contentColor = Color.Black,
                shape = MaterialTheme.shapes.medium,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Payments, contentDescription = null)
                }
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 14.dp)) {
                Text("Daily Expenses", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    if (accountCount == 0) "Set up bank and cash balances" else "$accountCount accounts · ${formatDashboardCurrency(todayExpensePaise)} spent today",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Available", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                Text(
                    formatDashboardCurrency(balancePaise),
                    color = if (balancePaise < 0) ExpenseDebitColor else ExpenseCreditColor,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

private fun String.firstName(): String = trim().substringBefore(' ').ifBlank { trim() }

private fun GreetingPeriod.labelRes(): Int = when (this) {
    GreetingPeriod.MORNING -> R.string.good_morning
    GreetingPeriod.AFTERNOON -> R.string.good_afternoon
    GreetingPeriod.EVENING -> R.string.good_evening
    GreetingPeriod.NIGHT -> R.string.good_night
}

private val DASHBOARD_TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm a")
private val DASHBOARD_DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM")

internal fun formatDashboardDate(
    date: LocalDate,
    locale: Locale = Locale.getDefault(),
): String = date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", locale))

private val LedgerMainContainer = Color(0xFFF1E3F8)
private val LedgerMainContainerDark = Color(0xFF351047)

private fun formatDashboardCurrency(paise: Long): String =
    NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN")).apply {
        maximumFractionDigits = if (paise % 100 == 0L) 0 else 2
    }.format(BigDecimal(paise).movePointLeft(2))
