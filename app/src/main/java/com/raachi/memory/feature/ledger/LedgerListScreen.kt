package com.raachi.memory.feature.ledger

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.net.toUri
import com.raachi.memory.R
import com.raachi.memory.core.ui.AppSection
import com.raachi.memory.core.ui.LedgerItemColor
import com.raachi.memory.core.ui.LedgerMainColor
import com.raachi.memory.core.ui.LedgerMoneyColor
import com.raachi.memory.core.ui.RaachiBottomBar
import com.raachi.memory.core.ui.RaachiSectionTopBar
import com.raachi.memory.core.ui.NotificationPermissionControls
import com.raachi.memory.core.ui.ledgerKindColor
import com.raachi.memory.core.ui.raachiSuccessColor
import com.raachi.memory.domain.model.LedgerDirection
import com.raachi.memory.domain.model.LedgerEntry
import com.raachi.memory.domain.model.LedgerKind
import com.raachi.memory.domain.usecase.ledgerShareMessage
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerListScreen(
    onBack: () -> Unit,
    onOpenSection: (AppSection) -> Unit,
    onAddEntry: () -> Unit,
    onEditEntry: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LedgerListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var searching by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<LedgerEntry?>(null) }
    pendingDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.delete_ledger_entry)) },
            text = { Text(stringResource(R.string.delete_ledger_confirmation)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(entry.id)
                    pendingDelete = null
                }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text(stringResource(R.string.cancel)) } },
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            RaachiSectionTopBar(
                title = stringResource(R.string.nav_ledger),
                onBack = onBack,
                accentColor = LedgerMainColor,
                actions = {
                    IconButton(onClick = { searching = !searching }) {
                        Icon(Icons.Outlined.Search, contentDescription = stringResource(R.string.search_ledger))
                    }
                },
            )
        },
        bottomBar = { RaachiBottomBar(AppSection.LEDGER, onOpenSection) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddEntry,
                containerColor = LedgerMainColor,
                contentColor = Color.White,
            ) {
                Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.add_ledger))
            }
        },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (searching) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = viewModel::updateSearch,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                    placeholder = { Text(stringResource(R.string.search_ledger)) },
                    singleLine = true,
                )
            }
            LedgerSummaryRow(state.summary)
            NotificationPermissionControls(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            )
            LedgerTabs(state.selectedTab, viewModel::selectTab)
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                state.entries.isEmpty() -> LedgerEmptyState()
                else -> LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 104.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(state.entries, key = LedgerEntry::id) { entry ->
                        LedgerCard(
                            entry = entry,
                            onEdit = { onEditEntry(entry.id) },
                            onDelete = { pendingDelete = entry },
                            onShare = { shareLedger(context, entry) },
                            onReturned = { viewModel.markReturned(entry.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LedgerSummaryRow(summary: LedgerSummary) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LedgerSummaryCard(
            title = stringResource(R.string.total_lent),
            amount = summary.lentPaise,
            items = summary.lentItems,
            icon = { Icon(Icons.AutoMirrored.Outlined.TrendingUp, contentDescription = null) },
            modifier = Modifier.weight(1f),
        )
        LedgerSummaryCard(
            title = stringResource(R.string.total_borrowed),
            amount = summary.borrowedPaise,
            items = summary.borrowedItems,
            icon = { Icon(Icons.AutoMirrored.Outlined.TrendingDown, contentDescription = null) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun LedgerSummaryCard(
    title: String,
    amount: Long,
    items: Int,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier, color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.large, tonalElevation = 1.dp) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(
                color = LedgerMainColor.copy(alpha = 0.10f),
                contentColor = LedgerMainColor,
                shape = MaterialTheme.shapes.medium,
            ) {
                Box(modifier = Modifier.padding(10.dp), contentAlignment = Alignment.Center) { icon() }
            }
            Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            Text(formatCurrency(amount), color = LedgerMoneyColor, style = MaterialTheme.typography.titleLarge, maxLines = 1)
            Text(
                pluralStringResource(R.plurals.ledger_item_count, items, items),
                color = LedgerItemColor,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun LedgerTabs(selected: LedgerTab, onSelected: (LedgerTab) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        LedgerTab.entries.forEach { tab ->
            FilterChip(
                selected = tab == selected,
                onClick = { onSelected(tab) },
                label = { Text(stringResource(tab.labelRes())) },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = LedgerMainColor,
                    selectedLabelColor = Color.White,
                ),
            )
        }
    }
}

@Composable
private fun LedgerEmptyState() {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(44.dp), tint = LedgerMainColor)
            Text(stringResource(R.string.no_ledger_entries), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LedgerCard(
    entry: LedgerEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onReturned: () -> Unit,
) {
    val today = LocalDate.now()
    val kindColor = ledgerKindColor(entry.kind)
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onEdit),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Surface(color = kindColor.copy(alpha = 0.12f), shape = MaterialTheme.shapes.medium) {
                    Text(
                        text = entry.personName.initials(),
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 11.dp),
                        color = kindColor,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
                    Text(entry.personName, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        text = stringResource(
                            if (entry.direction == LedgerDirection.LENT) R.string.lent_on_date else R.string.borrowed_on_date,
                            entry.transactionDate.format(DATE_FORMAT),
                        ),
                        color = LedgerMainColor,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = entry.dueLabel(today),
                        color = when {
                            entry.isReturned -> raachiSuccessColor()
                            entry.isOverdue(today) -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.delete_ledger_entry), tint = MaterialTheme.colorScheme.error)
                    }
                    Text(
                        text = entry.amountPaise?.let(::formatCurrency) ?: entry.itemName.orEmpty(),
                        color = kindColor,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(stringResource(if (entry.direction == LedgerDirection.LENT) R.string.lent else R.string.borrowed), style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (!entry.isReturned) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TextButton(onClick = onShare, modifier = Modifier.weight(1f)) {
                        Icon(
                            painter = painterResource(R.drawable.ic_whatsapp),
                            contentDescription = stringResource(R.string.open_whatsapp),
                            tint = Color(0xFF128C7E),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.whatsapp))
                    }
                    TextButton(onClick = onReturned, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.mark_returned))
                    }
                }
            }
        }
    }
}

private fun shareLedger(context: Context, entry: LedgerEntry) {
    val message = ledgerShareMessage(entry)
    entry.mobileNumber?.let { mobile ->
        val directChat = "https://wa.me/91$mobile?text=${Uri.encode(message)}".toUri()
        for (packageName in WHATSAPP_PACKAGES) {
            try {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, directChat).apply { setPackage(packageName) },
                )
                return
            } catch (_: ActivityNotFoundException) {
                // Try WhatsApp Business, then the system share sheet.
            }
        }
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
    }
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_ledger_reminder)))
}

@Composable
private fun LedgerEntry.dueLabel(today: LocalDate): String = when {
    isReturned -> stringResource(R.string.returned)
    dueDate == null -> stringResource(R.string.no_due_date)
    isOverdue(today) -> pluralStringResource(R.plurals.overdue_days, (today.toEpochDay() - dueDate.toEpochDay()).toInt(), (today.toEpochDay() - dueDate.toEpochDay()).toInt())
    dueDate == today -> stringResource(R.string.due_today)
    else -> stringResource(R.string.due_on, dueDate.format(DATE_FORMAT))
}

private fun formatCurrency(paise: Long): String = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN")).apply {
    maximumFractionDigits = if (paise % 100 == 0L) 0 else 2
}.format(BigDecimal(paise).movePointLeft(2))

private fun String.initials(): String = trim().split(Regex("\\s+")).filter(String::isNotBlank).take(2).joinToString("") { it.take(1).uppercase() }

private fun LedgerTab.labelRes(): Int = when (this) {
    LedgerTab.ALL -> R.string.all
    LedgerTab.LENT -> R.string.lent
    LedgerTab.BORROWED -> R.string.borrowed
}

private val DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM uuuu")
private val WHATSAPP_PACKAGES = listOf("com.whatsapp", "com.whatsapp.w4b")
