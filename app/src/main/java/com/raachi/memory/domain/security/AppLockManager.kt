package com.raachi.memory.domain.security

import com.raachi.memory.domain.model.AppLockSettings
import com.raachi.memory.domain.repository.AppLockRepository
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

@Singleton
class AppLockSession @Inject constructor() {
    private val unlockedState = MutableStateFlow(false)
    val unlocked = unlockedState.asStateFlow()

    fun unlock() {
        unlockedState.value = true
    }

    fun lock() {
        unlockedState.value = false
    }
}

@Singleton
class AppLockManager @Inject constructor(
    private val repository: AppLockRepository,
    private val session: AppLockSession,
) {
    val settings: Flow<AppLockSettings> = repository.appLockSettings
    val unlocked = session.unlocked

    fun generateRecoveryCode(): String = buildString {
        repeat(12) { index ->
            if (index > 0 && index % 4 == 0) append('-')
            append(SECURE_RANDOM.nextInt(10))
        }
    }

    suspend fun enable(passcode: String, recoveryCode: String, biometricEnabled: Boolean) {
        requireValidPasscode(passcode)
        val passcodeCredential = hash(passcode)
        val recoveryCredential = hash(normalizeRecoveryCode(recoveryCode))
        repository.replaceAppLockSettings(
            AppLockSettings(
                enabled = true,
                passcodeHash = passcodeCredential.hash,
                passcodeSalt = passcodeCredential.salt,
                recoveryHash = recoveryCredential.hash,
                recoverySalt = recoveryCredential.salt,
                biometricEnabled = biometricEnabled,
            ),
        )
        session.unlock()
    }

    suspend fun verifyPasscode(passcode: String): Boolean {
        val settings = repository.appLockSettings.first()
        return settings.enabled && verify(passcode, settings.passcodeHash, settings.passcodeSalt)
    }

    suspend fun verifyRecoveryCode(recoveryCode: String): Boolean {
        val settings = repository.appLockSettings.first()
        return settings.enabled && verify(
            normalizeRecoveryCode(recoveryCode),
            settings.recoveryHash,
            settings.recoverySalt,
        )
    }

    suspend fun changePasscode(currentPasscode: String, newPasscode: String): Boolean {
        requireValidPasscode(newPasscode)
        val settings = repository.appLockSettings.first()
        if (!verify(currentPasscode, settings.passcodeHash, settings.passcodeSalt)) return false
        val credential = hash(newPasscode)
        repository.replaceAppLockSettings(
            settings.copy(passcodeHash = credential.hash, passcodeSalt = credential.salt),
        )
        return true
    }

    suspend fun resetPasscode(newPasscode: String): String {
        requireValidPasscode(newPasscode)
        val settings = repository.appLockSettings.first()
        require(settings.enabled)
        val recoveryCode = generateRecoveryCode()
        val passcodeCredential = hash(newPasscode)
        val recoveryCredential = hash(normalizeRecoveryCode(recoveryCode))
        repository.replaceAppLockSettings(
            settings.copy(
                passcodeHash = passcodeCredential.hash,
                passcodeSalt = passcodeCredential.salt,
                recoveryHash = recoveryCredential.hash,
                recoverySalt = recoveryCredential.salt,
            ),
        )
        return recoveryCode
    }

    suspend fun regenerateRecoveryCode(currentPasscode: String): String? {
        val settings = repository.appLockSettings.first()
        if (!verify(currentPasscode, settings.passcodeHash, settings.passcodeSalt)) return null
        val recoveryCode = generateRecoveryCode()
        val credential = hash(normalizeRecoveryCode(recoveryCode))
        repository.replaceAppLockSettings(
            settings.copy(recoveryHash = credential.hash, recoverySalt = credential.salt),
        )
        return recoveryCode
    }

    suspend fun disable(passcode: String): Boolean {
        if (!verifyPasscode(passcode)) return false
        repository.replaceAppLockSettings(AppLockSettings())
        session.unlock()
        return true
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        val settings = repository.appLockSettings.first()
        if (settings.enabled) repository.replaceAppLockSettings(settings.copy(biometricEnabled = enabled))
    }

    fun unlockSession() = session.unlock()

    fun lockSession() = session.lock()

    private suspend fun hash(value: String): StoredCredential = withContext(Dispatchers.Default) {
        val salt = ByteArray(SALT_BYTES).also(SECURE_RANDOM::nextBytes)
        StoredCredential(
            hash = Base64.getEncoder().encodeToString(derive(value, salt)),
            salt = Base64.getEncoder().encodeToString(salt),
        )
    }

    private suspend fun verify(value: String, expectedHash: String, encodedSalt: String): Boolean =
        withContext(Dispatchers.Default) {
            if (expectedHash.isBlank() || encodedSalt.isBlank()) return@withContext false
            runCatching {
                val expected = Base64.getDecoder().decode(expectedHash)
                val salt = Base64.getDecoder().decode(encodedSalt)
                MessageDigest.isEqual(expected, derive(value, salt))
            }.getOrDefault(false)
        }

    private fun derive(value: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(value.toCharArray(), salt, PBKDF2_ITERATIONS, HASH_BITS)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun requireValidPasscode(passcode: String) {
        require(passcode.length == 6 && passcode.all(Char::isDigit))
    }

    private fun normalizeRecoveryCode(code: String): String = code.filter(Char::isDigit)

    private data class StoredCredential(val hash: String, val salt: String)

    private companion object {
        const val SALT_BYTES = 16
        const val PBKDF2_ITERATIONS = 210_000
        const val HASH_BITS = 256
        val SECURE_RANDOM = SecureRandom()
    }
}
