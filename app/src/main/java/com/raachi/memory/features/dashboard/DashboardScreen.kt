package com.raachi.memory.features.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raachi.memory.R
import com.raachi.memory.ui.components.EmptyState
import com.raachi.memory.ui.components.ExpandableFab
import com.raachi.memory.ui.components.SectionHeader

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToReminder: () -> Unit,
    onNavigateToLedger: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            ExpandableFab(
                onAddReminder = onNavigateToReminder,
                onAddLedger = onNavigateToLedger
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            val greetingRes = when (state.timeOfDay) {
                TimeOfDay.MORNING -> R.string.good_morning
                TimeOfDay.AFTERNOON -> R.string.good_afternoon
                TimeOfDay.EVENING -> R.string.good_evening
            }

            Text(
                text = stringResource(id = greetingRes),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${state.userName} \uD83D\uDC4B",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = state.currentDate,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OverviewMetric(
                        label = stringResource(R.string.reminders),
                        value = state.activeRemindersCount.toString()
                    )
                    OverviewMetric(
                        label = stringResource(R.string.ledger),
                        value = state.pendingLedgerCount.toString()
                    )
                    OverviewMetric(
                        label = stringResource(R.string.done),
                        value = state.completedTodayCount.toString()
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            SectionHeader(title = stringResource(R.string.up_next))
            Spacer(modifier = Modifier.height(16.dp))

            state.nextReminder?.let { reminder ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = reminder.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = stringResource(R.string.category_format, reminder.category.name),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } ?: run {
                EmptyState(
                    title = stringResource(R.string.all_caught_up),
                    message = stringResource(R.string.no_reminders_scheduled)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            SectionHeader(title = stringResource(R.string.pending_ledger))
            Spacer(modifier = Modifier.height(16.dp))

            if (state.pendingLedgerCount > 0) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.items_pending_format, state.pendingLedgerCount),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(R.string.total_amount_format, state.pendingLedgerAmount),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            } else {
                EmptyState(
                    title = stringResource(R.string.all_settled),
                    message = stringResource(R.string.nothing_pending)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            SectionHeader(title = stringResource(R.string.quick_actions))
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                QuickActionCard(
                    title = stringResource(R.string.reminder),
                    icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToReminder
                )
                QuickActionCard(
                    title = stringResource(R.string.ledger),
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToLedger
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun OverviewMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(modifier = modifier, onClick = onClick) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            icon()
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, style = MaterialTheme.typography.labelLarge)
        }
    }
}