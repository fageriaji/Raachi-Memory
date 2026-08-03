package com.raachi.memory.feature.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Snooze
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raachi.memory.R
import com.raachi.memory.core.ui.RaachiSectionTopBar
import com.raachi.memory.domain.model.ThemeMode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onOpenDrawer: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenAppLock: () -> Unit,
    modifier: Modifier = Modifier,
    backupOnly: Boolean = false,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingImport by remember { mutableStateOf<Uri?>(null) }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let(viewModel::export)
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        pendingImport = uri
    }

    state.message?.let { message ->
        val text = stringResource(message.stringRes())
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(text)
            viewModel.consumeMessage()
        }
    }
    pendingImport?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingImport = null },
            title = { Text(stringResource(R.string.restore_backup)) },
            text = { Text(stringResource(R.string.restore_backup_warning)) },
            confirmButton = {
                TextButton(onClick = {
                    pendingImport = null
                    viewModel.import(uri)
                }) { Text(stringResource(R.string.restore)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingImport = null }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            RaachiSectionTopBar(
                title = stringResource(if (backupOnly) R.string.nav_backup_restore else R.string.nav_settings),
                onOpenDrawer = onOpenDrawer,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        ) {
            if (state.isWorking) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                if (!backupOnly) {
                    SettingsSectionTitle(stringResource(R.string.appearance))
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.large) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            PreferenceHeading(Icons.Outlined.DarkMode, stringResource(R.string.theme), stringResource(R.string.theme_support))
                            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                ThemeMode.entries.forEachIndexed { index, mode ->
                                    SegmentedButton(
                                        selected = state.preferences.themeMode == mode,
                                        onClick = { viewModel.setThemeMode(mode) },
                                        shape = SegmentedButtonDefaults.itemShape(index, ThemeMode.entries.size),
                                    ) { Text(stringResource(mode.labelRes())) }
                                }
                            }
                        }
                    }

                    SettingsSectionTitle(stringResource(R.string.reminder_preferences))
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.large) {
                        Column {
                            PreferenceRow(
                                icon = Icons.Outlined.Notifications,
                                title = stringResource(R.string.reminder_sound_setting),
                                support = stringResource(R.string.reminder_sound_setting_support),
                                trailing = {
                                    Switch(
                                        checked = state.preferences.reminderSoundEnabled,
                                        onCheckedChange = viewModel::setReminderSound,
                                    )
                                },
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))
                            SnoozePreferenceRow(state.preferences.defaultSnoozeMinutes, viewModel::setDefaultSnooze)
                        }
                    }
                }

                SettingsSectionTitle(stringResource(R.string.data_and_privacy))
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.large) {
                    Column {
                        if (!backupOnly) {
                            PreferenceRow(
                                icon = Icons.Outlined.Lock,
                                title = stringResource(R.string.app_lock),
                                support = stringResource(R.string.app_lock_support),
                                onClick = onOpenAppLock,
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))
                        }
                        PreferenceRow(
                            icon = Icons.Outlined.Backup,
                            title = stringResource(R.string.export_backup),
                            support = stringResource(R.string.export_backup_support),
                            enabled = !state.isWorking,
                            onClick = { exportLauncher.launch(backupFileName()) },
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))
                        PreferenceRow(
                            icon = Icons.Outlined.Restore,
                            title = stringResource(R.string.import_backup),
                            support = stringResource(R.string.import_backup_support),
                            enabled = !state.isWorking,
                            onClick = { importLauncher.launch(arrayOf("application/json", "text/plain")) },
                        )
                    }
                }

                if (!backupOnly) {
                    SettingsSectionTitle(stringResource(R.string.support_and_about))
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.large) {
                        PreferenceRow(
                            icon = Icons.Outlined.Info,
                            title = stringResource(R.string.about_raachi_memory),
                            support = stringResource(R.string.about_support),
                            onClick = onOpenAbout,
                        )
                    }
                }
                Spacer(Modifier.size(8.dp))
            }
        }
    }
}

@Composable
private fun SnoozePreferenceRow(minutes: Int, onSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    PreferenceRow(
        icon = Icons.Outlined.Snooze,
        title = stringResource(R.string.default_snooze),
        support = stringResource(R.string.default_snooze_support),
        trailing = {
            Box {
                TextButton(onClick = { expanded = true }) {
                    Text(stringResource(R.string.minutes_short, minutes))
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    SNOOZE_OPTIONS.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.minutes_long, option)) },
                            onClick = {
                                expanded = false
                                onSelected(option)
                            },
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun PreferenceHeading(icon: ImageVector, title: String, support: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        SettingsIcon(icon)
        Spacer(Modifier.width(14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(support, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun PreferenceRow(
    icon: ImageVector,
    title: String,
    support: String,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val clickableModifier = if (onClick == null) Modifier else Modifier.clickable(enabled = enabled, onClick = onClick)
    Row(
        modifier = Modifier.fillMaxWidth().then(clickableModifier).padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsIcon(icon)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(support, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        }
        when {
            trailing != null -> trailing()
            onClick != null -> Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null)
        }
    }
}

@Composable
private fun SettingsIcon(icon: ImageVector) {
    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.medium) {
        Box(modifier = Modifier.size(46.dp), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(title, modifier = Modifier.padding(horizontal = 4.dp), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
}

private fun ThemeMode.labelRes(): Int = when (this) {
    ThemeMode.SYSTEM -> R.string.system_theme
    ThemeMode.LIGHT -> R.string.light_theme
    ThemeMode.DARK -> R.string.dark_theme
}

private fun SettingsMessage.stringRes(): Int = when (this) {
    SettingsMessage.EXPORT_COMPLETE -> R.string.export_complete
    SettingsMessage.IMPORT_COMPLETE -> R.string.import_complete
    SettingsMessage.OPERATION_FAILED -> R.string.backup_operation_failed
}

private fun backupFileName(): String = "Raachi_Memory_Backup_${LocalDateTime.now().format(BACKUP_FILE_TIME)}.json"

private val BACKUP_FILE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
private val SNOOZE_OPTIONS = listOf(5, 10, 15, 30, 60)
