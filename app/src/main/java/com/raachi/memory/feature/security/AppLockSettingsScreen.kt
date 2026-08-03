package com.raachi.memory.feature.security

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raachi.memory.R
import com.raachi.memory.core.ui.raachiSuccessColor
import com.raachi.memory.core.ui.raachiSuccessContainerColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLockSettingsScreen(
    onBack: () -> Unit,
    biometricAvailable: Boolean,
    modifier: Modifier = Modifier,
    viewModel: AppLockSettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    state.message?.let { message ->
        val text = stringResource(message.messageRes())
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(text)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.app_lock), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        if (state.working) LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(innerPadding))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            if (state.settings.enabled) {
                EnabledAppLockContent(state, biometricAvailable, viewModel)
            } else {
                AppLockSetupContent(biometricAvailable, state.working, viewModel)
            }
        }
    }
}

@Composable
private fun AppLockSetupContent(
    biometricAvailable: Boolean,
    working: Boolean,
    viewModel: AppLockSettingsViewModel,
) {
    var passcode by rememberSaveable { mutableStateOf("") }
    var confirmation by rememberSaveable { mutableStateOf("") }
    var biometricEnabled by rememberSaveable { mutableStateOf(biometricAvailable) }
    var recoveryCode by rememberSaveable { mutableStateOf("") }

    SecurityHeader(Icons.Outlined.Lock, stringResource(R.string.set_up_app_lock), stringResource(R.string.set_up_app_lock_support))
    if (recoveryCode.isBlank()) {
        SettingsPasscodeField(passcode, { passcode = it.digitsOnly(6) }, stringResource(R.string.new_passcode))
        SettingsPasscodeField(confirmation, { confirmation = it.digitsOnly(6) }, stringResource(R.string.confirm_passcode))
        if (biometricAvailable) {
            Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Icon(Icons.Outlined.Fingerprint, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.enable_fingerprint), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.enable_fingerprint_support), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(checked = biometricEnabled, onCheckedChange = { biometricEnabled = it })
                }
            }
        }
        Button(
            onClick = {
                if (passcode.length == 6 && passcode == confirmation) recoveryCode = viewModel.generateRecoveryCode()
            },
            enabled = passcode.length == 6 && confirmation.length == 6 && !working,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) { Text(stringResource(R.string.continue_label)) }
        if (passcode.length == 6 && confirmation.length == 6 && passcode != confirmation) {
            Text(stringResource(R.string.passcodes_do_not_match), color = MaterialTheme.colorScheme.error)
        }
    } else {
        RecoveryCodePanel(recoveryCode)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = { recoveryCode = "" },
                modifier = Modifier.weight(1f).height(52.dp),
            ) { Text(stringResource(R.string.back)) }
            Button(
                onClick = { viewModel.enable(passcode, confirmation, recoveryCode, biometricEnabled) },
                enabled = !working,
                modifier = Modifier.weight(1f).height(52.dp),
            ) { Text(stringResource(R.string.enable_app_lock), maxLines = 1) }
        }
    }
}

@Composable
private fun EnabledAppLockContent(
    state: AppLockSettingsUiState,
    biometricAvailable: Boolean,
    viewModel: AppLockSettingsViewModel,
) {
    var action by rememberSaveable { mutableStateOf<AppLockAction?>(null) }
    var currentPasscode by rememberSaveable { mutableStateOf("") }
    var newPasscode by rememberSaveable { mutableStateOf("") }
    var confirmation by rememberSaveable { mutableStateOf("") }
    var recoveryCode by rememberSaveable { mutableStateOf("") }

    Surface(color = raachiSuccessContainerColor(), shape = MaterialTheme.shapes.medium) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(Icons.Outlined.Security, contentDescription = null, tint = raachiSuccessColor(), modifier = Modifier.size(32.dp))
            Column {
                Text(stringResource(R.string.app_lock_enabled), color = raachiSuccessColor(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.app_lock_enabled_support), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

    if (biometricAvailable) {
        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Icon(Icons.Outlined.Fingerprint, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.fingerprint_unlock), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.fingerprint_unlock_support), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = state.settings.biometricEnabled, onCheckedChange = viewModel::setBiometricEnabled)
            }
        }
    }

    OutlinedButton(onClick = { action = AppLockAction.CHANGE_PASSCODE }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
        Icon(Icons.Outlined.Key, contentDescription = null)
        Spacer(Modifier.size(8.dp))
        Text(stringResource(R.string.change_passcode))
    }
    OutlinedButton(onClick = { action = AppLockAction.REGENERATE_RECOVERY }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
        Icon(Icons.Outlined.Security, contentDescription = null)
        Spacer(Modifier.size(8.dp))
        Text(stringResource(R.string.replace_recovery_code))
    }
    OutlinedButton(onClick = { action = AppLockAction.DISABLE }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
        Icon(Icons.Outlined.LockOpen, contentDescription = null, tint = MaterialTheme.colorScheme.error)
        Spacer(Modifier.size(8.dp))
        Text(stringResource(R.string.disable_app_lock), color = MaterialTheme.colorScheme.error)
    }

    when (action) {
        AppLockAction.CHANGE_PASSCODE -> ActionPanel(title = stringResource(R.string.change_passcode)) {
            SettingsPasscodeField(currentPasscode, { currentPasscode = it.digitsOnly(6) }, stringResource(R.string.current_passcode))
            SettingsPasscodeField(newPasscode, { newPasscode = it.digitsOnly(6) }, stringResource(R.string.new_passcode))
            SettingsPasscodeField(confirmation, { confirmation = it.digitsOnly(6) }, stringResource(R.string.confirm_passcode))
            Button(
                onClick = { viewModel.changePasscode(currentPasscode, newPasscode, confirmation) },
                enabled = currentPasscode.length == 6 && newPasscode.length == 6 && confirmation.length == 6 && !state.working,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.save_changes)) }
        }
        AppLockAction.REGENERATE_RECOVERY -> ActionPanel(title = stringResource(R.string.replace_recovery_code)) {
            if (recoveryCode.isBlank()) {
                SettingsPasscodeField(currentPasscode, { currentPasscode = it.digitsOnly(6) }, stringResource(R.string.current_passcode))
                Button(
                    onClick = { viewModel.regenerateRecoveryCode(currentPasscode) { recoveryCode = it } },
                    enabled = currentPasscode.length == 6 && !state.working,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.generate_new_code)) }
            } else {
                RecoveryCodePanel(recoveryCode)
            }
        }
        AppLockAction.DISABLE -> ActionPanel(title = stringResource(R.string.disable_app_lock)) {
            Text(stringResource(R.string.disable_app_lock_warning), color = MaterialTheme.colorScheme.onSurfaceVariant)
            SettingsPasscodeField(currentPasscode, { currentPasscode = it.digitsOnly(6) }, stringResource(R.string.current_passcode))
            Button(
                onClick = { viewModel.disable(currentPasscode) },
                enabled = currentPasscode.length == 6 && !state.working,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.disable_app_lock)) }
        }
        null -> Unit
    }
}

@Composable
private fun ActionPanel(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun RecoveryCodePanel(code: String) {
    SecurityHeader(Icons.Outlined.Security, stringResource(R.string.save_recovery_code), stringResource(R.string.recovery_code_once_support))
    Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.medium) {
        Text(
            code,
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun SecurityHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, support: String) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(support, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@Composable
private fun SettingsPasscodeField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
    )
}

private enum class AppLockAction { CHANGE_PASSCODE, REGENERATE_RECOVERY, DISABLE }

private fun AppLockSettingsMessage.messageRes(): Int = when (this) {
    AppLockSettingsMessage.ENABLED -> R.string.app_lock_enabled
    AppLockSettingsMessage.PASSCODE_CHANGED -> R.string.passcode_changed
    AppLockSettingsMessage.INCORRECT_PASSCODE -> R.string.incorrect_passcode
    AppLockSettingsMessage.PASSCODES_DO_NOT_MATCH -> R.string.passcodes_do_not_match
    AppLockSettingsMessage.DISABLED -> R.string.app_lock_disabled
}

private fun String.digitsOnly(maxLength: Int): String = filter(Char::isDigit).take(maxLength)
