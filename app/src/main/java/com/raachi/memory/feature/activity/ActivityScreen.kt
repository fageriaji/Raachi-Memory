package com.raachi.memory.feature.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Snooze
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raachi.memory.R
import com.raachi.memory.core.ui.LedgerMainColor
import com.raachi.memory.core.ui.RaachiSectionTopBar
import com.raachi.memory.domain.model.ActivityEventType
import com.raachi.memory.domain.model.ActivityLog
import com.raachi.memory.domain.model.ActivitySource
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ActivityViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var searching by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            RaachiSectionTopBar(
                title = stringResource(R.string.nav_activity),
                onOpenDrawer = onOpenDrawer,
                actions = {
                    IconButton(onClick = { searching = !searching }) {
                        Icon(Icons.Outlined.Search, contentDescription = stringResource(R.string.search_activity))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (searching) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = viewModel::updateSearch,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                    placeholder = { Text(stringResource(R.string.search_activity)) },
                    singleLine = true,
                )
            }
            ActivityFilters(state.selectedFilter, viewModel::selectFilter)
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.activities.isEmpty() -> ActivityEmptyState(hasFilter = state.searchQuery.isNotBlank() || state.selectedFilter != ActivityFilter.ALL)
                else -> ActivityTimeline(state.activities)
            }
        }
    }
}

@Composable
private fun ActivityFilters(selected: ActivityFilter, onSelected: (ActivityFilter) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ActivityFilter.entries.forEach { filter ->
            FilterChip(
                selected = selected == filter,
                onClick = { onSelected(filter) },
                label = { Text(stringResource(filter.labelRes())) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ActivityTimeline(activities: List<ActivityLog>) {
    val zone = ZoneId.systemDefault()
    val grouped = activities.groupBy { it.eventTime.atZone(zone).toLocalDate() }
    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 28.dp),
    ) {
        grouped.forEach { (date, logs) ->
            item(key = "header-$date") { ActivityDateHeader(date) }
            items(logs, key = ActivityLog::id) { activity ->
                ActivityTimelineRow(activity, zone, isLast = activity == logs.last())
            }
        }
    }
}

@Composable
private fun ActivityDateHeader(date: LocalDate) {
    val today = LocalDate.now()
    val label = when (date) {
        today -> stringResource(R.string.today)
        today.minusDays(1) -> stringResource(R.string.yesterday)
        else -> date.format(DATE_FORMAT)
    }
    Text(
        text = label.uppercase(),
        modifier = Modifier.padding(top = 18.dp, bottom = 10.dp),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun ActivityTimelineRow(activity: ActivityLog, zone: ZoneId, isLast: Boolean) {
    val tint = activity.tint()
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                color = tint.copy(alpha = 0.12f),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(activity.icon(), contentDescription = null, tint = tint, modifier = Modifier.size(23.dp))
                }
            }
            if (!isLast) {
                Spacer(
                    Modifier.width(2.dp).height(50.dp).background(MaterialTheme.colorScheme.outlineVariant),
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(
            modifier = Modifier.weight(1f).padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = activity.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = activity.eventTime.atZone(zone).format(TIME_FORMAT),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            activity.description?.let { description ->
                Text(
                    text = description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = stringResource(if (activity.source == ActivitySource.REMINDER) R.string.nav_reminders else R.string.nav_ledger),
                color = tint,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun ActivityEmptyState(hasFilter: Boolean) {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Outlined.History, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
            Text(
                text = stringResource(if (hasFilter) R.string.no_matching_activity else R.string.no_activity_yet),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun ActivityFilter.labelRes(): Int = when (this) {
    ActivityFilter.ALL -> R.string.all
    ActivityFilter.REMINDERS -> R.string.nav_reminders
    ActivityFilter.LEDGER -> R.string.nav_ledger
}

private fun ActivityLog.icon(): ImageVector = when (eventType) {
    ActivityEventType.REMINDER_CREATED, ActivityEventType.LEDGER_CREATED -> Icons.Outlined.Notifications
    ActivityEventType.REMINDER_UPDATED, ActivityEventType.LEDGER_UPDATED -> Icons.Outlined.Edit
    ActivityEventType.REMINDER_ALERT_SENT, ActivityEventType.LEDGER_ALERT_SENT -> Icons.Outlined.Alarm
    ActivityEventType.REMINDER_SNOOZED -> Icons.Outlined.Snooze
    ActivityEventType.REMINDER_COMPLETED, ActivityEventType.LEDGER_RETURNED -> Icons.Outlined.CheckCircle
    ActivityEventType.REMINDER_SKIPPED -> Icons.Outlined.History
    ActivityEventType.REMINDER_DELETED, ActivityEventType.LEDGER_DELETED -> Icons.Outlined.Delete
}

@Composable
private fun ActivityLog.tint(): Color = when {
    eventType in setOf(ActivityEventType.REMINDER_DELETED, ActivityEventType.LEDGER_DELETED) -> MaterialTheme.colorScheme.error
    source == ActivitySource.LEDGER -> LedgerMainColor
    else -> MaterialTheme.colorScheme.primary
}

private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM uuuu")
private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
