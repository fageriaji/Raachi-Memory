package com.raachi.memory.features.ledger

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raachi.memory.R
import com.raachi.memory.domain.model.LedgerEntry
import com.raachi.memory.domain.model.LedgerStatus
import com.raachi.memory.domain.util.DateTimeUtils
import com.raachi.memory.domain.util.isOverdue
import com.raachi.memory.ui.components.AppCard
import com.raachi.memory.ui.components.EmptyState
import com.raachi.memory.ui.components.RaachiSnackbarHost
import com.raachi.memory.ui.components.SectionHeader
import com.raachi.memory.ui.components.SnackbarType

@Composable
fun LedgerScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Int) -> Unit,
    viewModel: LedgerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var entryPendingDeletion by remember { mutableStateOf<LedgerEntry?>(null) }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { messageResId ->
            snackbarHostState.showSnackbar(context.getString(messageResId))
        }
    }

    Scaffold(
        snackbarHost = {
            RaachiSnackbarHost(
                hostState = snackbarHostState,
                type = SnackbarType.INFO
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.ledger_add_title)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader(title = stringResource(R.string.ledger))

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::updateSearchQuery,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = {
                    Text(stringResource(R.string.ledger_search_label))
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            LedgerFilterRow(
                selectedFilter = uiState.filter,
                onFilterSelected = viewModel::selectFilter
            )

            Spacer(modifier = Modifier.height(12.dp))

            LedgerSortSelector(
                selectedSort = uiState.sort,
                onSortSelected = viewModel::selectSort
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.entries.isEmpty()) {
                EmptyState(
                    title = stringResource(R.string.ledger_empty_title),
                    message = stringResource(R.string.ledger_empty_message)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.entries,
                        key = { entry -> entry.id }
                    ) { entry ->
                        LedgerEntryCard(
                            entry = entry,
                            currentTimeMillis = uiState.currentTimeMillis,
                            onEdit = { onNavigateToEdit(entry.id) },
                            onMarkReturned = {
                                viewModel.markAsReturned(entry)
                            },
                            onShare = {
                                viewModel.shareEntry(entry)
                            },
                            onDelete = {
                                entryPendingDeletion = entry
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(88.dp))
                    }
                }
            }
        }
    }

    entryPendingDeletion?.let { entry ->
        AlertDialog(
            onDismissRequest = {
                entryPendingDeletion = null
            },
            title = {
                Text(stringResource(R.string.ledger_delete_title))
            },
            text = {
                Text(
                    stringResource(
                        R.string.ledger_delete_message,
                        entry.personName
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteEntry(entry)
                        entryPendingDeletion = null
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        entryPendingDeletion = null
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LedgerFilterRow(
    selectedFilter: LedgerFilter,
    onFilterSelected: (LedgerFilter) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LedgerFilter.entries.forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = {
                    onFilterSelected(filter)
                },
                label = {
                    Text(stringResource(filter.labelResId()))
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LedgerSortSelector(
    selectedSort: LedgerSort,
    onSortSelected: (LedgerSort) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        }
    ) {
        OutlinedTextField(
            value = stringResource(selectedSort.labelResId()),
            onValueChange = {},
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            readOnly = true,
            label = {
                Text(stringResource(R.string.ledger_sort_label))
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            LedgerSort.entries.forEach { sort ->
                DropdownMenuItem(
                    text = {
                        Text(stringResource(sort.labelResId()))
                    },
                    onClick = {
                        onSortSelected(sort)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun LedgerEntryCard(
    entry: LedgerEntry,
    currentTimeMillis: Long,
    onEdit: () -> Unit,
    onMarkReturned: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val statusResId = entry.statusResId(currentTimeMillis)
    val itemName = entry.itemName
        ?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.not_set)

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit)
    ) {
        Text(
            text = entry.personName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = stringResource(
                R.string.ledger_item_format,
                stringResource(entry.itemType.labelResId()),
                itemName
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        entry.amount?.let { amount ->
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(
                    R.string.ledger_amount_format,
                    amount
                ),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = stringResource(
                R.string.ledger_borrowed_datetime_format,
                DateTimeUtils.formatDate(entry.borrowDateTime),
                DateTimeUtils.formatTime(entry.borrowDateTime)
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = entry.dueDateTime?.let { dueDateTime ->
                stringResource(
                    R.string.ledger_due_datetime_format,
                    DateTimeUtils.formatDate(dueDateTime),
                    DateTimeUtils.formatTime(dueDateTime)
                )
            } ?: stringResource(R.string.ledger_due_not_set),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        AssistChip(
            onClick = {},
            label = {
                Text(stringResource(statusResId))
            },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = statusContainerColor(
                    entry = entry,
                    currentTimeMillis = currentTimeMillis
                ),
                labelColor = statusContentColor(
                    entry = entry,
                    currentTimeMillis = currentTimeMillis
                )
            ),
            border = AssistChipDefaults.assistChipBorder(
                enabled = true,
                borderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onEdit) {
                Text(stringResource(R.string.ledger_edit))
            }

            if (entry.status == LedgerStatus.PENDING) {
                TextButton(onClick = onMarkReturned) {
                    Text(stringResource(R.string.ledger_mark_returned))
                }

                TextButton(onClick = onShare) {
                    Text(stringResource(R.string.ledger_notification_share))
                }
            }

            TextButton(onClick = onDelete) {
                Text(stringResource(R.string.delete))
            }

            TextButton(onClick = onDelete) {
                Text(stringResource(R.string.delete))
            }
        }
    }
}

@Composable
private fun statusContainerColor(
    entry: LedgerEntry,
    currentTimeMillis: Long
) = when {
    entry.isOverdue(currentTimeMillis) -> MaterialTheme.colorScheme.errorContainer
    entry.status == LedgerStatus.RETURNED -> MaterialTheme.colorScheme.secondaryContainer
    else -> MaterialTheme.colorScheme.primaryContainer
}

@Composable
private fun statusContentColor(
    entry: LedgerEntry,
    currentTimeMillis: Long
) = when {
    entry.isOverdue(currentTimeMillis) -> MaterialTheme.colorScheme.onErrorContainer
    entry.status == LedgerStatus.RETURNED -> MaterialTheme.colorScheme.onSecondaryContainer
    else -> MaterialTheme.colorScheme.onPrimaryContainer
}

private fun LedgerEntry.statusResId(
    currentTimeMillis: Long
): Int {
    return when {
        isOverdue(currentTimeMillis) -> R.string.ledger_filter_overdue
        status == LedgerStatus.RETURNED -> R.string.ledger_filter_returned
        else -> R.string.ledger_filter_pending
    }
}
