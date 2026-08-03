package com.raachi.memory.feature.security

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raachi.memory.R
import com.raachi.memory.core.ui.RaachiMark
import com.raachi.memory.core.ui.RaachiWordmark

private enum class LockScreenMode { UNLOCK, RECOVERY_OPTIONS, RECOVERY_CODE, NEW_PASSCODE, NEW_RECOVERY_CODE }

@Composable
fun AppLockGate(
    viewModel: AppLockViewModel,
    biometricAvailable: Boolean,
    deviceCredentialAvailable: Boolean,
    authenticateBiometric: ((Boolean) -> Unit) -> Unit,
    authenticateDeviceCredential: ((Boolean) -> Unit) -> Unit,
    content: @Composable () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    when {
        state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        !state.settings.enabled || state.unlocked -> content()
        else -> AppLockScreen(
            state = state,
            biometricAvailable = biometricAvailable,
            deviceCredentialAvailable = deviceCredentialAvailable,
            onUnlock = viewModel::unlockWithPasscode,
            onBiometric = {
                authenticateBiometric { authenticated ->
                    if (authenticated) viewModel.onBiometricAuthenticated()
                }
            },
            onDeviceCredential = { onAuthorized ->
                authenticateDeviceCredential { authenticated ->
                    if (authenticated) {
                        viewModel.authorizeWithDeviceCredential { onAuthorized(true) }
                    } else {
                        onAuthorized(false)
                    }
                }
            },
            onVerifyRecovery = viewModel::authorizeRecoveryCode,
            onResetPasscode = viewModel::resetPasscode,
            onResetComplete = viewModel::unlockAfterReset,
            onClearError = viewModel::clearError,
        )
    }
}

@Composable
private fun AppLockScreen(
    state: AppLockGateUiState,
    biometricAvailable: Boolean,
    deviceCredentialAvailable: Boolean,
    onUnlock: (String) -> Unit,
    onBiometric: () -> Unit,
    onDeviceCredential: ((Boolean) -> Unit) -> Unit,
    onVerifyRecovery: (String, () -> Unit) -> Unit,
    onResetPasscode: (String, String, (String) -> Unit) -> Unit,
    onResetComplete: () -> Unit,
    onClearError: () -> Unit,
) {
    var mode by rememberSaveable { mutableStateOf(LockScreenMode.UNLOCK) }
    var passcode by rememberSaveable { mutableStateOf("") }
    var confirmation by rememberSaveable { mutableStateOf("") }
    var recoveryInput by rememberSaveable { mutableStateOf("") }
    var newRecoveryCode by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(mode) {
        passcode = ""
        confirmation = ""
        recoveryInput = ""
        onClearError()
    }

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        ) {
            RaachiMark(modifier = Modifier.size(88.dp))
            Spacer(Modifier.height(10.dp))
            RaachiWordmark(
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            )
            Spacer(Modifier.height(28.dp))

            when (mode) {
                LockScreenMode.UNLOCK -> UnlockContent(
                    passcode = passcode,
                    onPasscodeChange = { passcode = it.digitsOnly(6) },
                    state = state,
                    biometricEnabled = state.settings.biometricEnabled && biometricAvailable,
                    onUnlock = { onUnlock(passcode) },
                    onBiometric = onBiometric,
                    onForgot = { mode = LockScreenMode.RECOVERY_OPTIONS },
                )
                LockScreenMode.RECOVERY_OPTIONS -> RecoveryOptions(
                    deviceCredentialAvailable = deviceCredentialAvailable,
                    onDeviceCredential = {
                        onDeviceCredential { authorized ->
                            if (authorized) mode = LockScreenMode.NEW_PASSCODE
                        }
                    },
                    onRecoveryCode = { mode = LockScreenMode.RECOVERY_CODE },
                    onBack = { mode = LockScreenMode.UNLOCK },
                )
                LockScreenMode.RECOVERY_CODE -> RecoveryCodeInput(
                    value = recoveryInput,
                    onValueChange = { recoveryInput = it.digitsOnly(12) },
                    state = state,
                    onContinue = {
                        onVerifyRecovery(recoveryInput) { mode = LockScreenMode.NEW_PASSCODE }
                    },
                    onBack = { mode = LockScreenMode.RECOVERY_OPTIONS },
                )
                LockScreenMode.NEW_PASSCODE -> NewPasscodeContent(
                    passcode = passcode,
                    confirmation = confirmation,
                    onPasscodeChange = { passcode = it.digitsOnly(6) },
                    onConfirmationChange = { confirmation = it.digitsOnly(6) },
                    state = state,
                    onSave = {
                        onResetPasscode(passcode, confirmation) { recoveryCode ->
                            newRecoveryCode = recoveryCode
                            mode = LockScreenMode.NEW_RECOVERY_CODE
                        }
                    },
                    onBack = { mode = LockScreenMode.RECOVERY_OPTIONS },
                )
                LockScreenMode.NEW_RECOVERY_CODE -> RecoveryCodeDisplay(
                    recoveryCode = newRecoveryCode,
                    onDone = onResetComplete,
                )
            }
        }
    }
}

@Composable
private fun UnlockContent(
    passcode: String,
    onPasscodeChange: (String) -> Unit,
    state: AppLockGateUiState,
    biometricEnabled: Boolean,
    onUnlock: () -> Unit,
    onBiometric: () -> Unit,
    onForgot: () -> Unit,
) {
    Icon(Icons.Outlined.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
    Text(stringResource(R.string.app_locked), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    Text(
        stringResource(R.string.app_locked_support),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(22.dp))
    PasscodeField(passcode, onPasscodeChange, stringResource(R.string.enter_passcode), state.error)
    LockoutMessage(state)
    Button(
        onClick = onUnlock,
        enabled = passcode.length == 6 && !state.working && state.lockoutSeconds == 0,
        modifier = Modifier.fillMaxWidth().height(52.dp),
    ) { Text(stringResource(R.string.unlock)) }
    if (biometricEnabled) {
        OutlinedButton(
            onClick = onBiometric,
            enabled = state.lockoutSeconds == 0,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Icon(Icons.Outlined.Fingerprint, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text(stringResource(R.string.unlock_with_biometric))
        }
    }
    TextButton(onClick = onForgot) { Text(stringResource(R.string.forgot_passcode)) }
}

@Composable
private fun RecoveryOptions(
    deviceCredentialAvailable: Boolean,
    onDeviceCredential: () -> Unit,
    onRecoveryCode: () -> Unit,
    onBack: () -> Unit,
) {
    Icon(Icons.Outlined.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(42.dp))
    Text(stringResource(R.string.reset_app_lock), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Text(stringResource(R.string.reset_app_lock_support), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    Spacer(Modifier.height(22.dp))
    Button(onClick = onDeviceCredential, enabled = deviceCredentialAvailable, modifier = Modifier.fillMaxWidth().height(52.dp)) {
        Text(stringResource(R.string.use_device_screen_lock))
    }
    if (!deviceCredentialAvailable) {
        Text(stringResource(R.string.device_lock_unavailable), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
    BackAndActionRow(
        actionLabel = stringResource(R.string.use_recovery_code),
        onBack = onBack,
        onAction = onRecoveryCode,
    )
}

@Composable
private fun RecoveryCodeInput(
    value: String,
    onValueChange: (String) -> Unit,
    state: AppLockGateUiState,
    onContinue: () -> Unit,
    onBack: () -> Unit,
) {
    Text(stringResource(R.string.enter_recovery_code), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Text(stringResource(R.string.recovery_code_input_support), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    Spacer(Modifier.height(20.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.recovery_code)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        visualTransformation = RecoveryCodeVisualTransformation,
        isError = state.error == AppLockError.INVALID_RECOVERY_CODE,
        supportingText = if (state.error == AppLockError.INVALID_RECOVERY_CODE) {
            { Text(stringResource(R.string.invalid_recovery_code)) }
        } else null,
    )
    LockoutMessage(state)
    BackAndActionRow(
        actionLabel = stringResource(R.string.continue_label),
        actionEnabled = value.length == 12 && !state.working && state.lockoutSeconds == 0,
        onBack = onBack,
        onAction = onContinue,
    )
}

@Composable
private fun NewPasscodeContent(
    passcode: String,
    confirmation: String,
    onPasscodeChange: (String) -> Unit,
    onConfirmationChange: (String) -> Unit,
    state: AppLockGateUiState,
    onSave: () -> Unit,
    onBack: () -> Unit,
) {
    Icon(Icons.Outlined.Password, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(42.dp))
    Text(stringResource(R.string.create_new_passcode), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Text(stringResource(R.string.create_new_passcode_support), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    Spacer(Modifier.height(20.dp))
    PasscodeField(passcode, onPasscodeChange, stringResource(R.string.new_passcode), state.error)
    PasscodeField(confirmation, onConfirmationChange, stringResource(R.string.confirm_passcode), state.error)
    BackAndActionRow(
        actionLabel = stringResource(R.string.save_new_passcode),
        actionEnabled = passcode.length == 6 && confirmation.length == 6 && !state.working,
        onBack = onBack,
        onAction = onSave,
    )
}

@Composable
private fun RecoveryCodeDisplay(recoveryCode: String, onDone: () -> Unit) {
    Icon(Icons.Outlined.Security, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(42.dp))
    Text(stringResource(R.string.new_recovery_code), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Text(stringResource(R.string.recovery_code_once_support), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    Spacer(Modifier.height(20.dp))
    Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.medium) {
        Text(
            recoveryCode,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
    }
    Spacer(Modifier.height(20.dp))
    Button(onClick = onDone, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text(stringResource(R.string.i_saved_recovery_code)) }
}

@Composable
private fun PasscodeField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: AppLockError?,
) {
    val message = when (error) {
        AppLockError.INVALID_PASSCODE -> R.string.incorrect_passcode
        AppLockError.PASSCODES_DO_NOT_MATCH -> R.string.passcodes_do_not_match
        else -> null
    }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        isError = message != null,
        supportingText = message?.let { res -> ({ Text(stringResource(res)) }) },
    )
}

@Composable
private fun LockoutMessage(state: AppLockGateUiState) {
    if (state.lockoutSeconds > 0) {
        Text(
            stringResource(R.string.try_again_in_seconds, state.lockoutSeconds),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun BackAndActionRow(
    actionLabel: String,
    onBack: () -> Unit,
    onAction: () -> Unit,
    actionEnabled: Boolean = true,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.weight(1f).height(52.dp),
        ) {
            Text(stringResource(R.string.back))
        }
        Button(
            onClick = onAction,
            enabled = actionEnabled,
            modifier = Modifier.weight(1f).height(52.dp),
        ) {
            Text(actionLabel, maxLines = 1)
        }
    }
}

private fun String.digitsOnly(maxLength: Int): String = filter(Char::isDigit).take(maxLength)

private object RecoveryCodeVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val formatted = buildString {
            text.text.forEachIndexed { index, character ->
                if (index == 4 || index == 8) append('-')
                append(character)
            }
        }
        return TransformedText(
            text = AnnotatedString(formatted),
            offsetMapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int = when {
                    offset <= 4 -> offset
                    offset <= 8 -> offset + 1
                    else -> offset + 2
                }

                override fun transformedToOriginal(offset: Int): Int = when {
                    offset <= 4 -> offset
                    offset <= 9 -> offset - 1
                    else -> offset - 2
                }
            },
        )
    }
}
