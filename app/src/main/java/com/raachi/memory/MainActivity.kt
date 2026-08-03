package com.raachi.memory

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raachi.memory.core.designsystem.theme.RaachiMemoryTheme
import com.raachi.memory.core.navigation.RaachiMemoryApp
import com.raachi.memory.feature.security.AppLockGate
import com.raachi.memory.feature.security.AppLockViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.raachi.memory.domain.model.ThemeMode

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    private val themeViewModel: ThemeViewModel by viewModels()
    private val appLockViewModel: AppLockViewModel by viewModels()
    private var authenticationResult: ((Boolean) -> Unit)? = null
    private var trustedMediaFlowStartedAt: Long? = null
    private lateinit var biometricPrompt: BiometricPrompt
    private val deviceCredentialLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        authenticationResult.complete(result.resultCode == RESULT_OK)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        biometricPrompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    authenticationResult.complete(true)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    authenticationResult.complete(false)
                }
            },
        )
        setContent {
            val themeMode by themeViewModel.themeMode.collectAsStateWithLifecycle()
            val biometricAvailable = remember { isBiometricAvailable() }
            val deviceCredentialAvailable = remember { isDeviceCredentialAvailable() }
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            SideEffect {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
            RaachiMemoryTheme(darkTheme = darkTheme) {
                AppLockGate(
                    viewModel = appLockViewModel,
                    biometricAvailable = biometricAvailable,
                    deviceCredentialAvailable = deviceCredentialAvailable,
                    authenticateBiometric = ::authenticateBiometric,
                    authenticateDeviceCredential = ::authenticateDeviceCredential,
                ) {
                    RaachiMemoryApp(biometricAvailable = biometricAvailable)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations && !isTrustedMediaFlowActive()) appLockViewModel.lock()
    }

    fun beginTrustedMediaFlow() {
        trustedMediaFlowStartedAt = SystemClock.elapsedRealtime()
    }

    fun endTrustedMediaFlow() {
        trustedMediaFlowStartedAt = null
    }

    private fun isTrustedMediaFlowActive(): Boolean {
        val startedAt = trustedMediaFlowStartedAt ?: return false
        val active = SystemClock.elapsedRealtime() - startedAt <= TRUSTED_MEDIA_FLOW_TIMEOUT_MS
        if (!active) trustedMediaFlowStartedAt = null
        return active
    }

    private fun authenticateBiometric(onResult: (Boolean) -> Unit) {
        if (!isBiometricAvailable()) {
            onResult(false)
            return
        }
        authenticationResult = onResult
        biometricPrompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.biometric_prompt_title))
                .setSubtitle(getString(R.string.biometric_prompt_subtitle))
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .setNegativeButtonText(getString(R.string.cancel))
                .build(),
        )
    }

    private fun authenticateDeviceCredential(onResult: (Boolean) -> Unit) {
        if (!isDeviceCredentialAvailable()) {
            onResult(false)
            return
        }
        authenticationResult = onResult
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            biometricPrompt.authenticate(
                BiometricPrompt.PromptInfo.Builder()
                    .setTitle(getString(R.string.device_credential_prompt_title))
                    .setSubtitle(getString(R.string.device_credential_prompt_subtitle))
                    .setAllowedAuthenticators(BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                    .build(),
            )
        } else {
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            @Suppress("DEPRECATION")
            val intent: Intent? = keyguardManager.createConfirmDeviceCredentialIntent(
                getString(R.string.device_credential_prompt_title),
                getString(R.string.device_credential_prompt_subtitle),
            )
            if (intent == null) {
                authenticationResult.complete(false)
            } else {
                deviceCredentialLauncher.launch(intent)
            }
        }
    }

    private fun isBiometricAvailable(): Boolean = BiometricManager.from(this).canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_STRONG,
    ) == BiometricManager.BIOMETRIC_SUCCESS

    private fun isDeviceCredentialAvailable(): Boolean {
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return keyguardManager.isDeviceSecure
    }

    private fun ((Boolean) -> Unit)?.complete(result: Boolean) {
        val callback = this ?: return
        authenticationResult = null
        callback(result)
    }

    private companion object {
        const val TRUSTED_MEDIA_FLOW_TIMEOUT_MS = 2 * 60 * 1_000L
    }
}
